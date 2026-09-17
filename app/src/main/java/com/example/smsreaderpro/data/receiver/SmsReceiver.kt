package com.example.smsreaderpro.data.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import androidx.core.app.NotificationCompat
import com.example.smsreaderpro.MainActivity
import com.example.smsreaderpro.SmsReaderProApp
import com.example.smsreaderpro.data.model.Transaction
import com.example.smsreaderpro.data.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            if (messages.isNullOrEmpty()) return

            val app = context.applicationContext as? SmsReaderProApp ?: return
            val repository = app.repository

            val fullBody = StringBuilder()
            var sender = ""

            for (sms in messages) {
                sender = sms.displayOriginatingAddress ?: ""
                fullBody.append(sms.displayMessageBody ?: "")
            }

            val body = fullBody.toString()

            CoroutineScope(Dispatchers.IO).launch {
                val tx = repository.processIncomingSms(sender, body, isVerified = false)
                if (tx != null) {
                    showTransactionAlert(context, tx)
                }
            }
        }
    }

    private fun showTransactionAlert(context: Context, tx: Transaction) {
        val channelId = "fariin_aqriye_alerts"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Fariimaha Lacagta",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Ogeysiisyada fariimaha cusub ee EVC Plus, E-Dahab, iwm."
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("EXTRA_TRANSACTION_ID", tx.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            tx.id.toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val isIncome = tx.transactionType == TransactionType.INCOME
        val party = tx.senderNumber ?: tx.receiverNumber ?: ""
        val title = "Fariin Cusub: ${tx.provider}"
        val content = "${if (isIncome) "+$" else "-$"}${String.format(Locale.ROOT, "%.2f", tx.amount)} $party • Taabo si aad u diiwaangeliso"

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(tx.id.toInt(), notification)
    }
}
