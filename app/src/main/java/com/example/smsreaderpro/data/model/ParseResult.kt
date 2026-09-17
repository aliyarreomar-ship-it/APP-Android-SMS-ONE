package com.example.smsreaderpro.data.model

data class ParseResult(
    val amount: Double,
    val type: TransactionType,
    val sender: String? = null,
    val receiver: String? = null,
    val balance: Double? = null,
    val reference: String? = null,
    val provider: String,
    val category: String? = null,
    val confidenceScore: Float = 0.95f,
    val timestamp: Long = System.currentTimeMillis()
)
