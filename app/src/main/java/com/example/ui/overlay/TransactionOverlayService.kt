package com.example.ui.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.ui.theme.SmsReaderProTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TransactionOverlayService : Service(), LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {
    private lateinit var windowManager: WindowManager
    private var composeView: ComposeView? = null

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override val viewModelStore: ViewModelStore
        get() = store

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundServiceNotification()
        
        val transactionId = intent?.getLongExtra("transactionId", -1L) ?: -1L
        val amount = intent?.getStringExtra("amount") ?: ""
        val provider = intent?.getStringExtra("provider") ?: ""
        val category = intent?.getStringExtra("category") ?: "Other"
        val type = intent?.getStringExtra("type") ?: "EXPENSE"
        
        showOverlay(transactionId, amount, provider, category, type)
        return START_NOT_STICKY
    }

    private fun startForegroundServiceNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "overlay_channel",
                "Transaction Overlay",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
        val notification: Notification = NotificationCompat.Builder(this, "overlay_channel")
            .setContentTitle("Processing Transaction")
            .setContentText("Displaying transaction details")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1001, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(1001, notification)
        }
    }

    private fun showOverlay(transactionId: Long, amount: String, provider: String, initialCategory: String, initialType: String) {
        if (composeView != null) return

        composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@TransactionOverlayService)
            setViewTreeSavedStateRegistryOwner(this@TransactionOverlayService)
            setViewTreeViewModelStoreOwner(this@TransactionOverlayService)
            setContent {
                SmsReaderProTheme {
                    var transaction by remember { mutableStateOf<com.example.data.local.entity.TransactionEntity?>(null) }
                    var isSaving by remember { mutableStateOf(false) }

                    LaunchedEffect(transactionId) {
                        val db = com.example.data.local.AppDatabase.getDatabase(this@TransactionOverlayService)
                        transaction = db.transactionDao().getTransactionById(transactionId)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (transaction != null) {
                            com.example.ui.components.TransactionPopupContent(
                                transaction = transaction!!,
                                onSave = { category, note ->
                                    isSaving = true
                                    CoroutineScope(Dispatchers.IO).launch {
                                        val db = com.example.data.local.AppDatabase.getDatabase(this@TransactionOverlayService)
                                        val txDao = db.transactionDao()
                                        txDao.updateTransaction(
                                            transaction!!.copy(
                                                category = category,
                                                notes = note.takeIf { it.isNotBlank() },
                                                isVerified = true
                                            )
                                        )
                                        kotlinx.coroutines.withContext(Dispatchers.Main) {
                                            closeOverlay()
                                        }
                                    }
                                },
                                onDismiss = {
                                    closeOverlay()
                                }
                            )
                        } else {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.CENTER

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        
        try {
            windowManager.addView(composeView, params)
        } catch (e: Exception) {
            e.printStackTrace()
            // Stop service if we cannot show the overlay
            stopSelf()
        }
    }

    private fun closeOverlay() {
        composeView?.let {
            windowManager.removeView(it)
            composeView = null
        }
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
        composeView?.let {
            windowManager.removeView(it)
            composeView = null
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
