package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.TransactionType

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val provider: String,
    val transactionType: TransactionType,
    val amount: Double,
    val currency: String = "USD",
    val senderNumber: String?,
    val receiverNumber: String?,
    val balance: Double?,
    val transactionDate: Long, // timestamp
    val transactionTime: Long, // optional separate time representation if needed, or combined with transactionDate
    val referenceId: String?,
    val smsSender: String,
    val originalSms: String,
    val category: String?,
    val notes: String?,
    val confidence: Int, // 0-100
    val isVerified: Boolean,
    val isDuplicate: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
