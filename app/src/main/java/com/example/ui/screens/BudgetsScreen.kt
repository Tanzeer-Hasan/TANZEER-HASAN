package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CategoryType
import com.example.data.viewmodel.ExpenseViewModel
import com.example.ui.components.AddCategoryBottomSheet
import com.example.ui.components.BudgetProgressCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val summaries by viewModel.categorySummaries.collectAsStateWithLifecycle()
    val allBudgets by viewModel.allBudgets.collectAsStateWithLifecycle()
    val dbCategories by viewModel.allCategories.collectAsStateWithLifecycle()

    var selectedTabType by remember { mutableStateOf(CategoryType.FIXED_EXPENSE) }

    var editingCategory by remember { mutableStateOf<String?>(null) }
    var editAmountText by remember { mutableStateOf("") }
    var deletingCategory by remember { mutableStateOf<String?>(null) }
    var showAddCategoryBottomSheet by remember { mutableStateOf(false) }

    val budgetMap = remember(allBudgets) {
        allBudgets.associate { it.categoryName to it.monthlyLimit }
    }

    val filteredCategories = dbCategories.filter { cat ->
        when (selectedTabType) {
            CategoryType.FIXED_EXPENSE -> cat.type == "FIXED_EXPENSE"
            CategoryType.VARIABLE_EXPENSE -> cat.type == "VARIABLE_EXPENSE" || cat.type == "EXPENSE"
            CategoryType.INCOME -> cat.type == "INCOME"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories & Budgets", fontWeight = FontWeight.Bold) },
                actions = {
                    Button(
                        onClick = { showAddCategoryBottomSheet = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("add_category_budgets_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Category")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding() + 8.dp,
                bottom = paddingValues.calculateBottomPadding() + 80.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Text(
                    text = "Manage Fixed Expenses, Variable Expenses, and Income. Set budgets or delete unneeded categories.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Tab Selector: Fixed Expenses vs Variable Expenses vs Income
            item {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SegmentedButton(
                        selected = selectedTabType == CategoryType.FIXED_EXPENSE,
                        onClick = { selectedTabType = CategoryType.FIXED_EXPENSE },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                        modifier = Modifier.testTag("tab_fixed_expenses")
                    ) {
                        Text("Fixed", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    SegmentedButton(
                        selected = selectedTabType == CategoryType.VARIABLE_EXPENSE,
                        onClick = { selectedTabType = CategoryType.VARIABLE_EXPENSE },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                        modifier = Modifier.testTag("tab_variable_expenses")
                    ) {
                        Text("Variable", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    SegmentedButton(
                        selected = selectedTabType == CategoryType.INCOME,
                        onClick = { selectedTabType = CategoryType.INCOME },
                        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                        modifier = Modifier.testTag("tab_income_categories")
                    ) {
                        Text("Income", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (filteredCategories.isEmpty()) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "No categories found in this section.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredCategories, key = { it.id }) { category ->
                    val categoryName = category.name
                    val spent = summaries.firstOrNull { it.categoryName == categoryName }?.totalSpent ?: 0.0
                    val limit = budgetMap[categoryName] ?: 500.0

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            BudgetProgressCard(
                                categoryName = categoryName,
                                spentAmount = spent,
                                limitAmount = limit,
                                dbCategories = dbCategories,
                                onEditBudget = {
                                    editingCategory = categoryName
                                    editAmountText = limit.toString()
                                }
                            )
                        }

                        IconButton(
                            onClick = { deletingCategory = categoryName },
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .testTag("delete_category_btn_$categoryName")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Category",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }

    // Edit Budget Limit Dialog
    editingCategory?.let { cat ->
        AlertDialog(
            onDismissRequest = { editingCategory = null },
            title = { Text("Set Budget for $cat") },
            text = {
                Column {
                    Text("Enter monthly spending limit ($):")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editAmountText,
                        onValueChange = { editAmountText = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("budget_input_field")
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val limitVal = editAmountText.toDoubleOrNull() ?: 0.0
                        if (limitVal >= 0) {
                            viewModel.setBudgetLimit(cat, limitVal)
                        }
                        editingCategory = null
                    },
                    modifier = Modifier.testTag("save_budget_dialog_button")
                ) {
                    Text("Save Target", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingCategory = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Category Confirmation Dialog
    deletingCategory?.let { cat ->
        AlertDialog(
            onDismissRequest = { deletingCategory = null },
            title = { Text("Delete Category?") },
            text = { Text("Are you sure you want to delete '$cat'? Transactions using this category will remain, but the category will be removed.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCategoryByName(cat)
                        deletingCategory = null
                    },
                    modifier = Modifier.testTag("confirm_delete_category_button")
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingCategory = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Category Bottom Sheet
    if (showAddCategoryBottomSheet) {
        AddCategoryBottomSheet(
            onDismiss = { showAddCategoryBottomSheet = false },
            onCreateCategory = { catName, iconId, hex, type ->
                viewModel.createCustomCategory(catName, iconId, hex, type)
            }
        )
    }
}
