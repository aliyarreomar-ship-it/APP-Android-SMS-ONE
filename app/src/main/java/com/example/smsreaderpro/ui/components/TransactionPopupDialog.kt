package com.example.smsreaderpro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import com.example.smsreaderpro.data.model.Transaction
import com.example.smsreaderpro.data.model.TransactionType
import com.example.smsreaderpro.ui.theme.ExpenseRed
import com.example.smsreaderpro.ui.theme.IncomeGreen
import java.util.Locale

private val SOMALI_CATEGORIES = listOf(
    "Bajaaj", "Cunto", "Dukaan", "Kiro", "Koronto", "Biyo",
    "Gas", "TV", "Nadaafad", "Ku hadal Taleefan", "Internet", "Shaah"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionPopupDialog(
    transaction: Transaction,
    onSave: (category: String, note: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCategory by remember {
        mutableStateOf(
            if (transaction.category != null && transaction.category != "Other") transaction.category else "Cunto"
        )
    }
    var isCustom by remember { mutableStateOf(false) }
    var customCategoryText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf(transaction.notes ?: "") }

    val isIncome = transaction.transactionType == TransactionType.INCOME
    val amountColor = if (isIncome) IncomeGreen else ExpenseRed
    val sign = if (isIncome) "+" else "-"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("popup_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Top header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${transaction.provider} • CUSUB",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = Color(0xFF0F172A)
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Amount banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(amountColor.copy(alpha = 0.08f))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = if (isIncome) "XAWILAAD LAGUU SOO DIRAY" else "WAXAAD U DIRTAY LACAG",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = amountColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$sign$${String.format(Locale.ROOT, "%.2f", transaction.amount)}",
                            fontWeight = FontWeight.Black,
                            fontSize = 28.sp,
                            color = amountColor
                        )
                        val counterparty = transaction.senderNumber ?: transaction.receiverNumber
                        if (counterparty != null) {
                            Text(
                                text = counterparty,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E293B)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Maxaan ku diiwaangeliyaa?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SOMALI_CATEGORIES.forEach { cat ->
                        val isSelected = !isCustom && selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(0xFF059669) else Color(0xFFF1F5F9))
                                .clickable {
                                    isCustom = false
                                    selectedCategory = cat
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF334155)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Qoraal dheeraad ah (ikhtiyaari)") },
                    placeholder = { Text("Tusaale: Cuntadii qadada") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("popup_note_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("popup_dismiss_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Ka noqo", color = Color(0xFF475569), fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val cat = if (isCustom && customCategoryText.isNotBlank()) customCategoryText.trim() else selectedCategory
                            onSave(cat, noteText.trim())
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("popup_save_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Keydi Xisaabta", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
