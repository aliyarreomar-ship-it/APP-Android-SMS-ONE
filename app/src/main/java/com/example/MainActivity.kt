package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Telephony
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.data.sms.SmsContentObserver
import com.example.ui.MainScreen
import com.example.ui.theme.SmsReaderProTheme

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.domain.worker.PendingTransactionWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private var smsContentObserver: SmsContentObserver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        registerSmsObserver()
        schedulePendingReminders()

        setContent {
            SmsReaderProTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        registerSmsObserver()
    }

    override fun onResume() {
        super.onResume()
        registerSmsObserver()
    }

    override fun onStop() {
        super.onStop()
        unregisterSmsObserver()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterSmsObserver()
    }

    private fun registerSmsObserver() {
        if (smsContentObserver != null) return
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
                smsContentObserver = SmsContentObserver(this)
                contentResolver.registerContentObserver(
                    Telephony.Sms.CONTENT_URI,
                    true,
                    smsContentObserver!!
                )
            }
        } catch (e: Exception) {
            // Log or ignore safely
        }
    }

    private fun unregisterSmsObserver() {
        smsContentObserver?.let {
            try {
                contentResolver.unregisterContentObserver(it)
            } catch (e: Exception) {
                // Ignore safely
            }
            smsContentObserver = null
        }
    }

    private fun schedulePendingReminders() {
        val workRequest = PeriodicWorkRequestBuilder<PendingTransactionWorker>(1, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "PendingTransactionReminder",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}


