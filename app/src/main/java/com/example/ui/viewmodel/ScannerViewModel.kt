package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ScannedReceipt
import com.example.data.remote.LocalReceiptScanner
import com.example.data.remote.SampleReceiptTemplate
import com.example.data.remote.SampleReceipts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

sealed class ScanState {
    object Idle : ScanState()
    data class Processing(val stepMessage: String) : ScanState()
    data class Success(val receipt: ScannedReceipt, val savedImageUri: String?) : ScanState()
    data class Error(val errorMessage: String) : ScanState()
}

class ScannerViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    private val _currentBitmap = MutableStateFlow<Bitmap?>(null)
    val currentBitmap: StateFlow<Bitmap?> = _currentBitmap.asStateFlow()

    private val _savedImageUri = MutableStateFlow<String?>(null)
    val savedImageUri: StateFlow<String?> = _savedImageUri.asStateFlow()

    fun resetState() {
        _scanState.value = ScanState.Idle
        _currentBitmap.value = null
        _savedImageUri.value = null
    }

    fun processBitmapFromUri(uri: Uri) {
        viewModelScope.launch {
            _scanState.value = ScanState.Processing("Loading receipt photo...")
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (bitmap != null) {
                    _currentBitmap.value = bitmap
                    val savedPath = saveReceiptBitmapLocally(bitmap)
                    _savedImageUri.value = savedPath
                    scanBitmapWithLocalMLKit(bitmap, savedPath)
                } else {
                    _scanState.value = ScanState.Error("Could not load image from photo picker.")
                }
            } catch (e: Exception) {
                _scanState.value = ScanState.Error("Failed to load receipt: ${e.localizedMessage}")
            }
        }
    }

    fun processBitmapDirect(bitmap: Bitmap) {
        viewModelScope.launch {
            _scanState.value = ScanState.Processing("Processing captured receipt...")
            _currentBitmap.value = bitmap
            val savedPath = saveReceiptBitmapLocally(bitmap)
            _savedImageUri.value = savedPath
            scanBitmapWithLocalMLKit(bitmap, savedPath)
        }
    }

    fun processSampleReceipt(template: SampleReceiptTemplate) {
        viewModelScope.launch {
            _scanState.value = ScanState.Processing("Generating sample receipt photo...")
            val bitmap = SampleReceipts.createReceiptBitmap(template)
            _currentBitmap.value = bitmap
            val savedPath = saveReceiptBitmapLocally(bitmap)
            _savedImageUri.value = savedPath

            scanBitmapWithLocalMLKit(bitmap, savedPath)
        }
    }

    private suspend fun scanBitmapWithLocalMLKit(bitmap: Bitmap, savedPath: String?) {
        _scanState.value = ScanState.Processing("Running On-Device ML Kit Text Recognition...")

        val result = LocalReceiptScanner.scanReceiptImageOnDevice(bitmap)
        if (result.isSuccess) {
            _scanState.value = ScanState.Success(result.getOrThrow(), savedPath)
        } else {
            val errStr = result.exceptionOrNull()?.message ?: "Local OCR recognition error"
            _scanState.value = ScanState.Error(errorMessage = errStr)
        }
    }

    private suspend fun saveReceiptBitmapLocally(bitmap: Bitmap): String? = withContext(Dispatchers.IO) {
        try {
            val receiptsDir = File(context.filesDir, "receipts")
            if (!receiptsDir.exists()) receiptsDir.mkdirs()

            val file = File(receiptsDir, "receipt_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            outputStream.flush()
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
