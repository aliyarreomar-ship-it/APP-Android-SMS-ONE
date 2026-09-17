package com.example.data.parser

import com.example.domain.model.TransactionType

data class ParseResult(
    val isFinancial: Boolean,
    val provider: String? = null,
    val transactionType: TransactionType? = null,
    val amount: Double? = null,
    val currency: String = "USD",
    val senderNumber: String? = null,
    val receiverNumber: String? = null,
    val balance: Double? = null,
    val referenceId: String? = null,
    val confidence: Int = 0, // 0-100
    val suggestedCategory: String? = null,
    val extractedTimestamp: Long? = null
)

interface TransactionParser {
    fun canParse(sender: String, body: String): Boolean
    fun parse(sender: String, body: String, timestamp: Long): ParseResult
}
