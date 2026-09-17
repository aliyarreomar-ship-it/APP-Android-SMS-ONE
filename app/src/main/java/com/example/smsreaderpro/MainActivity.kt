package com.example.smsreaderpro

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.smsreaderpro.ui.components.TransactionDetailSheet
import com.example.smsreaderpro.ui.components.TransactionPopupDialog
import com.example.smsreaderpro.ui.navigation.Screen
import com.example.smsreaderpro.ui.screens.AnalyticsScreen
import com.example.smsreaderpro.ui.screens.DashboardScreen
import com.example.smsreaderpro.ui.screens.ReportsScreen
import com.example.smsreaderpro.ui.screens.ReviewScreen
import com.example.smsreaderpro.ui.screens.ScannerScreen
import com.example.smsreaderpro.ui.screens.SettingsScreen
import com.example.smsreaderpro.ui.screens.TransactionsScreen
import com.example.smsreaderpro.ui.theme.SMSReaderProTheme
import com.example.smsreaderpro.ui.viewmodel.TransactionViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: TransactionViewModel by viewModels {
        val app = application as SmsReaderProApp
        TransactionViewModel.Factory(app.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)
        setContent {
            SMSReaderProTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val txId = intent?.getLongExtra("EXTRA_TRANSACTION_ID", -1L) ?: -1L
        if (txId != -1L) {
            viewModel.openTransactionById(txId)
        }
    }
}

@Composable
fun MainAppScreen(viewModel: TransactionViewModel) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route

    val pendingPopupTx by viewModel.pendingPopupTx.collectAsState()
    val selectedDetailTx by viewModel.selectedDetailTx.collectAsState()
    val toastMsg by viewModel.toastMessage.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val smsGranted = permissions[Manifest.permission.RECEIVE_SMS] == true
        if (smsGranted) {
            Toast.makeText(context, "Fariin Aqriye: Oggolaanshaha SMS waa la siiyay!", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        val neededPermissions = mutableListOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            neededPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        val missing = neededPermissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            permissionLauncher.launch(missing.toTypedArray())
        }
    }

    LaunchedEffect(toastMsg) {
        toastMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_navigation")
            ) {
                Screen.bottomNavItems.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title, fontSize = 11.sp) },
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF059669),
                            selectedTextColor = Color(0xFF059669),
                            indicatorColor = Color(0xFFD1FAE5),
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B)
                        ),
                        modifier = Modifier.testTag("nav_item_${screen.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route
            ) {
                composable(Screen.Dashboard.route) {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToReview = { navController.navigate(Screen.Review.route) },
                        onNavigateToTransactions = { navController.navigate(Screen.Transactions.route) },
                        onNavigateToAnalytics = { navController.navigate(Screen.Analytics.route) }
                    )
                }
                composable(Screen.Transactions.route) {
                    TransactionsScreen(viewModel = viewModel)
                }
                composable(Screen.Review.route) {
                    ReviewScreen(viewModel = viewModel)
                }
                composable(Screen.Analytics.route) {
                    AnalyticsScreen(viewModel = viewModel)
                }
                composable(Screen.Reports.route) {
                    ReportsScreen(viewModel = viewModel)
                }
                composable(Screen.Scanner.route) {
                    ScannerScreen(viewModel = viewModel)
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(viewModel = viewModel)
                }
            }

            // Quick categorization modal dialog
            pendingPopupTx?.let { tx ->
                TransactionPopupDialog(
                    transaction = tx,
                    onSave = { cat, note -> viewModel.savePopup(cat, note) },
                    onDismiss = { viewModel.dismissPopup() }
                )
            }

            // Full detail bottom sheet
            selectedDetailTx?.let { tx ->
                TransactionDetailSheet(
                    transaction = tx,
                    onDismiss = { viewModel.selectDetailTx(null) },
                    onDelete = { viewModel.deleteTransaction(tx.id) }
                )
            }
        }
    }
}
