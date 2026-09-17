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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smsreaderpro.data.model.TransactionType
import com.example.smsreaderpro.ui.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportsScreen(viewModel: TransactionViewModel) {
    val transactions by viewModel.transactions.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val netBalance by viewModel.netBalance.collectAsState()

    val reportDate = remember { SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date()) }

    fun generateCsvContent(): String {
        val sb = StringBuilder()
        sb.append("ID,Date,Provider,Type,Amount,Counterparty,Category,Reference,Balance\n")
        transactions.forEach { tx ->
            val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ROOT).format(Date(tx.transactionDate))
            val party = (tx.senderNumber ?: tx.receiverNumber ?: "").replace(",", " ")
            val cat = (tx.category ?: "").replace(",", " ")
            val ref = tx.referenceId ?: ""
            val bal = tx.balance?.toString() ?: ""
            sb.append("${tx.id},\"$date\",\"${tx.provider}\",\"${tx.transactionType.name}\",${tx.amount},\"$party\",\"$cat\",\"$ref\",\"$bal\"\n")
        }
        return sb.toString()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 16.dp)
            .testTag("reports_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Warbixinnada Lacagta",
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                color = Color(0xFF0F172A)
            )
            Text(
                text = "Soo deji ama daabaco warbixinta dhaqdhaqaaqaaga mobile money",
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )
        }

        // Summary statement sheet preview
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("SMS READER PRO", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFF0F172A))
                            Text("Bayaanka Dhaqdhaqaaqa Xisaabaadka", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                        Text(reportDate, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF8FAFC))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Dakhliga Guud", fontSize = 11.sp, color = Color(0xFF64748B))
                                Text("+$${String.format(Locale.ROOT, "%.2f", totalIncome)}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF059669))
                            }
                            Column {
                                Text("Kharashka Guud", fontSize = 11.sp, color = Color(0xFF64748B))
                                Text("-$${String.format(Locale.ROOT, "%.2f", totalExpense)}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFFDC2626))
                            }
                            Column {
                                Text("Farqiga (Net)", fontSize = 11.sp, color = Color(0xFF64748B))
                                Text("${if (netBalance >= 0) "+" else "-"}$${String.format(Locale.ROOT, "%.2f", Math.abs(netBalance))}", fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color(0xFF0F172A))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Wadarta xisaabaadka la qabtay: ${transactions.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF475569)
                    )
                }
            }
        }

        // Export Actions
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Dhoofinta Xogta (Export Options)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val csv = generateCsvContent()
                            viewModel.showToast("Warbixinta CSV (${transactions.size} saf) waa la diyaariyay!")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_csv_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Soo deji CSV Spreadsheet", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            viewModel.showToast("Warbixinta qoraal ahaan waa la koobiyeeyay!")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color(0xFF0F172A))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("La wadaag Warbixinta Guud", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
