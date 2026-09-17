package com.example.data.sms

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import com.example.data.local.AppDatabase
import com.example.data.local.entity.SmsRecordEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.parser.SmsParserEngine
import com.example.data.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SmsInboxScanner(private val context: Context) {
    private val parserEngine = SmsParserEngine()

    data class ScanResult(
        val totalScanned: Int,
        val financialFound: Int,
        val newImported: Int,
        val errorMessage: String? = null
    )

    suspend fun scanInbox(limit: Int = 1000): ScanResult = withContext(Dispatchers.IO) {
        val database = AppDatabase.getDatabase(context)
        val repository = AppRepository(database.transactionDao(), database.categoryDao(), database.smsRecordDao())

        var totalScanned = 0
        var financialFound = 0
        var newImported = 0

        val uri: Uri = Telephony.Sms.Inbox.CONTENT_URI
        val projection = arrayOf(
            Telephony.Sms.Inbox._ID,
            Telephony.Sms.Inbox.ADDRESS,
            Telephony.Sms.Inbox.BODY,
            Telephony.Sms.Inbox.DATE
        )

        try {
            val cursor = context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                "${Telephony.Sms.Inbox.DATE} DESC"
            )

            if (cursor == null) {
                return@withContext ScanResult(0, 0, 0, "Could not access SMS inbox. Please verify permissions.")
            }

            cursor.use { c ->
                val addressIdx = c.getColumnIndex(Telephony.Sms.Inbox.ADDRESS)
                val bodyIdx = c.getColumnIndex(Telephony.Sms.Inbox.BODY)
                val dateIdx = c.getColumnIndex(Telephony.Sms.Inbox.DATE)

                while (c.moveToNext() && totalScanned < limit) {
                    val rawSender = if (addressIdx != -1) c.getString(addressIdx) else null
                    val body = if (bodyIdx != -1) c.getString(bodyIdx) ?: "" else ""
                    val timestamp = if (dateIdx != -1) c.getLong(dateIdx) else System.currentTimeMillis()

                    if (body.isBlank()) continue
                    totalScanned++

                    val sender = when {
                        !rawSender.isNullOrBlank() && rawSender != "Unknown" -> rawSender
                        body.contains("[-EVCPLUS-]", ignoreCase = true) || body.contains("waafi", ignoreCase = true) -> "192"
                        body.contains("[-EDAHAB-]", ignoreCase = true) || body.contains("somtel", ignoreCase = true) -> "898"
                        else -> "192"
                    }

                    // Check if already in database by body and timestamp
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
                        financialFound++
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
                                isVerified = false,
                                isDuplicate = false
                            )
                            repository.insertTransaction(transaction)
                            newImported++
                        }
                    }
                }
            }
        } catch (e: SecurityException) {
            return@withContext ScanResult(totalScanned, financialFound, newImported, "SMS permission not granted.")
        } catch (e: Exception) {
            return@withContext ScanResult(totalScanned, financialFound, newImported, "Scan error: ${e.localizedMessage}")
        }

        ScanResult(totalScanned, financialFound, newImported)
    }
}
