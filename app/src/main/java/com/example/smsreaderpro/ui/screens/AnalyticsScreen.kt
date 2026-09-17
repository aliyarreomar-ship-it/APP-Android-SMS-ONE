package com.example.smsreaderpro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smsreaderpro.data.model.TransactionType
import com.example.smsreaderpro.ui.viewmodel.TransactionViewModel
import java.util.Locale

@Composable
fun AnalyticsScreen(viewModel: TransactionViewModel) {
    val transactions by viewModel.transactions.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()

    val categoryBreakdown = remember(transactions) {
        val expenses = transactions.filter { it.transactionType != TransactionType.INCOME }
        expenses.groupBy { it.category ?: "Other" }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .entries.sortedByDescending { it.value }
    }

    val providerBreakdown = remember(transactions) {
        transactions.groupBy { it.provider }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .entries.sortedByDescending { it.value }
    }

    val totalVolume = totalIncome + totalExpense

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 16.dp)
            .testTag("analytics_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Falanqaynta Dhaqaalaha",
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                color = Color(0xFF0F172A)
            )
            Text(
                text = "Warbixin faahfaahsan oo ku saabsan sida lacagtu kuugu baxday",
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )
        }

        // Cash flow comparison card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.BarChart, contentDescription = null, tint = Color(0xFF059669))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Dakhli vs Kharash", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                        }
                        Text("Wadarta: $${String.format(Locale.ROOT, "%.2f", totalVolume)}", fontSize = 12.sp, color = Color(0xFF64748B))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val incomeRatio = if (totalVolume > 0) (totalIncome / totalVolume).toFloat() else 0.5f

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .weight(incomeRatio.coerceIn(0.05f, 0.95f))
                                .height(12.dp)
                                .clip(RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp))
                                .background(Color(0xFF059669))
                        )
                        Box(
                            modifier = Modifier
                                .weight((1f - incomeRatio).coerceIn(0.05f, 0.95f))
                                .height(12.dp)
                                .clip(RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp))
                                .background(Color(0xFFDC2626))
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFF059669)))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Dakhli: $${String.format(Locale.ROOT, "%.2f", totalIncome)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF059669))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFFDC2626)))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Kharash: $${String.format(Locale.ROOT, "%.2f", totalExpense)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFDC2626))
                        }
                    }
                }
            }
        }

        // Category Breakdown card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.PieChart, contentDescription = null, tint = Color(0xFF0F172A))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kharashyada Qayb Kasta", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (categoryBreakdown.isEmpty()) {
                        Text("Ma jiro wax kharash ah oo la diiwaangeliyay", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    } else {
                        categoryBreakdown.forEach { (cat, amount) ->
                            val pct = if (totalExpense > 0) (amount / totalExpense).toFloat() else 0f
                            Column(modifier = Modifier.padding(vertical = 5.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(cat, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
                                    Text("$${String.format(Locale.ROOT, "%.2f", amount)} (${(pct * 100).toInt()}%)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { pct.coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(7.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = Color(0xFF059669),
                                    trackColor = Color(0xFFF1F5F9)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Provider distribution
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.ShowChart, contentDescription = null, tint = Color(0xFFD97706))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Shirkadaha Lacagta (Providers)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    providerBreakdown.forEach { (provider, amount) ->
                        val pct = if (totalVolume > 0) (amount / totalVolume).toFloat() else 0f
                        Column(modifier = Modifier.padding(vertical = 5.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(provider, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                                Text("$${String.format(Locale.ROOT, "%.2f", amount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { pct.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFFD97706),
                                trackColor = Color(0xFFF1F5F9)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
