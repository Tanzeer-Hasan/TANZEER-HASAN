package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.viewmodel.ExpenseViewModel
import com.example.ui.theme.IncomeGreen
import java.util.Locale

data class IncomeItem(
    val id: String,
    val name: String,
    var amountText: String
)

data class FixedExpenseItem(
    val name: String,
    val subtitle: String,
    val icon: ImageVector,
    var amountText: String,
    val quickOptions: List<Double>
)

data class CustomFixedExpense(
    val id: String,
    var name: String,
    var amountText: String
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InitialBudgetSetupScreen(
    viewModel: ExpenseViewModel,
    onComplete: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableStateOf(1) } // 1: Income, 2: Fixed Expenses, 3: Review

    // Income inputs
    var personalIncomeText by remember { mutableStateOf("4500") }
    var familyIncomeText by remember { mutableStateOf("2500") }
    val otherIncomes = remember {
        mutableStateListOf(
            IncomeItem(id = "1", name = "Freelance & Side Gig", amountText = "600")
        )
    }

    // Fixed expense items according to user request
    val fixedExpenseList = remember {
        mutableStateListOf(
            FixedExpenseItem(
                name = "Rent or Mortgage",
                subtitle = "Monthly rent or home loan repayment",
                icon = Icons.Default.Home,
                amountText = "1800",
                quickOptions = listOf(1200.0, 1800.0, 2400.0, 3200.0)
            ),
            FixedExpenseItem(
                name = "Home Insurance",
                subtitle = "Homeowners or renters insurance coverage",
                icon = Icons.Default.Shield,
                amountText = "95",
                quickOptions = listOf(45.0, 95.0, 150.0, 220.0)
            ),
            FixedExpenseItem(
                name = "City Property Tax",
                subtitle = "Municipal property or council tax",
                icon = Icons.Default.AccountBalance,
                amountText = "280",
                quickOptions = listOf(0.0, 150.0, 280.0, 450.0)
            ),
            FixedExpenseItem(
                name = "Gas or Electricity Bill",
                subtitle = "Power, gas, heating & utility energy",
                icon = Icons.Default.Bolt,
                amountText = "140",
                quickOptions = listOf(75.0, 140.0, 220.0, 300.0)
            ),
            FixedExpenseItem(
                name = "Car Loan",
                subtitle = "Monthly vehicle financing or lease",
                icon = Icons.Default.DirectionsCar,
                amountText = "350",
                quickOptions = listOf(0.0, 250.0, 350.0, 500.0)
            ),
            FixedExpenseItem(
                name = "Car Insurance",
                subtitle = "Auto policy premium",
                icon = Icons.Default.Shield,
                amountText = "120",
                quickOptions = listOf(70.0, 120.0, 180.0, 260.0)
            ),
            FixedExpenseItem(
                name = "Internet Bill",
                subtitle = "Home broadband & high-speed Wi-Fi",
                icon = Icons.Default.Wifi,
                amountText = "75",
                quickOptions = listOf(45.0, 75.0, 100.0, 140.0)
            ),
            FixedExpenseItem(
                name = "Mobile Bill",
                subtitle = "Cell phone plan & wireless service",
                icon = Icons.Default.Smartphone,
                amountText = "65",
                quickOptions = listOf(35.0, 65.0, 110.0, 160.0)
            ),
            FixedExpenseItem(
                name = "Child Activities",
                subtitle = "Childcare, extracurriculars & tuition",
                icon = Icons.Default.ChildCare,
                amountText = "200",
                quickOptions = listOf(0.0, 150.0, 200.0, 380.0)
            )
        )
    }

    val customFixedExpenses = remember {
        mutableStateListOf<CustomFixedExpense>()
    }

    var createInitialTransactions by remember { mutableStateOf(true) }
    var showSkipDialog by remember { mutableStateOf(false) }

    // Calculated Totals
    val personalIncome = personalIncomeText.toDoubleOrNull() ?: 0.0
    val familyIncome = familyIncomeText.toDoubleOrNull() ?: 0.0
    val otherIncomeTotal = otherIncomes.sumOf { it.amountText.toDoubleOrNull() ?: 0.0 }
    val totalIncome = personalIncome + familyIncome + otherIncomeTotal

    val standardFixedTotal = fixedExpenseList.sumOf { it.amountText.toDoubleOrNull() ?: 0.0 }
    val customFixedTotal = customFixedExpenses.sumOf { it.amountText.toDoubleOrNull() ?: 0.0 }
    val totalFixedExpenses = standardFixedTotal + customFixedTotal

    val discretionarySurplus = (totalIncome - totalFixedExpenses).coerceAtLeast(0.0)
    val fixedExpenseRatio = if (totalIncome > 0) (totalFixedExpenses / totalIncome).coerceIn(0.0, 1.0).toFloat() else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Initial Financial Setup",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = when (currentStep) {
                                1 -> "Step 1 of 3: Income Sources"
                                2 -> "Step 2 of 3: Fixed Expenses"
                                else -> "Step 3 of 3: Review Foundation"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    if (currentStep > 1) {
                        IconButton(
                            onClick = { currentStep -= 1 },
                            modifier = Modifier.testTag("setup_back_button")
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    TextButton(
                        onClick = { showSkipDialog = true },
                        modifier = Modifier.testTag("setup_skip_button")
                    ) {
                        Text("Skip")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = { currentStep -= 1 },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("setup_prev_step_button")
                        ) {
                            Text("Back")
                        }
                    }

                    Button(
                        onClick = {
                            if (currentStep < 3) {
                                currentStep += 1
                            } else {
                                // Final submission
                                val otherPairs = otherIncomes.map { it.name to (it.amountText.toDoubleOrNull() ?: 0.0) }
                                val fixedPairs = fixedExpenseList.map { it.name to (it.amountText.toDoubleOrNull() ?: 0.0) } +
                                        customFixedExpenses.map { it.name to (it.amountText.toDoubleOrNull() ?: 0.0) }

                                viewModel.completeInitialSetup(
                                    personalIncome = personalIncome,
                                    familyIncome = familyIncome,
                                    otherIncomes = otherPairs,
                                    fixedExpenses = fixedPairs,
                                    createInitialTransactions = createInitialTransactions,
                                    onComplete = onComplete
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(if (currentStep > 1) 1.5f else 1f)
                            .height(50.dp)
                            .testTag(if (currentStep == 3) "setup_complete_button" else "setup_next_step_button")
                    ) {
                        if (currentStep < 3) {
                            Text(if (currentStep == 1) "Next: Fixed Expenses" else "Next: Review Summary", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        } else {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save & Get Started", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Step Progress Bar
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Step $currentStep of 3",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${(currentStep * 33.3f).toInt()}% Completed",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { currentStep / 3f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            when (currentStep) {
                1 -> {
                    // STEP 1: HOUSEHOLD INCOME
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Savings,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Household Monthly Income",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Enter your earnings to calculate your total monthly budget.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    shadowElevation = 1.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Total Monthly Income",
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = String.format(Locale.US, "$%,.2f", totalIncome),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = IncomeGreen
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Question 1: Personal Primary Income
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "1. What is your primary monthly income?",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            text = "Your main take-home salary or net paycheck after taxes",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = personalIncomeText,
                                    onValueChange = { personalIncomeText = it },
                                    label = { Text("Your Net Monthly Income") },
                                    prefix = { Text("$ ", fontWeight = FontWeight.Bold) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("setup_personal_income_input")
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "Quick options:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    listOf(3000.0, 4500.0, 6000.0, 8500.0).forEach { amount ->
                                        val isSelected = personalIncomeText == amount.toInt().toString()
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { personalIncomeText = amount.toInt().toString() },
                                            label = { Text(String.format(Locale.US, "$%,d", amount.toInt())) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Question 2: Family Member Income
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Group,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "2. Do you have family member income?",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            text = "Spouse, partner, or contributing family member (or $0 if none)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = familyIncomeText,
                                    onValueChange = { familyIncomeText = it },
                                    label = { Text("Family Member Monthly Income") },
                                    prefix = { Text("$ ", fontWeight = FontWeight.Bold) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("setup_family_income_input")
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "Quick options:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    listOf(0.0, 1500.0, 2500.0, 4000.0, 5500.0).forEach { amount ->
                                        val isSelected = familyIncomeText == amount.toInt().toString()
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { familyIncomeText = amount.toInt().toString() },
                                            label = {
                                                Text(if (amount == 0.0) "None ($0)" else String.format(Locale.US, "$%,d", amount.toInt()))
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Question 3: Other Income Sources
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Payments,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "3. Any other income sources for the total?",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            text = "Freelance, investments, rental income, pensions, or bonuses",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // List of additional income sources
                                otherIncomes.forEachIndexed { index, incomeItem ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = incomeItem.name,
                                            onValueChange = { newName ->
                                                otherIncomes[index] = incomeItem.copy(name = newName)
                                            },
                                            label = { Text("Source Title") },
                                            singleLine = true,
                                            modifier = Modifier.weight(1.2f)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        OutlinedTextField(
                                            value = incomeItem.amountText,
                                            onValueChange = { newAmount ->
                                                otherIncomes[index] = incomeItem.copy(amountText = newAmount)
                                            },
                                            label = { Text("Amount") },
                                            prefix = { Text("$") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            singleLine = true,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = { otherIncomes.removeAt(index) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Remove",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedButton(
                                    onClick = {
                                        val newId = (otherIncomes.size + 1).toString()
                                        otherIncomes.add(IncomeItem(id = newId, name = "Side Income", amountText = "300"))
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("setup_add_income_button")
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add Another Income Source")
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // STEP 2: FIXED EXPENSES
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = "Monthly Fixed Expenses",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "First start with your essential commitments like rent/mortgage, insurance, property tax, utilities, and loans.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    shadowElevation = 1.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Total Fixed Expenses",
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = String.format(Locale.US, "$%,.2f", totalFixedExpenses),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Remaining for Variable & Savings:",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = String.format(Locale.US, "$%,.2f", discretionarySurplus),
                                                fontWeight = FontWeight.Bold,
                                                color = IncomeGreen
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Standard Fixed Expenses Requested By User in Sequence
                    item {
                        Text(
                            text = "Standard Fixed Commitments",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    fixedExpenseList.forEachIndexed { index, fixedItem ->
                        item(key = fixedItem.name) {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primaryContainer)
                                        ) {
                                            Icon(
                                                imageVector = fixedItem.icon,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${index + 1}. ${fixedItem.name}",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = fixedItem.subtitle,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = fixedItem.amountText,
                                        onValueChange = { newText ->
                                            fixedExpenseList[index] = fixedItem.copy(amountText = newText)
                                        },
                                        label = { Text("Monthly Amount ($)") },
                                        prefix = { Text("$ ", fontWeight = FontWeight.Bold) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("setup_fixed_${fixedItem.name.lowercase().replace(" ", "_")}")
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        fixedItem.quickOptions.forEach { optAmount ->
                                            val isSelected = fixedItem.amountText == optAmount.toInt().toString()
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = {
                                                    fixedExpenseList[index] = fixedItem.copy(amountText = optAmount.toInt().toString())
                                                },
                                                label = {
                                                    Text(
                                                        if (optAmount == 0.0) "None ($0)" else String.format(Locale.US, "$%,d", optAmount.toInt()),
                                                        fontSize = 11.sp
                                                    )
                                                },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Any Other Fixed Expenses Section
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Any other fixed expenses to add?",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "Student loan, card loan, gym membership, streaming services, or storage",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Quick presets to add
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    listOf(
                                        "Student Loan" to "250",
                                        "Card Loan" to "150",
                                        "Gym Membership" to "60",
                                        "Streaming Subs" to "45"
                                    ).forEach { (presetName, defaultAmt) ->
                                        OutlinedButton(
                                            onClick = {
                                                val newId = System.currentTimeMillis().toString()
                                                customFixedExpenses.add(CustomFixedExpense(newId, presetName, defaultAmt))
                                            },
                                            shape = RoundedCornerShape(20.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("+ $presetName", fontSize = 12.sp)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                customFixedExpenses.forEachIndexed { cIndex, cItem ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = cItem.name,
                                            onValueChange = { newN ->
                                                customFixedExpenses[cIndex] = cItem.copy(name = newN)
                                            },
                                            label = { Text("Expense Name") },
                                            singleLine = true,
                                            modifier = Modifier.weight(1.3f)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        OutlinedTextField(
                                            value = cItem.amountText,
                                            onValueChange = { newA ->
                                                customFixedExpenses[cIndex] = cItem.copy(amountText = newA)
                                            },
                                            label = { Text("Amount") },
                                            prefix = { Text("$") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            singleLine = true,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = { customFixedExpenses.removeAt(cIndex) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Remove",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        val newId = System.currentTimeMillis().toString()
                                        customFixedExpenses.add(CustomFixedExpense(newId, "Other Bill", "100"))
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("setup_add_custom_fixed_expense_button")
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add Custom Fixed Expense")
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // STEP 3: REVIEW & CONFIRMATION
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(IncomeGreen.copy(alpha = 0.2f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = IncomeGreen,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Your Financial Foundation",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            text = "Monthly overview & budget limits",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Foundation Metric Grid
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text("Total Monthly Income", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(
                                                    String.format(Locale.US, "$%,.2f", totalIncome),
                                                    style = MaterialTheme.typography.titleLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = IncomeGreen
                                                )
                                            }

                                            Column(horizontalAlignment = Alignment.End) {
                                                Text("Total Fixed Expenses", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(
                                                    String.format(Locale.US, "$%,.2f", totalFixedExpenses),
                                                    style = MaterialTheme.typography.titleLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        Text(
                                            text = "Fixed Cost Ratio: ${(fixedExpenseRatio * 100).toInt()}% of Income",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        LinearProgressIndicator(
                                            progress = { fixedExpenseRatio },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .clip(CircleShape),
                                            color = if (fixedExpenseRatio > 0.65f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                            trackColor = MaterialTheme.colorScheme.surface
                                        )

                                        Spacer(modifier = Modifier.height(14.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Discretionary Spending & Savings:",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = String.format(Locale.US, "$%,.2f", discretionarySurplus),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                // Options Checkbox
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { createInitialTransactions = !createInitialTransactions }
                                        .padding(vertical = 4.dp)
                                ) {
                                    Checkbox(
                                        checked = createInitialTransactions,
                                        onCheckedChange = { createInitialTransactions = it }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Populate initial ledger transactions",
                                            fontWeight = FontWeight.SemiBold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "Adds your income and fixed bills into this month's transaction history",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Itemized Review of Incomes
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Monthly Income Breakdown",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = IncomeGreen
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                if (personalIncome > 0) {
                                    ReviewRow(title = "Primary Income", amount = personalIncome, isIncome = true)
                                }
                                if (familyIncome > 0) {
                                    ReviewRow(title = "Family Member Income", amount = familyIncome, isIncome = true)
                                }
                                otherIncomes.filter { (it.amountText.toDoubleOrNull() ?: 0.0) > 0 }.forEach { item ->
                                    ReviewRow(title = item.name, amount = item.amountText.toDoubleOrNull() ?: 0.0, isIncome = true)
                                }
                            }
                        }
                    }

                    // Itemized Review of Fixed Expenses
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Monthly Fixed Expenses Breakdown",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                fixedExpenseList.filter { (it.amountText.toDoubleOrNull() ?: 0.0) > 0 }.forEach { item ->
                                    ReviewRow(title = item.name, amount = item.amountText.toDoubleOrNull() ?: 0.0, isIncome = false)
                                }

                                customFixedExpenses.filter { (it.amountText.toDoubleOrNull() ?: 0.0) > 0 }.forEach { item ->
                                    ReviewRow(title = item.name, amount = item.amountText.toDoubleOrNull() ?: 0.0, isIncome = false)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSkipDialog) {
        AlertDialog(
            onDismissRequest = { showSkipDialog = false },
            title = { Text("Skip Initial Setup?") },
            text = {
                Text("You can still add income and fixed expenses manually later from the Categories & Budgets tab.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSkipDialog = false
                        viewModel.securityManager.setOnboardingCompleted(true)
                        onSkip()
                    }
                ) {
                    Text("Skip for Now", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSkipDialog = false }) {
                    Text("Continue Setup")
                }
            }
        )
    }
}

@Composable
private fun ReviewRow(
    title: String,
    amount: Double,
    isIncome: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = (if (isIncome) "+ " else "- ") + String.format(Locale.US, "$%,.2f", amount),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (isIncome) IncomeGreen else MaterialTheme.colorScheme.onSurface
        )
    }
}
