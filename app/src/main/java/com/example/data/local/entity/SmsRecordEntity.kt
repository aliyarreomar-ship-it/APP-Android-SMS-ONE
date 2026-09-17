package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sms_records")
data class SmsRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String,
    val body: String,
    val timestamp: Long,
    val isProcessed: Boolean,
    val isFinancial: Boolean,
    val linkedTransactionId: Long?
)
