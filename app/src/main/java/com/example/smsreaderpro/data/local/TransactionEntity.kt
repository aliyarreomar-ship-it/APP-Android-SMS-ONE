package com.example.smsreaderpro.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.smsreaderpro.data.model.Transaction
import com.example.smsreaderpro.data.model.TransactionType

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val senderNumber: String?,
    val receiverNumber: String?,
    val transactionDate: Long,
    val rawSms: String,
    val originalSms: String,
    val transactionType: String,
    val category: String?,
    val notes: String?,
    val referenceId: String?,
    val balance: Double?,
    val provider: String,
    val isVerified: Boolean
) {
    fun toDomain(): Transaction {
        val type = try {
            TransactionType.valueOf(transactionType)
        } catch (_: Exception) {
            TransactionType.EXPENSE
        }
        return Transaction(
            id = id,
            amount = amount,
            senderNumber = senderNumber,
            receiverNumber = receiverNumber,
            transactionDate = transactionDate,
            rawSms = rawSms,
            originalSms = originalSms,
            transactionType = type,
            category = category,
            notes = notes,
            referenceId = referenceId,
            balance = balance,
            provider = provider,
            isVerified = isVerified
        )
    }

    companion object {
        fun fromDomain(transaction: Transaction): TransactionEntity {
            return TransactionEntity(
                id = transaction.id,
                amount = transaction.amount,
                senderNumber = transaction.senderNumber,
                receiverNumber = transaction.receiverNumber,
                transactionDate = transaction.transactionDate,
                rawSms = transaction.rawSms,
                originalSms = transaction.originalSms,
                transactionType = transaction.transactionType.name,
                category = transaction.category,
                notes = transaction.notes,
                referenceId = transaction.referenceId,
                balance = transaction.balance,
                provider = transaction.provider,
                isVerified = transaction.isVerified
            )
        }
    }
}
