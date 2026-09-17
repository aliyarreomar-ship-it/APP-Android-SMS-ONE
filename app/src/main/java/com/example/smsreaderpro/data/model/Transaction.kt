package com.example.smsreaderpro.data.model

data class Transaction(
    val id: Long = 0,
    val amount: Double,
    val senderNumber: String? = null,
    val receiverNumber: String? = null,
    val transactionDate: Long = System.currentTimeMillis(),
    val rawSms: String,
    val originalSms: String,
    val transactionType: TransactionType,
    val category: String? = null,
    val notes: String? = null,
    val referenceId: String? = null,
    val balance: Double? = null,
    val provider: String,
    val isVerified: Boolean = false
)
