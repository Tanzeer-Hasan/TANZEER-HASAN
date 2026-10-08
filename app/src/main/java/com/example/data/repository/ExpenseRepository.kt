package com.example.data.repository

import com.example.data.local.BudgetDao
import com.example.data.local.CategoryDao
import com.example.data.local.TransactionDao
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.DefaultCategories
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.Calendar

class ExpenseRepository(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao,
    private val categoryDao: CategoryDao
) {
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allBudgets: Flow<List<BudgetEntity>> = budgetDao.getAllBudgets()
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    fun getTransactionById(id: Long): Flow<TransactionEntity?> = transactionDao.getTransactionById(id)

    suspend fun insertTransaction(transaction: TransactionEntity): Long = withContext(Dispatchers.IO) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun upsertBudget(budget: BudgetEntity): Long = withContext(Dispatchers.IO) {
        budgetDao.upsertBudget(budget)
    }

    suspend fun insertCategory(category: CategoryEntity): Long = withContext(Dispatchers.IO) {
        categoryDao.insertCategory(category)
    }

    suspend fun deleteCategory(category: CategoryEntity) = withContext(Dispatchers.IO) {
        categoryDao.deleteCategory(category)
    }

    suspend fun deleteCategoryByName(name: String) = withContext(Dispatchers.IO) {
        categoryDao.deleteCategoryByName(name)
        budgetDao.deleteBudgetByCategory(name)
    }

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        // Seed default categories if empty
        val existingCategories = allCategories.first()
        if (existingCategories.isEmpty()) {
            DefaultCategories.defaults.forEach { categoryDao.insertCategory(it) }
        }

        // Seed initial transactions & budgets if empty
        val currentTransactions = allTransactions.first()
        if (currentTransactions.isEmpty()) {
            val now = System.currentTimeMillis()

            val sampleTransactions = listOf(
                TransactionEntity(
                    title = "Monthly Salary Deposit",
                    amount = 4500.00,
                    type = TransactionType.INCOME.name,
                    category = "Salary",
                    paymentMethod = "Bank Transfer",
                    dateMillis = now - 86400000L * 2,
                    notes = "Tech Corp Inc. Direct Deposit"
                ),
                TransactionEntity(
                    title = "Trader Joe's Groceries",
                    amount = 87.45,
                    type = TransactionType.EXPENSE.name,
                    category = "Groceries",
                    paymentMethod = "Credit Card",
                    dateMillis = now - 3600000L * 5,
                    notes = "Weekly grocery restock",
                    lineItemsJson = """[{"name":"Organic Milk","price":4.99},{"name":"Fresh Produce","price":24.50},{"name":"Sourdough Bread","price":5.99},{"name":"Almond Butter","price":8.99}]"""
                ),
                TransactionEntity(
                    title = "Blue Bottle Coffee",
                    amount = 14.50,
                    type = TransactionType.EXPENSE.name,
                    category = "Food & Dining",
                    paymentMethod = "Apple Pay",
                    dateMillis = now - 3600000L * 18,
                    notes = "Oat milk latte & avocado toast"
                ),
                TransactionEntity(
                    title = "Shell Gas Station",
                    amount = 48.20,
                    type = TransactionType.EXPENSE.name,
                    category = "Transportation",
                    paymentMethod = "Debit Card",
                    dateMillis = now - 86400000L * 3,
                    notes = "Full tank refill"
                ),
                TransactionEntity(
                    title = "Electric & Power Utility",
                    amount = 112.30,
                    type = TransactionType.EXPENSE.name,
                    category = "Housing & Bills",
                    paymentMethod = "Bank Transfer",
                    dateMillis = now - 86400000L * 5,
                    notes = "Monthly energy bill"
                ),
                TransactionEntity(
                    title = "Freelance UX Design",
                    amount = 850.00,
                    type = TransactionType.INCOME.name,
                    category = "Freelance",
                    paymentMethod = "Digital Wallet",
                    dateMillis = now - 86400000L * 7,
                    notes = "Client website consultation"
                ),
                TransactionEntity(
                    title = "AMC Movie Tickets & Snacks",
                    amount = 38.00,
                    type = TransactionType.EXPENSE.name,
                    category = "Entertainment",
                    paymentMethod = "Credit Card",
                    dateMillis = now - 86400000L * 8,
                    notes = "Weekend cinema"
                )
            )

            sampleTransactions.forEach { transactionDao.insertTransaction(it) }

            val initialBudgets = listOf(
                BudgetEntity(categoryName = "Groceries", monthlyLimit = 450.0, monthYear = "ALL"),
                BudgetEntity(categoryName = "Food & Dining", monthlyLimit = 300.0, monthYear = "ALL"),
                BudgetEntity(categoryName = "Shopping", monthlyLimit = 250.0, monthYear = "ALL"),
                BudgetEntity(categoryName = "Housing & Bills", monthlyLimit = 1200.0, monthYear = "ALL"),
                BudgetEntity(categoryName = "Transportation", monthlyLimit = 200.0, monthYear = "ALL"),
                BudgetEntity(categoryName = "Entertainment", monthlyLimit = 150.0, monthYear = "ALL")
            )

            initialBudgets.forEach { budgetDao.upsertBudget(it) }
        }
    }
}
