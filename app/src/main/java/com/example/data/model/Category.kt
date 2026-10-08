package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class CategoryType(val displayName: String) {
    FIXED_EXPENSE("Fixed Expense"),
    VARIABLE_EXPENSE("Variable Expense"),
    INCOME("Income");

    companion object {
        fun fromString(value: String): CategoryType {
            return when {
                value.equals("FIXED_EXPENSE", ignoreCase = true) || value.equals("FIXED", ignoreCase = true) -> FIXED_EXPENSE
                value.equals("VARIABLE_EXPENSE", ignoreCase = true) || value.equals("VARIABLE", ignoreCase = true) -> VARIABLE_EXPENSE
                value.equals("INCOME", ignoreCase = true) -> INCOME
                else -> VARIABLE_EXPENSE
            }
        }
    }
}

data class CategoryInfo(
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val categoryType: CategoryType = CategoryType.VARIABLE_EXPENSE,
    val defaultBudget: Double = 500.0
) {
    val isExpense: Boolean get() = categoryType != CategoryType.INCOME
}

object IconPalette {
    val iconsMap = mapOf(
        "Home" to Icons.Default.Home,
        "AccountBalance" to Icons.Default.AccountBalance,
        "LocalGasStation" to Icons.Default.LocalGasStation,
        "Shield" to Icons.Default.Shield,
        "DirectionsCar" to Icons.Default.DirectionsCar,
        "CreditCard" to Icons.Default.CreditCard,
        "School" to Icons.Default.School,
        "Smartphone" to Icons.Default.Smartphone,
        "ChildCare" to Icons.Default.ChildCare,
        "LocalGroceryStore" to Icons.Default.LocalGroceryStore,
        "CardGiftcard" to Icons.Default.CardGiftcard,
        "Fastfood" to Icons.Default.Fastfood,
        "ShoppingBag" to Icons.Default.ShoppingBag,
        "Movie" to Icons.Default.Movie,
        "FitnessCenter" to Icons.Default.FitnessCenter,
        "Build" to Icons.Default.Build,
        "LocalHospital" to Icons.Default.LocalHospital,
        "Flight" to Icons.Default.Flight,
        "Pets" to Icons.Default.Pets,
        "Work" to Icons.Default.Work,
        "Savings" to Icons.Default.Savings,
        "Payments" to Icons.Default.Payments
    )

    fun getIcon(identifier: String): ImageVector {
        return iconsMap[identifier] ?: Icons.Default.Category
    }
}

object ColorPalette {
    val predefinedColors = listOf(
        "#3F51B5", "#009688", "#795548", "#0288D1", "#E91E63",
        "#4CAF50", "#FF9800", "#9C27B0", "#607D8B", "#F4511E",
        "#2E7D32", "#00BCD4", "#D81B60", "#7CB342", "#8E24AA"
    )

    fun parseHexColor(hex: String): Color {
        return try {
            val cleanHex = hex.removePrefix("#")
            val colorInt = cleanHex.toLong(16).toInt()
            if (cleanHex.length == 6) {
                Color(colorInt or 0xFF000000.toInt())
            } else {
                Color(colorInt)
            }
        } catch (e: Exception) {
            Color(0xFF607D8B)
        }
    }
}

