package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long = 0L,
    val categoryName: String,
    val monthlyLimit: Double,
    val monthYear: String // e.g., "2026-10" or "ALL"
)
