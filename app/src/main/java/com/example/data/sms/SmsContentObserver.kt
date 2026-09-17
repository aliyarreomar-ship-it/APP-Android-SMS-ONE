package com.example.data.sms

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import com.example.data.events.TransactionEventBus
import com.example.data.local.AppDatabase
import com.example.data.local.entity.SmsRecordEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.parser.SmsParserEngine
import com.example.data.repository.AppRepository
import com.example.notifications.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SmsContentObserver(
    private val context: Context,
    handler: Handler = Handler(Looper.getMainLooper())
) : ContentObserver(handler) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val parserEngine = SmsParserEngine()
    private val mutex = Mutex()

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        // High-speed instant check when SMS database updates
        scope.launch {
            mutex.withLock {
                checkLatestSms()
            }
        }
    }

    private suspend fun checkLatestSms() {
        val database = AppDatabase.getDatabase(context)
        val repository = AppRepository(database.transactionDao(), database.categoryDao(), database.smsRecordDao())

        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE
        )

        try {
            val cursor = context.contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                projection,
                null,
                null,
                "${Telephony.Sms.DATE} DESC LIMIT 5"
            ) ?: return

            cursor.use { c ->
                val addressIdx = c.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIdx = c.getColumnIndex(Telephony.Sms.BODY)
                val dateIdx = c.getColumnIndex(Telephony.Sms.DATE)

                while (c.moveToNext()) {
                    val rawSender = if (addressIdx != -1) c.getString(addressIdx) else null
                    val body = if (bodyIdx != -1) c.getString(bodyIdx) ?: "" else ""
                    val timestamp = if (dateIdx != -1) c.getLong(dateIdx) else System.currentTimeMillis()

                    if (body.isBlank()) continue

                    val sender = when {
                        !rawSender.isNullOrBlank() -> rawSender
                        body.contains("[-EVCPLUS-]", ignoreCase = true) || body.contains("waafi", ignoreCase = true) -> "192"
                        body.contains("[-EDAHAB-]", ignoreCase = true) || body.contains("somtel", ignoreCase = true) -> "898"
                        else -> "192"
                    }

                    // Check if SMS is already registered
                    val existingSms = repository.getSmsRecordByContent(body, timestamp)
                    if (existingSms != null) continue

                    // Parse SMS
                    val result = parserEngine.parse(sender, body, timestamp)

                    val smsId = repository.insertSmsRecord(
                        SmsRecordEntity(
                            sender = sender,
                            body = body,
                            timestamp = timestamp,
                            isProcessed = true,
                            isFinancial = result.isFinancial,
                            linkedTransactionId = null
                        )
                    )

                    if (result.isFinancial && result.provider != null && result.transactionType != null && result.amount != null) {
                        val ref = result.referenceId ?: "REF_${timestamp}_$smsId"

                        val isDup = repository.isDuplicate(
                            provider = result.provider,
                            referenceId = ref,
                            smsBody = body,
                            type = result.transactionType.name,
                            amount = result.amount,
                            timestamp = timestamp
                        )

                        if (!isDup) {
                            val txDate = result.extractedTimestamp ?: timestamp
                            val transaction = TransactionEntity(
                                provider = result.provider,
                                transactionType = result.transactionType,
                                amount = result.amount,
                                currency = result.currency,
                                senderNumber = result.senderNumber,
                                receiverNumber = result.receiverNumber,
                                balance = result.balance,
                                transactionDate = txDate,
                                transactionTime = txDate,
                                referenceId = ref,
                                smsSender = sender,
                                originalSms = body,
                                category = result.suggestedCategory ?: "Other",
                                notes = null,
                                confidence = result.confidence,
                                isVerified = true,
                                isDuplicate = false
                            )

                            val txId = repository.insertTransaction(transaction)
                            val savedTx = repository.getTransactionById(txId)
                            if (savedTx != null) {
                                // 1. Post to Event Bus for instant UI Popup / Splash
                                TransactionEventBus.postTransaction(savedTx)

                                // 2. Show system notification
                                val notificationHelper = NotificationHelper(context)
                                notificationHelper.showTransactionNotification(savedTx)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore permission or cursor exceptions
        }
    }
}
