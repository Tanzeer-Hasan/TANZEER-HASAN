package com.example.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.CategoryInfo
import com.example.data.model.CategoryResolver
import com.example.data.model.ColorPalette
import com.example.data.model.IconPalette
import com.example.data.model.LineItem
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.repository.ExpenseRepository
import com.example.data.security.SecurityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ExpenseFilterState(
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val selectedType: TransactionType? = null,
    val selectedPaymentMethod: String? = null,
    val monthOffset: Int = 0
)

data class CategoryExpenseSummary(
    val categoryName: String,
    val categoryInfo: CategoryInfo,
    val totalSpent: Double,
    val percentage: Float,
    val budgetLimit: Double? = null
)

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ExpenseRepository
    val securityManager: SecurityManager = SecurityManager(application)

    private val _filterState = MutableStateFlow(ExpenseFilterState())
    val filterState: StateFlow<ExpenseFilterState> = _filterState.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ExpenseRepository(
            database.transactionDao(),
            database.budgetDao(),
            database.categoryDao()
        )
        viewModelScope.launch(Dispatchers.IO) {
            repository.seedInitialDataIfEmpty()
        }
    }

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allBudgets: StateFlow<List<BudgetEntity>> = repository.allBudgets
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allCategories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        allTransactions,
        _filterState
    ) { transactions, filter ->
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, filter.monthOffset)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val monthStart = cal.timeInMillis

        cal.add(Calendar.MONTH, 1)
        val monthEnd = cal.timeInMillis - 1

        transactions.filter { tx ->
            val matchesSearch = filter.searchQuery.isBlank() ||
                    tx.title.contains(filter.searchQuery, ignoreCase = true) ||
                    tx.category.contains(filter.searchQuery, ignoreCase = true) ||
                    tx.notes.contains(filter.searchQuery, ignoreCase = true)

            val matchesCategory = filter.selectedCategory == null || tx.category.equals(filter.selectedCategory, ignoreCase = true)
            val matchesType = filter.selectedType == null || tx.type.equals(filter.selectedType.name, ignoreCase = true)
            val matchesPayment = filter.selectedPaymentMethod == null || tx.paymentMethod.equals(filter.selectedPaymentMethod, ignoreCase = true)
            val matchesMonth = tx.dateMillis in monthStart..monthEnd

            matchesSearch && matchesCategory && matchesType && matchesPayment && matchesMonth
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val currentMonthTotalIncome: StateFlow<Double> = filteredTransactions.combine(allTransactions) { _, _ ->
        val monthTransactions = getTransactionsForMonthOffset(_filterState.value.monthOffset)
        monthTransactions
            .filter { it.type == TransactionType.INCOME.name }
            .sumOf { it.amount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    val currentMonthTotalExpense: StateFlow<Double> = filteredTransactions.combine(allTransactions) { _, _ ->
        val monthTransactions = getTransactionsForMonthOffset(_filterState.value.monthOffset)
        monthTransactions
            .filter { it.type == TransactionType.EXPENSE.name }
            .sumOf { it.amount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    val categorySummaries: StateFlow<List<CategoryExpenseSummary>> = combine(
        allTransactions,
        allBudgets,
        allCategories,
        _filterState
    ) { transactions, budgets, categories, filter ->
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, filter.monthOffset)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val monthStart = cal.timeInMillis

        cal.add(Calendar.MONTH, 1)
        val monthEnd = cal.timeInMillis - 1

        val monthExpenses = transactions.filter {
            it.type == TransactionType.EXPENSE.name && it.dateMillis in monthStart..monthEnd
        }

        val totalMonthExpense = monthExpenses.sumOf { it.amount }
        val budgetMap = budgets.associate { it.categoryName to it.monthlyLimit }
        val categoryMap = categories.associateBy { it.name }

        val categoryGroups = monthExpenses.groupBy { it.category }

        categoryGroups.map { (catName, catTxs) ->
            val sum = catTxs.sumOf { it.amount }
            val catInfo = CategoryResolver.resolve(catName, categories)

            val pct = if (totalMonthExpense > 0) (sum / totalMonthExpense).toFloat() else 0f
            CategoryExpenseSummary(
                categoryName = catName,
                categoryInfo = catInfo,
                totalSpent = sum,
                percentage = pct,
                budgetLimit = budgetMap[catName]
            )
        }.sortedByDescending { it.totalSpent }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setFilterSearchQuery(query: String) {
        _filterState.value = _filterState.value.copy(searchQuery = query)
    }

    fun setFilterCategory(category: String?) {
        _filterState.value = _filterState.value.copy(selectedCategory = category)
    }

    fun setFilterType(type: TransactionType?) {
        _filterState.value = _filterState.value.copy(selectedType = type)
    }

    fun setFilterMonthOffset(offset: Int) {
        _filterState.value = _filterState.value.copy(monthOffset = offset)
    }

    fun resetFilters() {
        _filterState.value = ExpenseFilterState()
    }

    // Bug Fix #2: Proper coroutine scope & callback after insert completes
    fun saveTransaction(
        id: Long = 0,
        title: String,
        amount: Double,
        type: TransactionType,
        category: String,
        paymentMethod: String,
        dateMillis: Long,
        notes: String = "",
        receiptImageUri: String? = null,
        lineItems: List<LineItem> = emptyList(),
        onComplete: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val jsonLineItems = if (lineItems.isNotEmpty()) serializeLineItems(lineItems) else null
            val entity = TransactionEntity(
                id = id,
                title = title.ifBlank { category },
                amount = amount,
                type = type.name,
                category = category,
                paymentMethod = paymentMethod,
                dateMillis = dateMillis,
                notes = notes,
                receiptImageUri = receiptImageUri,
                lineItemsJson = jsonLineItems
            )
            if (id > 0) {
                repository.updateTransaction(entity)
            } else {
                repository.insertTransaction(entity)
            }
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    fun deleteTransaction(transaction: TransactionEntity, onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTransaction(transaction)
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    // Feature #4: Custom Category Creation & Deletion
    fun createCustomCategory(
        name: String,
        iconIdentifier: String,
        colorHex: String,
        type: TransactionType
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val dbType = when (type) {
                TransactionType.INCOME -> "INCOME"
                else -> "VARIABLE_EXPENSE"
            }
            val newCategory = CategoryEntity(
                name = name.trim(),
                iconIdentifier = iconIdentifier,
                colorHex = colorHex,
                type = dbType,
                isDefault = false
            )
            repository.insertCategory(newCategory)
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCategory(category)
        }
    }

    fun deleteCategoryByName(categoryName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCategoryByName(categoryName)
        }
    }

    // Feature #5: Budget Target Upsert
    fun setBudgetLimit(categoryName: String, limit: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = BudgetEntity(
                categoryName = categoryName,
                monthlyLimit = limit,
                monthYear = "ALL"
            )
            repository.upsertBudget(entity)
        }
    }

    private fun getTransactionsForMonthOffset(offset: Int): List<TransactionEntity> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, offset)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val monthStart = cal.timeInMillis

        cal.add(Calendar.MONTH, 1)
        val monthEnd = cal.timeInMillis - 1

        return allTransactions.value.filter { it.dateMillis in monthStart..monthEnd }
    }

    fun serializeLineItems(items: List<LineItem>): String {
        val array = JSONArray()
        for (item in items) {
            val obj = JSONObject()
            obj.put("name", item.name)
            obj.put("price", item.price)
            array.put(obj)
        }
        return array.toString()
    }

    fun deserializeLineItems(jsonStr: String?): List<LineItem> {
        if (jsonStr.isNullOrBlank()) return emptyList()
        val list = mutableListOf<LineItem>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(LineItem(obj.getString("name"), obj.getDouble("price")))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun formatMonthYear(offset: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, offset)
        val format = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return format.format(cal.time)
    }

    fun generateCSVReport(): String {
        val transactions = filteredTransactions.value
        val sb = StringBuilder()
        sb.append("Date,Title,Type,Category,Amount,Payment Method,Notes\n")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        for (t in transactions) {
            val dateStr = dateFormat.format(Date(t.dateMillis))
            sb.append("\"$dateStr\",\"${t.title.replace("\"", "\"\"")}\",\"${t.type}\",\"${t.category}\",${t.amount},\"${t.paymentMethod}\",\"${t.notes.replace("\"", "\"\"")}\"\n")
        }
        return sb.toString()
    }
}
