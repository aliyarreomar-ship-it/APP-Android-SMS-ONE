package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.TransactionEntity
import com.example.domain.model.TransactionType
import java.text.NumberFormat
import java.util.Locale

/**
 * Category Model with Somali labels & icons
 */
data class QuickCategory(
    val name: String,
    val icon: ImageVector? = null
)

/**
 * An authentic, high-contrast modal popup splash dialog that springs into the middle
 * of the screen whenever an EVC Plus, E-Dahab, 192, or 898 SMS transaction is detected.
 * Prompts the user: "Maxaan ku diiwaangeliyaa?" with one-tap suggestions & custom input.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionPopupDialog(
    transaction: TransactionEntity,
    onSave: (category: String, note: String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        TransactionPopupContent(
            transaction = transaction,
            onSave = onSave,
            onDismiss = onDismiss
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionPopupContent(
    transaction: TransactionEntity,
    onSave: (category: String, note: String) -> Unit,
    onDismiss: () -> Unit
) {
    // Exact requested categories by the user
    val userSuggestions = listOf(
        QuickCategory("Bajaaj"),
        QuickCategory("Cunto"),
        QuickCategory("Dukaan"),
        QuickCategory("Kiro"),
        QuickCategory("Koronto"),
        QuickCategory("Biyo"),
        QuickCategory("Gas"),
        QuickCategory("TV"),
        QuickCategory("Nadaafad"),
        QuickCategory("Ku hadal Taleefan"),
        QuickCategory("Internet"),
        QuickCategory("Shaah")
    )

    var selectedCategory by remember {
        mutableStateOf(
            if (transaction.category.isNullOrBlank() || transaction.category == "Other" || transaction.category == "Guud") {
                "Cunto"
            } else {
                transaction.category ?: "Cunto"
            }
        )
    }

    var customCategoryText by remember { mutableStateOf("") }
    var isCustomCategorySelected by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf(transaction.notes ?: "") }

    val isIncome = transaction.transactionType == TransactionType.INCOME
    val isEvc = transaction.provider.contains("EVC", ignoreCase = true) || transaction.provider.contains("Hormuud", ignoreCase = true)

    // Vibrant EVC Plus Gold/Green and E-Dahab Brand Accents
    val evcGold = Color(0xFFEAA000)
    val evcGreen = Color(0xFF0F8A4B)
    val eDahabRed = Color(0xFFD92525)

    val primaryBrandColor = if (isIncome) evcGreen else if (isEvc) Color(0xFFC2410C) else eDahabRed
    val format = NumberFormat.getCurrencyInstance(Locale.US)
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .border(
                        width = 2.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                primaryBrandColor.copy(alpha = 0.8f),
                                primaryBrandColor.copy(alpha = 0.2f)
                            )
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Bar (EVC Plus / E-Dahab Badge & Close Button)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = primaryBrandColor.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(primaryBrandColor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isEvc) "EVC PLUS (192)" else "E-DAHAB (898)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = primaryBrandColor
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Transaction Direction Visual Icon & Flow Title
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        primaryBrandColor.copy(alpha = 0.22f),
                                        primaryBrandColor.copy(alpha = 0.05f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = primaryBrandColor,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isIncome) "XAWILAAD LAGUU SOO DIRAY" else "WAXAAD U DIRTAY LACAG",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = primaryBrandColor
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Huge Amount Display in EVC Style
                    Text(
                        text = "${if (isIncome) "+" else "-"}${format.format(transaction.amount)}",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = primaryBrandColor
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Official EVC/E-Dahab SMS Details Receipt Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val counterparty = transaction.senderNumber ?: transaction.receiverNumber
                            if (!counterparty.isNullOrBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        if (isIncome) "Qofka soo diray:" else "Qofka loo diray:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        counterparty,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            if (transaction.balance != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Haraagaaga:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        format.format(transaction.balance),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            if (!transaction.referenceId.isNullOrBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Tixraaca (Ref ID):",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        transaction.referenceId,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // The Core Prompt: "MAXAAN KU DIIWAANGELIYA?"
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Maxaan ku diiwaangeliyaa?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Dooro mid ka mid ah qaybaha hoose ama qor wax cusub:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Grid of Requested Suggestions (Bajaaj, Cunto, Dukaan, Kiro, Koronto, Biyo, Gas, TV, Nadaafad, Ku hadal Taleefan, Internet, Shaah)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        userSuggestions.forEach { cat ->
                            val isSelected = !isCustomCategorySelected && selectedCategory == cat.name
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    isCustomCategorySelected = false
                                    selectedCategory = cat.name
                                },
                                label = {
                                    Text(
                                        cat.name,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                leadingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = primaryBrandColor,
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = Color.White,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        // "Wax cusub qor (+)" Chip
                        FilterChip(
                            selected = isCustomCategorySelected,
                            onClick = {
                                isCustomCategorySelected = true
                            },
                            label = {
                                Text(
                                    "+ Wax cusub",
                                    fontWeight = if (isCustomCategorySelected) FontWeight.Bold else FontWeight.Medium,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                labelColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Custom Category Input Field (Shown if user clicks "+ Wax cusub")
                    AnimatedVisibility(visible = isCustomCategorySelected) {
                        Column(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                            OutlinedTextField(
                                value = customCategoryText,
                                onValueChange = { customCategoryText = it },
                                label = { Text("Qor magaca qaybta cusub") },
                                placeholder = { Text("Tus: Dhismaha guriga, Maalgashi...") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Optional Note Input
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Faahfaahin / Xusid gaar ah (Optional)") },
                        placeholder = { Text("Tus: Lacagtii heshiiska, shaaha shalay...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Save / Diiwaangeli Button
                    val finalCategory = if (isCustomCategorySelected && customCategoryText.isNotBlank()) {
                        customCategoryText.trim()
                    } else {
                        selectedCategory
                    }

                    Button(
                        onClick = {
                            onSave(finalCategory, noteText.trim())
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryBrandColor,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Diiwaangeli ($finalCategory)",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    TextButton(onClick = onDismiss) {
                        Text(
                            "Dib u dhig (Later)",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
}
