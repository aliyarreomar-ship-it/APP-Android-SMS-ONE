package com.example.smsreaderpro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smsreaderpro.data.model.TransactionType
import com.example.smsreaderpro.ui.components.TransactionCard
import com.example.smsreaderpro.ui.viewmodel.TransactionViewModel
import java.util.Locale

@Composable
fun TransactionsScreen(viewModel: TransactionViewModel) {
    val transactions by viewModel.transactions.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedProvider by remember { mutableStateOf("ALL") }
    var selectedType by remember { mutableStateOf("ALL") }

    val filtered = remember(transactions, searchQuery, selectedProvider, selectedType) {
        transactions.filter { tx ->
            val matchesProvider = selectedProvider == "ALL" || tx.provider.equals(selectedProvider, ignoreCase = true)
            val matchesType = when (selectedType) {
                "INCOME" -> tx.transactionType == TransactionType.INCOME
                "EXPENSE" -> tx.transactionType != TransactionType.INCOME
                else -> true
            }
            val matchesSearch = if (searchQuery.isBlank()) true else {
                val q = searchQuery.lowercase(Locale.ROOT)
                (tx.senderNumber?.lowercase(Locale.ROOT)?.contains(q) == true) ||
                        (tx.receiverNumber?.lowercase(Locale.ROOT)?.contains(q) == true) ||
                        (tx.referenceId?.lowercase(Locale.ROOT)?.contains(q) == true) ||
                        (tx.category?.lowercase(Locale.ROOT)?.contains(q) == true) ||
                        (tx.notes?.lowercase(Locale.ROOT)?.contains(q) == true) ||
                        tx.amount.toString().contains(q)
            }
            matchesProvider && matchesType && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 16.dp)
            .testTag("transactions_screen")
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Dhammaan Xisaabaadka",
            fontWeight = FontWeight.Black,
            fontSize = 24.sp,
            color = Color(0xFF0F172A)
        )
        Text(
            text = "Waxaa la helay ${filtered.size} xisaab",
            fontSize = 12.sp,
            color = Color(0xFF64748B)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Raadi magac, lambar, ref ama qoraal...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color(0xFF64748B))
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("transactions_search_input"),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Providers chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            val providers = listOf("ALL" to "Dhammaan", "EVCPlus" to "EVC Plus", "EDAHAB" to "E-Dahab", "Jeeb" to "Jeeb")
            items(providers) { (key, label) ->
                val isSelected = selectedProvider == key
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) Color(0xFF059669) else Color(0xFFE2E8F0))
                        .clickable { selectedProvider = key }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Color(0xFF334155)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Type filter chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            val types = listOf("ALL" to "Dhammaan Noocyada", "INCOME" to "+ Dakhli Keliya", "EXPENSE" to "- Kharash Keliya")
            items(types) { (key, label) ->
                val isSelected = selectedType == key
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) Color(0xFF0F172A) else Color.White)
                        .clickable { selectedType = key }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else Color(0xFF64748B)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isNotEmpty()) "Wax natiijo ah lama helin raadintaada" else "Ma jiraan xisaabaad ku jira qeybtaan",
                    color = Color(0xFF94A3B8),
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered, key = { it.id }) { tx ->
                    TransactionCard(
                        transaction = tx,
                        onClick = { viewModel.selectDetailTx(tx) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
