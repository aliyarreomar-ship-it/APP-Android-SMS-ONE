package com.example.ui.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.AppViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SettingsScreen(viewModel: AppViewModel) {
    val context = LocalContext.current

    val permissions = mutableListOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
    }

    val permissionState = rememberMultiplePermissionsState(permissions = permissions)

    // Dialog States
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var showResetAllHistoryDialog by remember { mutableStateOf(false) }
    var showClearSmsDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                Column {
                    Text(
                        "Habaynta & Maamulka",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Version 2.0 Pro • Automatic & Smart Engine",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Overlay Permission
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Popup Permissions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "App-ku wuxuu u baahan yahay fasax uu Popup ku soo saaro marka SMS soo dhaco.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val hasOverlayPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        android.provider.Settings.canDrawOverlays(context)
                    } else true
                    
                    if (!hasOverlayPermission) {
                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    val intent = android.content.Intent(
                                        android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        android.net.Uri.parse("package:${context.packageName}")
                                    )
                                    context.startActivity(intent)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Enable Overlay / Soo bandhig Popup")
                        }
                    } else {
                        Button(
                            onClick = { },
                            enabled = false,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Popup Permission Granted")
                        }
                    }
                }
            }
        }

        // 1. Data Management (Clear History / Clean Slate)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.DeleteSweep,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Maamulka Xogta (Data & History)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Tirtir xisaabaadkii hore ama bilaaw cusub",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Delete All Transactions Button
                    Button(
                        onClick = { showDeleteAllDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tir Dhammaan Xisaabaadka (Delete All Transactions)", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Full Reset / Clean Slate
                    Button(
                        onClick = { showResetAllHistoryDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Nadiifi App-ka (Full Clean Slate)", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Clear SMS Records only
                    OutlinedButton(
                        onClick = { showClearSmsDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Nadiifi Kaydka SMS-yada (Clear SMS Cache)", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        // 2. Permissions Status Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Security,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Oggolaanshaha SMS-ka",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    if (permissionState.allPermissionsGranted) "Dhammaan waa oggol yihiin (Active)" else "Fadlan oggolow SMS-ka",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (permissionState.allPermissionsGranted) Color(0xFF1B873F) else MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        if (permissionState.allPermissionsGranted) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Active",
                                tint = Color(0xFF1B873F),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    if (!permissionState.allPermissionsGranted) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { permissionState.launchMultiplePermissionRequest() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Oggolow Akhriska SMS-ka", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 3. Engine Intelligence Info (Version 2.0)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Tiknoolijiyada Version 2.0",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val features = listOf(
                        "100% Automatic Sync" to "Fariimaha si toos ah ayaa loo qabtaa marka ay kusoo dhacaan.",
                        "Zero Duplicate Engine" to "Shaandhayn labalaabasho la'aan ah (Tixraac, Waqti & Lacag).",
                        "Smart Auto-Categorization" to "Toos u kala soocida Kharashka, Dakhliga, Airtime-ka & Biilasha.",
                        "High-Speed Processing" to "Soo saaris degdeg ah iyadoo aan taleefanka culeys la saarin."
                    )

                    features.forEach { (title, desc) ->
                        Row(modifier = Modifier.padding(vertical = 6.dp)) {
                            Text("• ", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Column {
                                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // 4. Developer Test / Simulation Tools
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Tijaabi Fariin Cusub (Test Simulations)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Ku tijaabi sida app-ku toos ugu qabanayo fariimaha:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val testRef = "EVC" + (1000..9999).random()
                                viewModel.processSyntheticSms(
                                    sender = "EVCPlus",
                                    body = "[+$35.00] Waa laguu soo diray. Waxaana soo diray FAARAX MAXAMED (615887766). Haraagaagu waa $185.00. Ref:$testRef. Tar: 16/09/26 19:10:00"
                                )
                                Toast.makeText(context, "Dakhli EVC Plus ah ayaa la tijaabiyay (+$35)", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1B873F)
                            )
                        ) {
                            Text("+ Dakhli EVC", style = MaterialTheme.typography.labelSmall)
                        }

                        Button(
                            onClick = {
                                val testRef = "EVC" + (1000..9999).random()
                                viewModel.processSyntheticSms(
                                    sender = "EVCPlus",
                                    body = "[-$15.00] Waad u dirtay DUKAANKA CUNTADA (612334455). Haraagaagu waa $170.00. Ref:$testRef. Tar: 16/09/26 19:12:00"
                                )
                                Toast.makeText(context, "Kharash EVC Plus ah ayaa la tijaabiyay (-$15)", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFC62828)
                            )
                        ) {
                            Text("- Kharash EVC", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            val testRef = "EDH" + (1000..9999).random()
                            viewModel.processSyntheticSms(
                                sender = "EDAHAB",
                                body = "Waxaad heshay \$40.00 ka heshay AXMED ROOBLE (659911223). Haraagaagu waa \$210.00. Tarjumaha: $testRef"
                            )
                            Toast.makeText(context, "E-Dahab Dakhli ah ayaa la tijaabiyay (+$40)", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Tijaabi E-Dahab (+ $40)", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Dialog: Delete All Transactions
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = {
                Text("Tir Dhammaan Xisaabaadka", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
            },
            text = {
                Text("Ma hubtaa inaad tirtirto dhammaan xisaabaadka ku jira app-ka? Xisaabaadkii hore dhan waa la tirtirayaa.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAllTransactions {
                            Toast.makeText(context, "Dhammaan xisaabaadkii hore waa la tirtiray", Toast.LENGTH_SHORT).show()
                        }
                        showDeleteAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Haa, Tir Dhammaan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("Ka Noqo")
                }
            }
        )
    }

    // Dialog: Full Clean Slate / Reset All History
    if (showResetAllHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showResetAllHistoryDialog = false },
            icon = {
                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            },
            title = {
                Text("Nadiifi App-ka (Full Reset)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
            },
            text = {
                Text("Tani waxay tirtiri doontaa dhammaan xisaabaadka iyo dhammaan fariimihii hore ee ku kaydsanaa app-ka. App-ku wuxuu noqonayaa mid cusub oo nadiif ah, oo diyaar u ah fariimaha cusub ee hadda soo dhaca.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllHistory {
                            Toast.makeText(context, "App-ka hadda waa la nadiifiyay, diyaar ayuu u yahay fariimaha cusub!", Toast.LENGTH_LONG).show()
                        }
                        showResetAllHistoryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Haa, Nadiifi Dhammaan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetAllHistoryDialog = false }) {
                    Text("Ka Noqo")
                }
            }
        )
    }

    // Dialog: Clear SMS Cache
    if (showClearSmsDialog) {
        AlertDialog(
            onDismissRequest = { showClearSmsDialog = false },
            title = {
                Text("Nadiifi Kaydka SMS-yada", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Ma hubtaa inaad tirtirto kaydka SMS-yada ee la aqriyay? Xisaabaadka lama tirtiri doono.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearSmsCache {
                            Toast.makeText(context, "Kaydka SMS-yada waa la nadiifiyay", Toast.LENGTH_SHORT).show()
                        }
                        showClearSmsDialog = false
                    }
                ) {
                    Text("Haa, Nadiifi")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearSmsDialog = false }) {
                    Text("Ka Noqo")
                }
            }
        )
    }
}
