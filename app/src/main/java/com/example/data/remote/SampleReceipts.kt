package com.example.data.remote

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.example.data.model.LineItem
import com.example.data.model.ScannedReceipt

data class SampleReceiptTemplate(
    val title: String,
    val storeName: String,
    val date: String,
    val items: List<Pair<String, Double>>,
    val tax: Double,
    val total: Double,
    val category: String,
    val paymentMethod: String
)

object SampleReceipts {
    val templates = listOf(
        SampleReceiptTemplate(
            title = "Supermarket Grocery Receipt",
            storeName = "Trader Joe's Market #12",
            date = "2026-10-05",
            items = listOf(
                "Organic Whole Milk" to 4.99,
                "Fresh Hass Avocados (4pk)" to 5.49,
                "Artisan Sourdough" to 4.29,
                "Wild Salmon Fillet" to 14.99,
                "Dark Chocolate Bar 85%" to 2.99
            ),
            tax = 2.45,
            total = 35.20,
            category = "Groceries",
            paymentMethod = "Credit Card"
        ),
        SampleReceiptTemplate(
            title = "Restaurant Dining Receipt",
            storeName = "Bistro Italia & Pizzeria",
            date = "2026-10-04",
            items = listOf(
                "Margherita Pizza Large" to 18.50,
                "Fettuccine Alfredo" to 16.00,
                "Caesar Salad" to 9.50,
                "Italian Sparkling Water" to 4.50
            ),
            tax = 4.20,
            total = 52.70,
            category = "Food & Dining",
            paymentMethod = "Debit Card"
        ),
        SampleReceiptTemplate(
            title = "Tech & Electronics Receipt",
            storeName = "Best Tech Electronics",
            date = "2026-10-02",
            items = listOf(
                "USB-C Fast Charger 65W" to 29.99,
                "Screen Cleaner Kit" to 12.50,
                "Braided Cable 6ft" to 14.99
            ),
            tax = 4.80,
            total = 62.28,
            category = "Shopping",
            paymentMethod = "Apple Pay"
        )
    )

    fun createReceiptBitmap(template: SampleReceiptTemplate): Bitmap {
        val width = 600
        val height = 900
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background paper
        canvas.drawColor(Color.parseColor("#F9F8F3"))

        val paint = Paint().apply {
            color = Color.parseColor("#111111")
            isAntiAlias = true
        }

        // Border & shadow line
        val borderPaint = Paint().apply {
            color = Color.parseColor("#CCCCCC")
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRect(20f, 20f, (width - 20).toFloat(), (height - 20).toFloat(), borderPaint)

        // Header
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        paint.textSize = 34f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(template.storeName.uppercase(), (width / 2).toFloat(), 80f, paint)

        paint.textSize = 22f
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        canvas.drawText("DATE: ${template.date}   TIME: 14:32", (width / 2).toFloat(), 120f, paint)
        canvas.drawText("========================================", (width / 2).toFloat(), 150f, paint)

        // Column headers
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 22f
        canvas.drawText("ITEM DESCRIPTION", 40f, 190f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("PRICE", (width - 40).toFloat(), 190f, paint)
        canvas.drawText("----------------------------------------", (width - 40).toFloat(), 215f, paint)

        var y = 250f
        paint.textSize = 22f
        for ((name, price) in template.items) {
            paint.textAlign = Paint.Align.LEFT
            val displayName = if (name.length > 22) name.substring(0, 22) else name
            canvas.drawText(displayName, 40f, y, paint)

            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(String.format("$%.2f", price), (width - 40).toFloat(), y, paint)
            y += 42f
        }

        y += 20f
        canvas.drawText("----------------------------------------", (width - 40).toFloat(), y, paint)
        y += 40f

        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("SUBTOTAL:", 40f, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        val subtotal = template.total - template.tax
        canvas.drawText(String.format("$%.2f", subtotal), (width - 40).toFloat(), y, paint)
        y += 40f

        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("TAX (8.25%):", 40f, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(String.format("$%.2f", template.tax), (width - 40).toFloat(), y, paint)
        y += 45f

        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        paint.textSize = 28f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("TOTAL AMOUNT:", 40f, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(String.format("$%.2f", template.total), (width - 40).toFloat(), y, paint)
        y += 50f

        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        paint.textSize = 20f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("PAYMENT METHOD: ${template.paymentMethod.uppercase()}", (width / 2).toFloat(), y, paint)
        y += 35f
        canvas.drawText("THANK YOU FOR SHOPPING WITH US!", (width / 2).toFloat(), y, paint)

        return bitmap
    }

    fun templateToScannedReceipt(template: SampleReceiptTemplate): ScannedReceipt {
        return ScannedReceipt(
            merchantName = template.storeName,
            dateString = template.date,
            totalAmount = template.total,
            taxAmount = template.tax,
            suggestedCategory = template.category,
            paymentMethod = template.paymentMethod,
            lineItems = template.items.map { LineItem(it.first, it.second) },
            rawOcrText = "Receipt from ${template.storeName} for $${template.total}"
        )
    }
}
