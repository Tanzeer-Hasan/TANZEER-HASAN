package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconIdentifier: String,
    val colorHex: String,
    val type: String, // "EXPENSE" or "INCOME"
    val isDefault: Boolean = false
)
