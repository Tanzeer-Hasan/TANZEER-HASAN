package com.example.ui.screens

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CategoryType
import com.example.data.model.LineItem
import com.example.data.model.PaymentMethod
import com.example.data.model.ScannedReceipt
import com.example.data.model.TransactionType
import com.example.data.viewmodel.ExpenseViewModel
import com.example.ui.components.AddCategoryBottomSheet
import com.example.ui.components.CategoryChip
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionScreen(
    viewModel: ExpenseViewModel,
    transactionId: Long? = null,
    prefilledScannedReceipt: ScannedReceipt? = null,
    prefilledImageUri: String? = null,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val dbCategories by viewModel.allCategories.collectAsStateWithLifecycle()

    val existingTx = remember(transactionId, allTransactions) {
        if (transactionId != null && transactionId > 0) {
            allTransactions.firstOrNull { it.id == transactionId }
        } else null
    }

    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf(false) }

    var selectedMainType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedExpenseSubtype by remember { mutableStateOf(CategoryType.FIXED_EXPENSE) }
    var selectedCategory by remember { mutableStateOf("Rent / Mortgage") }
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.CREDIT_CARD.displayName) }
    var dateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var notes by remember { mutableStateOf("") }
    var receiptImageUri by remember { mutableStateOf<String?>(null) }
    val lineItems = remember { mutableStateListOf<LineItem>() }

    var showDatePicker by remember { mutableStateOf(false) }
    var paymentMenuExpanded by remember { mutableStateOf(false) }
    var showAddCategoryBottomSheet by remember { mutableStateOf(false) }
    var deletingCategoryName by remember { mutableStateOf<String?>(null) }

    // Camera Capture setup
    var tempCameraImageUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraFilePath by remember { mutableStateOf<String?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraFilePath != null) {
            receiptImageUri = tempCameraFilePath
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCameraCapture(
                context = context,
                onUriCreated = { uri, path ->
                    tempCameraImageUri = uri
                    tempCameraFilePath = path
                    takePictureLauncher.launch(uri)
                },
                onError = { err ->
                    coroutineScope.launch { snackbarHostState.showSnackbar("Camera error: $err") }
                }
            )
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Camera permission is required to capture receipts.")
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    val receiptsDir = File(context.filesDir, "receipts")
                    if (!receiptsDir.exists()) receiptsDir.mkdirs()
                    val file = File(receiptsDir, "manual_receipt_${System.currentTimeMillis()}.jpg")
                    val out = file.outputStream()
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, out)
                    out.close()
                    receiptImageUri = file.absolutePath
                }
            } catch (e: Exception) {
                Log.e("AddEditTransaction", "Failed to load photo", e)
            }
        }
    }

    // Initialize or Pre-fill values
    LaunchedEffect(existingTx, prefilledScannedReceipt) {
        if (existingTx != null) {
            title = existingTx.title
            amountText = String.format(Locale.US, "%.2f", existingTx.amount)
            selectedMainType = TransactionType.valueOf(existingTx.type)
            selectedCategory = existingTx.category
            selectedPaymentMethod = existingTx.paymentMethod
            dateMillis = existingTx.dateMillis
            notes = existingTx.notes
            receiptImageUri = existingTx.receiptImageUri

            val items = viewModel.deserializeLineItems(existingTx.lineItemsJson)
            lineItems.clear()
            lineItems.addAll(items)
        } else if (prefilledScannedReceipt != null) {
            title = prefilledScannedReceipt.merchantName.ifBlank { "Receipt Purchase" }
            amountText = if (prefilledScannedReceipt.totalAmount > 0) {
                String.format(Locale.US, "%.2f", prefilledScannedReceipt.totalAmount)
            } else ""
            selectedMainType = TransactionType.EXPENSE
            selectedCategory = prefilledScannedReceipt.suggestedCategory
            selectedPaymentMethod = prefilledScannedReceipt.paymentMethod
            receiptImageUri = prefilledImageUri

            lineItems.clear()
            lineItems.addAll(prefilledScannedReceipt.lineItems)

            if (prefilledScannedReceipt.dateString.isNotBlank()) {
                try {
                    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val d = format.parse(prefilledScannedReceipt.dateString)
                    if (d != null) dateMillis = d.time
                } catch (e: Exception) {
                    // default to current
                }
            }
        }
    }

    val displayedCategories = dbCategories.filter { cat ->
        if (selectedMainType == TransactionType.INCOME) {
            cat.type == "INCOME"
        } else {
            if (selectedExpenseSubtype == CategoryType.FIXED_EXPENSE) {
                cat.type == "FIXED_EXPENSE"
            } else {
                cat.type == "VARIABLE_EXPENSE" || cat.type == "EXPENSE"
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (existingTx != null) "Edit Transaction" else "New Transaction",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("back_button_add_edit")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (existingTx != null) {
                        IconButton(
                            onClick = {
                                viewModel.deleteTransaction(existingTx, onComplete = onNavigateBack)
                            },
                            modifier = Modifier.testTag("delete_transaction_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Type Toggle: Expense vs Income
            item {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SegmentedButton(
                        selected = selectedMainType == TransactionType.EXPENSE,
                        onClick = {
                            selectedMainType = TransactionType.EXPENSE
                            selectedCategory = "Rent / Mortgage"
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        modifier = Modifier.testTag("toggle_expense_type")
                    ) {
                        Text("Expense", fontWeight = FontWeight.Bold)
                    }

                    SegmentedButton(
                        selected = selectedMainType == TransactionType.INCOME,
                        onClick = {
                            selectedMainType = TransactionType.INCOME
                            selectedCategory = "Salary"
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        modifier = Modifier.testTag("toggle_income_type")
                    ) {
                        Text("Income", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Expense Classification Sub-Toggle (Fixed vs Variable)
            if (selectedMainType == TransactionType.EXPENSE) {
                item {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SegmentedButton(
                            selected = selectedExpenseSubtype == CategoryType.FIXED_EXPENSE,
                            onClick = {
                                selectedExpenseSubtype = CategoryType.FIXED_EXPENSE
                                val firstFixed = dbCategories.firstOrNull { it.type == "FIXED_EXPENSE" }?.name ?: "Rent / Mortgage"
                                selectedCategory = firstFixed
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            modifier = Modifier.testTag("toggle_fixed_expense")
                        ) {
                            Text("Fixed Expenses (Rent, Loans, Insurances)", fontSize = 12.sp)
                        }

                        SegmentedButton(
                            selected = selectedExpenseSubtype == CategoryType.VARIABLE_EXPENSE,
                            onClick = {
                                selectedExpenseSubtype = CategoryType.VARIABLE_EXPENSE
                                val firstVar = dbCategories.firstOrNull { it.type == "VARIABLE_EXPENSE" || it.type == "EXPENSE" }?.name ?: "Grocery"
                                selectedCategory = firstVar
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            modifier = Modifier.testTag("toggle_variable_expense")
                        ) {
                            Text("Variable (Grocery, Dining)", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Amount Input
            item {
                Column {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = {
                            amountText = it
                            amountError = false
                        },
                        isError = amountError,
                        label = { Text("Amount ($)") },
                        placeholder = { Text("0.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("amount_input")
                    )

                    if (amountError) {
                        Text(
                            text = "Please enter a valid amount greater than $0.00",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }
                }
            }

            // Title / Merchant
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (selectedMainType == TransactionType.EXPENSE) "Merchant / Payee Name" else "Income Source") },
                    placeholder = { Text("e.g. Landlord, Supermarket, Employer") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("title_input")
                )
            }

            // Category Selector with Deletion & "+ New Category" Button
            item {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Category (${displayedCategories.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(
                            onClick = { showAddCategoryBottomSheet = true },
                            modifier = Modifier.testTag("create_custom_category_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Category")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        displayedCategories.chunked(3).forEach { rowCategories ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                rowCategories.forEach { category ->
                                    CategoryChip(
                                        categoryName = category.name,
                                        isSelected = selectedCategory == category.name,
                                        dbCategories = dbCategories,
                                        onDelete = { deletingCategoryName = category.name },
                                        onClick = { selectedCategory = category.name },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Payment Method & Date
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ExposedDropdownMenuBox(
                        expanded = paymentMenuExpanded,
                        onExpandedChange = { paymentMenuExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedPaymentMethod,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Payment Method") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentMenuExpanded) },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("payment_dropdown")
                        )

                        ExposedDropdownMenu(
                            expanded = paymentMenuExpanded,
                            onDismissRequest = { paymentMenuExpanded = false }
                        ) {
                            PaymentMethod.entries.forEach { method ->
                                DropdownMenuItem(
                                    text = { Text(method.displayName) },
                                    onClick = {
                                        selectedPaymentMethod = method.displayName
                                        paymentMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .testTag("date_picker_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Select Date",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(dateFormat.format(Date(dateMillis)), fontSize = 13.sp)
                    }
                }
            }

            // Notes
            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Tags (Optional)") },
                    placeholder = { Text("Add extra description or memo...") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("notes_input")
                )
            }

            // Receipt Photo Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Receipt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Receipt Photo Attachment",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }

                            if (receiptImageUri != null) {
                                IconButton(
                                    onClick = { receiptImageUri = null },
                                    modifier = Modifier.testTag("remove_receipt_image")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove Photo",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val localPath = receiptImageUri
                        if (!localPath.isNullOrBlank() && File(localPath).exists()) {
                            val bitmap = remember(localPath) {
                                BitmapFactory.decodeFile(localPath)
                            }
                            bitmap?.let { b ->
                                Image(
                                    bitmap = b.asImageBitmap(),
                                    contentDescription = "Receipt Image",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            }
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("camera_capture_button")
                                ) {
                                    Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Camera")
                                }

                                OutlinedButton(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("attach_photo_button")
                                ) {
                                    Icon(imageVector = Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Gallery")
                                }
                            }
                        }
                    }
                }
            }

            // Itemized Line Items
            item {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Itemized Breakdown (${lineItems.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(
                            onClick = { lineItems.add(LineItem("", 0.0)) },
                            modifier = Modifier.testTag("add_line_item_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Item")
                        }
                    }

                    if (lineItems.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            lineItems.forEachIndexed { idx, item ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = item.name,
                                        onValueChange = { newName ->
                                            lineItems[idx] = item.copy(name = newName)
                                        },
                                        placeholder = { Text("Item Name") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(2f)
                                    )

                                    OutlinedTextField(
                                        value = if (item.price > 0) item.price.toString() else "",
                                        onValueChange = { newPrice ->
                                            val p = newPrice.toDoubleOrNull() ?: 0.0
                                            lineItems[idx] = item.copy(price = p)
                                        },
                                        placeholder = { Text("0.00") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    )

                                    IconButton(
                                        onClick = { lineItems.removeAt(idx) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Save Button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
                        if (parsedAmount <= 0.0) {
                            amountError = true
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Please enter a valid amount greater than $0.00")
                            }
                        } else {
                            amountError = false
                            viewModel.saveTransaction(
                                id = existingTx?.id ?: 0L,
                                title = title.ifBlank { selectedCategory },
                                amount = parsedAmount,
                                type = selectedMainType,
                                category = selectedCategory,
                                paymentMethod = selectedPaymentMethod,
                                dateMillis = dateMillis,
                                notes = notes,
                                receiptImageUri = receiptImageUri,
                                lineItems = lineItems,
                                onComplete = { onNavigateBack() }
                            )
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("save_transaction_button")
                ) {
                    Text(
                        text = if (existingTx != null) "Update Transaction" else "Save Transaction",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { dateMillis = it }
                        showDatePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Add Custom Category Bottom Sheet
    if (showAddCategoryBottomSheet) {
        AddCategoryBottomSheet(
            onDismiss = { showAddCategoryBottomSheet = false },
            onCreateCategory = { catName, iconId, hex, type ->
                viewModel.createCustomCategory(catName, iconId, hex, type)
                selectedCategory = catName
            }
        )
    }

    // Delete Category Dialog
    deletingCategoryName?.let { cat ->
        AlertDialog(
            onDismissRequest = { deletingCategoryName = null },
            title = { Text("Delete Category '$cat'?") },
            text = { Text("This will delete category '$cat'. Existing transactions will remain unaffected.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCategoryByName(cat)
                        if (selectedCategory == cat) {
                            selectedCategory = displayedCategories.firstOrNull { it.name != cat }?.name ?: "Other"
                        }
                        deletingCategoryName = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingCategoryName = null }) { Text("Cancel") }
            }
        )
    }
}

private fun launchCameraCapture(
    context: Context,
    onUriCreated: (Uri, String) -> Unit,
    onError: (String) -> Unit
) {
    try {
        val receiptsDir = File(context.filesDir, "receipts")
        if (!receiptsDir.exists()) receiptsDir.mkdirs()

        val photoFile = File(receiptsDir, "camera_receipt_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
        onUriCreated(uri, photoFile.absolutePath)
    } catch (e: Exception) {
        Log.e("CameraCapture", "Failed to create FileProvider Uri", e)
        onError(e.localizedMessage ?: "FileProvider error")
    }
}
