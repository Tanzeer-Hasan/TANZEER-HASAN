package com.example

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.fragment.app.FragmentActivity
import com.example.data.model.ScannedReceipt
import com.example.data.viewmodel.ExpenseViewModel
import com.example.ui.screens.AddEditTransactionScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.BudgetsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ReceiptScannerScreen
import com.example.ui.screens.SecurityAuthScreen
import com.example.ui.screens.TransactionsListScreen
import com.example.ui.theme.SpendWiseTheme
import com.example.ui.viewmodel.ScannerViewModel

enum class MainTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    TRANSACTIONS("History", Icons.Default.ListAlt),
    SCAN("AI Scan", Icons.Default.DocumentScanner),
    ANALYTICS("Analytics", Icons.Default.BarChart),
    BUDGETS("Budgets", Icons.Default.Savings)
}

sealed class Screen {
    data class Tab(val mainTab: MainTab) : Screen()
    data class AddEditTransaction(
        val transactionId: Long? = null,
        val prefilledScannedReceipt: ScannedReceipt? = null,
        val prefilledImageUri: String? = null
    ) : Screen()
}

class MainActivity : FragmentActivity() {
    private val expenseViewModel: ExpenseViewModel by viewModels()
    private val scannerViewModel: ScannerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SpendWiseTheme {
                var isUnlocked by remember {
                    mutableStateOf(!expenseViewModel.securityManager.isPinSet())
                }

                if (!isUnlocked) {
                    SecurityAuthScreen(
                        securityManager = expenseViewModel.securityManager,
                        onAuthenticated = { isUnlocked = true }
                    )
                } else {
                    var currentScreen by remember { mutableStateOf<Screen>(Screen.Tab(MainTab.HOME)) }
                    val showBottomBar = currentScreen is Screen.Tab

                    Scaffold(
                        bottomBar = {
                            if (showBottomBar) {
                                val activeTab = (currentScreen as Screen.Tab).mainTab
                                NavigationBar {
                                    MainTab.entries.forEach { tab ->
                                        NavigationBarItem(
                                            selected = activeTab == tab,
                                            onClick = { currentScreen = Screen.Tab(tab) },
                                            icon = { Icon(imageVector = tab.icon, contentDescription = tab.title) },
                                            label = { Text(tab.title) },
                                            modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
                                        )
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        val modifier = Modifier.padding(innerPadding)

                        when (val screen = currentScreen) {
                            is Screen.Tab -> {
                                when (screen.mainTab) {
                                    MainTab.HOME -> {
                                        HomeScreen(
                                            viewModel = expenseViewModel,
                                            onNavigateToAddTransaction = {
                                                currentScreen = Screen.AddEditTransaction()
                                            },
                                            onNavigateToScanReceipt = {
                                                scannerViewModel.resetState()
                                                currentScreen = Screen.Tab(MainTab.SCAN)
                                            },
                                            onNavigateToTransactionDetail = { id ->
                                                currentScreen = Screen.AddEditTransaction(transactionId = id)
                                            },
                                            onNavigateToAllTransactions = {
                                                currentScreen = Screen.Tab(MainTab.TRANSACTIONS)
                                            },
                                            onNavigateToBudgets = {
                                                currentScreen = Screen.Tab(MainTab.BUDGETS)
                                            },
                                            onLockApp = {
                                                isUnlocked = false
                                            },
                                            modifier = modifier
                                        )
                                    }

                                    MainTab.TRANSACTIONS -> {
                                        BackHandler { currentScreen = Screen.Tab(MainTab.HOME) }
                                        TransactionsListScreen(
                                            viewModel = expenseViewModel,
                                            onNavigateToTransactionDetail = { id ->
                                                currentScreen = Screen.AddEditTransaction(transactionId = id)
                                            },
                                            modifier = modifier
                                        )
                                    }

                                    MainTab.SCAN -> {
                                        BackHandler { currentScreen = Screen.Tab(MainTab.HOME) }
                                        ReceiptScannerScreen(
                                            scannerViewModel = scannerViewModel,
                                            onNavigateBack = { currentScreen = Screen.Tab(MainTab.HOME) },
                                            onConfirmScannedReceipt = { scannedReceipt, savedImageUri ->
                                                currentScreen = Screen.AddEditTransaction(
                                                    prefilledScannedReceipt = scannedReceipt,
                                                    prefilledImageUri = savedImageUri
                                                )
                                            },
                                            modifier = modifier
                                        )
                                    }

                                    MainTab.ANALYTICS -> {
                                        BackHandler { currentScreen = Screen.Tab(MainTab.HOME) }
                                        AnalyticsScreen(
                                            viewModel = expenseViewModel,
                                            modifier = modifier
                                        )
                                    }

                                    MainTab.BUDGETS -> {
                                        BackHandler { currentScreen = Screen.Tab(MainTab.HOME) }
                                        BudgetsScreen(
                                            viewModel = expenseViewModel,
                                            modifier = modifier
                                        )
                                    }
                                }
                            }

                            is Screen.AddEditTransaction -> {
                                BackHandler { currentScreen = Screen.Tab(MainTab.HOME) }
                                AddEditTransactionScreen(
                                    viewModel = expenseViewModel,
                                    transactionId = screen.transactionId,
                                    prefilledScannedReceipt = screen.prefilledScannedReceipt,
                                    prefilledImageUri = screen.prefilledImageUri,
                                    onNavigateBack = { currentScreen = Screen.Tab(MainTab.HOME) },
                                    modifier = modifier
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
