package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String, // "EXPENSE" or "INCOME"
    val category: String,
    val paymentMethod: String,
    val dateMillis: Long,
    val notes: String = "",
    val receiptImageUri: String? = null,
    val lineItemsJson: String? = null,
    val isFlagged: Boolean = false
)
