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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import com.example.smsreaderpro.ui.viewmodel.TransactionViewModel
import kotlin.random.Random

@Composable
fun ScannerScreen(viewModel: TransactionViewModel) {
    var rawText by remember { mutableStateOf("") }
    var senderField by remember { mutableStateOf("EVCPlus") }

    val sampleMessages = listOf(
        "EVC Income" to "[+$45.00] Waa laguu soo diray. Waxaana soo diray MARYAN CALI (615998877). Haraagaagu waa $230.00. Ref:EVC" + Random.nextInt(1000, 9999) + ". Tar: 16/09/26 14:10:00",
        "EVC Expense" to "[-$8.00] Waad u dirtay BAJAAJ WADAJIR (618223344). Haraagaagu waa $222.00. Ref:EVC" + Random.nextInt(1000, 9999) + ". Tar: 16/09/26 15:20:00",
        "E-Dahab Income" to "Waxaad heshay $60.00 ka heshay XASAN NOOR (651122334). Haraagaagu waa $282.00. Tarjumaha: EDH" + Random.nextInt(1000, 9999),
        "Jeeb Transfer" to "You have received $30.00 from PremierBank to your Jeeb Account. Balance: $312.00. TxId: JB" + Random.nextInt(1000, 9999)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 16.dp)
            .testTag("scanner_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "SMS Scanner & Tijaabo",
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                color = Color(0xFF0F172A)
            )
            Text(
                text = "Dheji fariin SMS ah si toos ah loogu rogo xisaab diiwaangashan",
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )
        }

        // Scanner input box
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Gali Qoraalka Fariinta (SMS Body)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = rawText,
                        onValueChange = { rawText = it },
                        placeholder = { Text("Tusaale: [+$35.00] Waa laguu soo diray...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .testTag("scanner_raw_input"),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { rawText = "" },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Nadiifi", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                if (rawText.isNotBlank()) {
                                    viewModel.processSyntheticSms(senderField, rawText.trim(), triggerPopup = true)
                                } else {
                                    viewModel.showToast("Fadlan qoraal gali")
                                }
                            },
                            modifier = Modifier
                                .weight(2f)
                                .testTag("scanner_parse_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Qabo & Diiwaangeli", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Quick sample messages
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tusaalooyin Degdeg Ah (Click to test)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF0F172A)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    sampleMessages.forEach { (label, sample) ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF8FAFC))
                                .clickable {
                                    rawText = sample
                                    senderField = if (label.contains("E-Dahab")) "EDAHAB" else if (label.contains("Jeeb")) "Jeeb" else "EVCPlus"
                                }
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF059669))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(sample, fontSize = 11.sp, color = Color(0xFF64748B), maxLines = 2)
                            }
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
