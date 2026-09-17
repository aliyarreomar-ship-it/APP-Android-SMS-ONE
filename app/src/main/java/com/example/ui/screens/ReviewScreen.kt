package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.TransactionEntity
import com.example.domain.model.TransactionType
import com.example.ui.AppViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ReviewScreen(viewModel: AppViewModel) {
    val unverifiedTransactions by viewModel.unverifiedTransactions.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Needs Review", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        
        if (unverifiedTransactions.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("All caught up!", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(top = 16.dp)) {
                items(unverifiedTransactions) { tx ->
                    ReviewItem(tx, 
                        onVerify = { viewModel.verifyTransaction(tx) },
                        onDiscard = { viewModel.deleteTransaction(tx) }
                    )
                }
            }
        }
    }
}

@Composable
fun ReviewItem(transaction: TransactionEntity, onVerify: () -> Unit, onDiscard: () -> Unit) {
    val format = NumberFormat.getCurrencyInstance(Locale.US)
    val isIncome = transaction.transactionType == TransactionType.INCOME
    val amountColor = if (isIncome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    val prefix = if (isIncome) "+" else if (transaction.transactionType == TransactionType.EXPENSE) "-" else ""

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("NEW TRANSACTION", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(transaction.provider, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("$prefix${format.format(transaction.amount)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = amountColor)
            }
            Spacer(modifier = Modifier.height(4.dp))
            val desc = if (isIncome) "Money received from ${transaction.senderNumber}" else "Money sent to ${transaction.receiverNumber}"
            Text(desc, style = MaterialTheme.typography.bodyMedium)
            transaction.balance?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Balance: ${format.format(it)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(onClick = onDiscard, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)) {
                    Text("DISCARD")
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onVerify) {
                    Text("SAVE")
                }
            }
        }
    }
}
