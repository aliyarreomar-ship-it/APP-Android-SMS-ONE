package com.example.data.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
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

class SmsReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val parserEngine = SmsParserEngine()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val rawSender = messages[0].displayOriginatingAddress ?: messages[0].originatingAddress
        
        val bodyBuilder = StringBuilder()
        for (message in messages) {
            bodyBuilder.append(message.displayMessageBody)
        }
        val body = bodyBuilder.toString()
        val timestamp = messages[0].timestampMillis

        val sender = when {
            !rawSender.isNullOrBlank() -> rawSender
            body.contains("[-EVCPLUS-]", ignoreCase = true) || body.contains("waafi", ignoreCase = true) -> "192"
            body.contains("[-EDAHAB-]", ignoreCase = true) || body.contains("somtel", ignoreCase = true) -> "898"
            else -> "192"
        }

        val pendingResult = goAsync()
        scope.launch {
            try {
                processSms(context, sender, body, timestamp)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun processSms(context: Context, sender: String, body: String, timestamp: Long) {
        val database = AppDatabase.getDatabase(context)
        val repository = AppRepository(database.transactionDao(), database.categoryDao(), database.smsRecordDao())
        
        // 1. Check duplicate SMS
        val existingSms = repository.getSmsRecordByContent(body, timestamp)
        if (existingSms != null) return

        // 2. Parse SMS
        val result = parserEngine.parse(sender, body, timestamp)

        // 3. Save SMS Record
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

        // 4. Save Transaction if financial
        if (result.isFinancial && result.provider != null && result.transactionType != null && result.amount != null) {
            val ref = result.referenceId ?: "REF_${timestamp}_$smsId"
            
            // Advanced Duplicate Prevention: Zero Mistakes
            val isDup = repository.isDuplicate(
                provider = result.provider,
                referenceId = ref,
                smsBody = body,
                type = result.transactionType.name,
                amount = result.amount,
                timestamp = timestamp
            )
            if (isDup) return

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
                isVerified = false,
                isDuplicate = false
            )
            
            val txId = repository.insertTransaction(transaction)
            
            val savedTx = repository.getTransactionById(txId)
            if (savedTx != null) {
                // Post to instant UI event bus
                TransactionEventBus.postTransaction(savedTx)

                // Show notification
                val notificationHelper = NotificationHelper(context)
                notificationHelper.showTransactionNotification(savedTx)
                
                // Launch Overlay Popup
                val intent = Intent(context, com.example.ui.overlay.TransactionOverlayService::class.java).apply {
                    putExtra("transactionId", txId)
                    putExtra("amount", result.currency + result.amount.toString())
                    putExtra("provider", result.provider)
                    putExtra("category", result.suggestedCategory ?: "Other")
                    putExtra("type", result.transactionType.name)
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            }
        }
    }
}
