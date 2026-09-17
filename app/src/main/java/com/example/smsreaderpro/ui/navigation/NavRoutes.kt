package com.example.smsreaderpro.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "Guudmar", Icons.Default.Home)
    data object Transactions : Screen("transactions", "Xisaabaadka", Icons.Default.ReceiptLong)
    data object Review : Screen("review", "Dib-u-eegis", Icons.Default.CheckCircle)
    data object Analytics : Screen("analytics", "Falanqayn", Icons.Default.Analytics)
    data object Reports : Screen("reports", "Warbixin", Icons.Default.Description)
    data object Scanner : Screen("scanner", "Scanner", Icons.Default.QrCodeScanner)
    data object Settings : Screen("settings", "Habayn", Icons.Default.Settings)

    companion object {
        val bottomNavItems = listOf(
            Dashboard,
            Transactions,
            Review,
            Analytics,
            Scanner
        )
    }
}
