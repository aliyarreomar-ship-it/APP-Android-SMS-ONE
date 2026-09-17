package com.example.smsreaderpro

import android.app.Application
import com.example.smsreaderpro.data.local.AppDatabase
import com.example.smsreaderpro.data.repository.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReaderProApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { TransactionRepository(database.transactionDao()) }

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            repository.checkAndSeedInitialData()
        }
    }
}
