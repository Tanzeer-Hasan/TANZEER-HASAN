package com.example.data.remote

import android.graphics.Bitmap
import android.util.Log
import com.example.data.model.LineItem
import com.example.data.model.ScannedReceipt
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.regex.Pattern
import kotlin.coroutines.resume

object LocalReceiptScanner {
    private const val TAG = "LocalReceiptScanner"

    suspend fun scanReceiptImageOnDevice(bitmap: Bitmap): Result<ScannedReceipt> = suspendCancellableCoroutine { continuation ->
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val rawText = visionText.text
                    val scannedReceipt = parseTextWithHeuristics(visionText, rawText)
                    if (continuation.isActive) {
                        continuation.resume(Result.success(scannedReceipt))
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "ML Kit OCR scanning failed", e)
                    if (continuation.isActive) {
                        continuation.resume(Result.failure(e))
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in ML Kit OCR setup", e)
            if (continuation.isActive) {
                continuation.resume(Result.failure(e))
            }
        }
    }

    private fun parseTextWithHeuristics(visionText: com.google.mlkit.vision.text.Text, rawText: String): ScannedReceipt {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }

        // 1. Merchant Name Extraction: Top 25% lines or top 3 lines
        var merchant = "Unspecified Store"
        for (i in 0 until minOf(4, lines.size)) {
            val line = lines[i]
            if (!line.contains(Regex("(?i)welcome|receipt|thank|copy|cashier|date|time|#"))) {
                merchant = line.replace(Regex("[^a-zA-Z0-9&'\\s]"), "").trim()
                if (merchant.length >= 3) break
            }
        }

        // 2. Amount Extraction via Regex & max decimal finding
        var totalAmount = 0.0
        // Match lines like "TOTAL $45.99" or "TOTAL 45.99" or "BALANCE DUE: 12.50"
        val totalPattern = Pattern.compile("(?i)(?:total|balance|due|amount|paid)[\\s:$]*\\$?([0-9]+[.,][0-9]{2})")
        val matcher = totalPattern.matcher(rawText)
        val candidateAmounts = mutableListOf<Double>()

        while (matcher.find()) {
            matcher.group(1)?.let { amtStr ->
                amtStr.replace(",", ".").toDoubleOrNull()?.let { candidateAmounts.add(it) }
            }
        }

        if (candidateAmounts.isNotEmpty()) {
            totalAmount = candidateAmounts.maxOrNull() ?: 0.0
        } else {
            // General decimal match finding (e.g. 12.34)
            val generalDecimalPattern = Pattern.compile("\\$?([0-9]+\\.[0-9]{2})")
            val genMatcher = generalDecimalPattern.matcher(rawText)
            val decimals = mutableListOf<Double>()
            while (genMatcher.find()) {
                genMatcher.group(1)?.toDoubleOrNull()?.let { decimals.add(it) }
            }
            totalAmount = decimals.maxOrNull() ?: 0.0
        }

        // 3. Date Extraction
        var dateString = ""
        val datePattern = Pattern.compile("\\b(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})\\b|\\b((?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\\s+\\d{1,2},?\\s+\\d{4})\\b", Pattern.CASE_INSENSITIVE)
        val dateMatcher = datePattern.matcher(rawText)
        if (dateMatcher.find()) {
            dateString = dateMatcher.group(0) ?: ""
        }

        // 4. Line Items Extraction
        val lineItems = mutableListOf<LineItem>()
        val itemPattern = Pattern.compile("^(.+?)\\s+\\$?([0-9]+\\.[0-9]{2})$")
        for (line in lines) {
            val itemMatcher = itemPattern.matcher(line)
            if (itemMatcher.find()) {
                val itemName = itemMatcher.group(1)?.trim() ?: ""
                val itemPrice = itemMatcher.group(2)?.toDoubleOrNull() ?: 0.0
                if (itemName.isNotBlank() && !itemName.contains(Regex("(?i)total|tax|subtotal|cash|change|due"))) {
                    lineItems.add(LineItem(itemName, itemPrice))
                }
            }
        }

        // 5. Category heuristic mapping
        val suggestedCategory = guessCategoryFromText(rawText, merchant)

        return ScannedReceipt(
            merchantName = merchant,
            dateString = dateString,
            totalAmount = totalAmount,
            taxAmount = 0.0,
            suggestedCategory = suggestedCategory,
            paymentMethod = "Credit Card",
            lineItems = lineItems,
            rawOcrText = rawText
        )
    }

    private fun guessCategoryFromText(rawText: String, merchant: String): String {
        val lower = (rawText + " " + merchant).lowercase()
        return when {
            lower.contains(Regex("grocery|trader|market|food|produce|supermarket|walmart|target|milk|bread|apple")) -> "Groceries"
            lower.contains(Regex("bistro|cafe|restaurant|pizza|coffee|starbucks|grill|burger|diner")) -> "Food & Dining"
            lower.contains(Regex("gas|shell|chevron|exxon|fuel|transport|transit|parking")) -> "Transportation"
            lower.contains(Regex("hospital|pharmacy|cvs|walgreens|clinic|doctor|health")) -> "Healthcare"
            lower.contains(Regex("electric|utility|power|water|utility|rent|internet")) -> "Housing & Bills"
            lower.contains(Regex("cinema|amc|movie|ticket|theatre|concert|game")) -> "Entertainment"
            else -> "Shopping"
        }
    }
}