object DefaultCategories {
    val defaults = listOf(
        // FIXED EXPENSES
        CategoryEntity(name = "Rent / Mortgage", iconIdentifier = "Home", colorHex = "#3F51B5", type = "FIXED_EXPENSE", isDefault = true),
        CategoryEntity(name = "City Tax", iconIdentifier = "AccountBalance", colorHex = "#607D8B", type = "FIXED_EXPENSE", isDefault = true),
        CategoryEntity(name = "Gas & Heating", iconIdentifier = "LocalGasStation", colorHex = "#F4511E", type = "FIXED_EXPENSE", isDefault = true),
        CategoryEntity(name = "Car Insurance", iconIdentifier = "Shield", colorHex = "#0288D1", type = "FIXED_EXPENSE", isDefault = true),
        CategoryEntity(name = "Home Insurance", iconIdentifier = "Shield", colorHex = "#009688", type = "FIXED_EXPENSE", isDefault = true),
        CategoryEntity(name = "Car Loan", iconIdentifier = "DirectionsCar", colorHex = "#00BCD4", type = "FIXED_EXPENSE", isDefault = true),
        CategoryEntity(name = "Card Loan", iconIdentifier = "CreditCard", colorHex = "#E91E63", type = "FIXED_EXPENSE", isDefault = true),
        CategoryEntity(name = "Student Loan", iconIdentifier = "School", colorHex = "#8E24AA", type = "FIXED_EXPENSE", isDefault = true),
        CategoryEntity(name = "Mobile & Phone", iconIdentifier = "Smartphone", colorHex = "#795548", type = "FIXED_EXPENSE", isDefault = true),
        CategoryEntity(name = "Child Activities", iconIdentifier = "ChildCare", colorHex = "#FF9800", type = "FIXED_EXPENSE", isDefault = true),

        // VARIABLE EXPENSES
        CategoryEntity(name = "Grocery", iconIdentifier = "LocalGroceryStore", colorHex = "#4CAF50", type = "VARIABLE_EXPENSE", isDefault = true),
        CategoryEntity(name = "Gift", iconIdentifier = "CardGiftcard", colorHex = "#D81B60", type = "VARIABLE_EXPENSE", isDefault = true),
        CategoryEntity(name = "Food & Dining", iconIdentifier = "Fastfood", colorHex = "#FF9800", type = "VARIABLE_EXPENSE", isDefault = true),
        CategoryEntity(name = "Shopping", iconIdentifier = "ShoppingBag", colorHex = "#E91E63", type = "VARIABLE_EXPENSE", isDefault = true),
        CategoryEntity(name = "Entertainment", iconIdentifier = "Movie", colorHex = "#9C27B0", type = "VARIABLE_EXPENSE", isDefault = true),
        CategoryEntity(name = "Healthcare", iconIdentifier = "FitnessCenter", colorHex = "#009688", type = "VARIABLE_EXPENSE", isDefault = true),

        // INCOME
        CategoryEntity(name = "Salary", iconIdentifier = "Work", colorHex = "#2E7D32", type = "INCOME", isDefault = true),
        CategoryEntity(name = "Freelance", iconIdentifier = "Work", colorHex = "#0288D1", type = "INCOME", isDefault = true),
        CategoryEntity(name = "Investment", iconIdentifier = "Savings", colorHex = "#4CAF50", type = "INCOME", isDefault = true)
    )
}

object CategoryResolver {
    fun resolve(categoryName: String, dbCategories: List<CategoryEntity> = emptyList()): CategoryInfo {
        val foundDb = dbCategories.firstOrNull { it.name.equals(categoryName, ignoreCase = true) }
        if (foundDb != null) {
            val catType = CategoryType.fromString(foundDb.type)
            return CategoryInfo(
                name = foundDb.name,
                icon = IconPalette.getIcon(foundDb.iconIdentifier),
                color = ColorPalette.parseHexColor(foundDb.colorHex),
                categoryType = catType
            )
        }

        val foundDefault = DefaultCategories.defaults.firstOrNull { it.name.equals(categoryName, ignoreCase = true) }
        if (foundDefault != null) {
            val catType = CategoryType.fromString(foundDefault.type)
            return CategoryInfo(
                name = foundDefault.name,
                icon = IconPalette.getIcon(foundDefault.iconIdentifier),
                color = ColorPalette.parseHexColor(foundDefault.colorHex),
                categoryType = catType
            )
        }

        return CategoryInfo(
            name = categoryName,
            icon = Icons.Default.Category,
            color = Color(0xFF607D8B),
            categoryType = CategoryType.VARIABLE_EXPENSE
        )
    }
}
