package com.example.data.model

data class LineItem(
    val name: String,
    val price: Double
)

data class ScannedReceipt(
    val merchantName: String = "",
    val dateString: String = "",
    val totalAmount: Double = 0.0,
    val taxAmount: Double = 0.0,
    val suggestedCategory: String = "Groceries",
    val paymentMethod: String = "Credit Card",
    val lineItems: List<LineItem> = emptyList(),
    val rawOcrText: String = ""
)
