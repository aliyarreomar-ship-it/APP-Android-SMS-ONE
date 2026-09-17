package com.example.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.TransactionPopupDialog
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.ReviewScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.screens.TransactionDetailScreen

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Dashboard : Screen("dashboard", "Home", Icons.Filled.Home)
    object Transactions : Screen("transactions", "Transactions", Icons.Filled.List)
    object Analytics : Screen("analytics", "Analytics", Icons.Filled.Analytics)
    object Reports : Screen("reports", "Reports", Icons.Filled.CheckCircle)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
}

@Composable
fun MainScreen(viewModel: AppViewModel = viewModel()) {
    val navController = rememberNavController()
    val pendingTransaction by viewModel.pendingReviewTransaction.collectAsStateWithLifecycle()

    // Interactive Transaction Popup / Splash when SMS is received
    pendingTransaction?.let { tx ->
        TransactionPopupDialog(
            transaction = tx,
            onSave = { category, note ->
                viewModel.saveReviewedTransaction(tx, category, note)
            },
            onDismiss = {
                viewModel.dismissPendingReview()
            }
        )
    }
    
    val items = listOf(
        Screen.Dashboard,
        Screen.Transactions,
        Screen.Analytics,
        Screen.Reports,
        Screen.Settings
    )
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.Dashboard.route, Modifier.padding(innerPadding)) {
            composable(Screen.Dashboard.route) { DashboardScreen(viewModel, navController) }
            composable(Screen.Transactions.route) { TransactionsScreen(viewModel, navController) }
            composable("review") { ReviewScreen(viewModel) }
            composable(Screen.Analytics.route) { AnalyticsScreen(viewModel) }
            composable(Screen.Reports.route) { ReportsScreen(viewModel) }
            composable(Screen.Settings.route) { SettingsScreen(viewModel) }
            composable("transaction_detail/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")?.toLongOrNull()
                id?.let {
                    TransactionDetailScreen(viewModel, it, navController)
                }
            }
        }
    }
}
