package com.example.data.remote

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.data.model.LineItem
import com.example.data.model.ScannedReceipt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

object GeminiReceiptScanner {
    private const val TAG = "GeminiReceiptScanner"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun scanReceiptImage(bitmap: Bitmap, apiKey: String): Result<ScannedReceipt> = withContext(Dispatchers.IO) {
        try {
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext Result.failure(
                    IllegalArgumentException("Gemini API key is not configured in Secrets panel.")
                )
            }

            // Scale down bitmap if very large to optimize upload speed
            val scaledBitmap = scaleBitmapIfNeeded(bitmap, 1200)
            val base64Image = bitmapToBase64(scaledBitmap)

            val promptText = """
                Analyze this receipt image and extract structured financial details into JSON format.
                Return ONLY a single valid JSON object with these exact keys:
                - "merchantName": (string, e.g. "Trader Joe's" or "Target")
                - "dateString": (string YYYY-MM-DD, e.g. "2026-10-06" or empty string if unreadable)
                - "totalAmount": (number, total price paid e.g. 45.99)
                - "taxAmount": (number, tax paid e.g. 3.50 or 0)
                - "suggestedCategory": (string, MUST be one of: "Groceries", "Food & Dining", "Shopping", "Housing & Bills", "Transportation", "Entertainment", "Healthcare", "Utilities", "Other Expense")
                - "paymentMethod": (string, e.g. "Credit Card", "Debit Card", "Cash", "Apple Pay", "Digital Wallet")
                - "lineItems": (array of objects with "name" [string] and "price" [number])
                - "rawOcrText": (string summary of text read)
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            put(JSONObject().apply { put("text", promptText) })
                            put(JSONObject().apply {
                                put("inlineData", JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", base64Image)
                                })
                            })
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)

                val generationConfig = JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.1)
                }
                put("generationConfig", generationConfig)
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val url = "$BASE_URL?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                Log.e(TAG, "Gemini API error ${response.code}: $errBody")
                return@withContext Result.failure(Exception("AI Scan failed (${response.code}): $errBody"))
            }

            val responseBodyString = response.body?.string() ?: ""
            val jsonResponse = JSONObject(responseBodyString)
            val candidates = jsonResponse.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val firstPart = parts?.optJSONObject(0)
            val textOutput = firstPart?.optString("text") ?: ""

            if (textOutput.isBlank()) {
                return@withContext Result.failure(Exception("AI returned empty receipt details."))
            }

            val parsedJson = JSONObject(textOutput)
            val merchant = parsedJson.optString("merchantName", "Unspecified Store")
            val dateStr = parsedJson.optString("dateString", "")
            val total = parsedJson.optDouble("totalAmount", 0.0)
            val tax = parsedJson.optDouble("taxAmount", 0.0)
            val category = parsedJson.optString("suggestedCategory", "Groceries")
            val payMethod = parsedJson.optString("paymentMethod", "Credit Card")
            val rawText = parsedJson.optString("rawOcrText", "")

            val lineItemsList = mutableListOf<LineItem>()
            val itemsArray = parsedJson.optJSONArray("lineItems")
            if (itemsArray != null) {
                for (i in 0 until itemsArray.length()) {
                    val itemObj = itemsArray.optJSONObject(i)
                    if (itemObj != null) {
                        val itemName = itemObj.optString("name", "Item ${i + 1}")
                        val itemPrice = itemObj.optDouble("price", 0.0)
                        lineItemsList.add(LineItem(itemName, itemPrice))
                    }
                }
            }

            val scannedReceipt = ScannedReceipt(
                merchantName = merchant,
                dateString = dateStr,
                totalAmount = total,
                taxAmount = tax,
                suggestedCategory = category,
                paymentMethod = payMethod,
                lineItems = lineItemsList,
                rawOcrText = rawText
            )

            Result.success(scannedReceipt)
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning receipt", e)
            Result.failure(e)
        }
    }

    private fun scaleBitmapIfNeeded(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDimension && height <= maxDimension) return bitmap

        val aspectRatio = width.toFloat() / height.toFloat()
        val (newWidth, newHeight) = if (width > height) {
            maxDimension to (maxDimension / aspectRatio).toInt()
        } else {
            (maxDimension * aspectRatio).toInt() to maxDimension
        }
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }
}
