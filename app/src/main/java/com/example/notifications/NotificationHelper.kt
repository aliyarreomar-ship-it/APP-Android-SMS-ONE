package com.example.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.entity.TransactionEntity
import com.example.domain.model.TransactionType
import java.text.NumberFormat
import java.util.Locale

class NotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createChannel()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Transactions",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new mobile money transactions"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showTransactionNotification(transaction: TransactionEntity) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("transaction_id", transaction.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            transaction.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val format = NumberFormat.getCurrencyInstance(Locale.US)
        val amountStr = format.format(transaction.amount)
        
        val title = when (transaction.transactionType) {
            TransactionType.INCOME -> "Money Received"
            TransactionType.EXPENSE -> "Money Sent"
            else -> "New Transaction"
        }
        
        val prefix = if (transaction.transactionType == TransactionType.INCOME) "+" else if (transaction.transactionType == TransactionType.EXPENSE) "-" else ""

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText("$prefix$amountStr via ${transaction.provider}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(transaction.id.toInt(), builder.build())
    }

    companion object {
        private const val CHANNEL_ID = "transaction_channel"
    }
}
