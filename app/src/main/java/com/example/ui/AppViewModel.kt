package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.SmsRecordEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.repository.AppRepository
import com.example.data.sms.SmsInboxScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = AppRepository(
        database.transactionDao(),
        database.categoryDao(),
        database.smsRecordDao()
    )
    private val inboxScanner = SmsInboxScanner(application)

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanNotice = MutableStateFlow<String?>(null)
    val scanNotice: StateFlow<String?> = _scanNotice.asStateFlow()

    private val _pendingReviewTransaction = MutableStateFlow<TransactionEntity?>(null)
    val pendingReviewTransaction: StateFlow<TransactionEntity?> = _pendingReviewTransaction.asStateFlow()

    init {
        viewModelScope.launch {
            com.example.data.events.TransactionEventBus.newTransactionEvent.collect { newTx ->
                _pendingReviewTransaction.value = newTx
            }
        }
    }

    fun dismissPendingReview() {
        _pendingReviewTransaction.value = null
    }

    fun saveReviewedTransaction(transaction: TransactionEntity, category: String, note: String) {
        viewModelScope.launch {
            repository.updateTransaction(
                transaction.copy(
                    category = category,
                    notes = note,
                    isVerified = true
                )
            )
            _pendingReviewTransaction.value = null
        }
    }

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val verifiedTransactions: StateFlow<List<TransactionEntity>> = repository.verifiedTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unverifiedTransactions: StateFlow<List<TransactionEntity>> = repository.unverifiedTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalIncome: StateFlow<Double> = repository.getTotalIncome()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpense: StateFlow<Double> = repository.getTotalExpense()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val netFlow: StateFlow<Double> = kotlinx.coroutines.flow.combine(totalIncome, totalExpense) { income, expense ->
        income - expense
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    
    val allCategories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun verifyTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(transaction.copy(isVerified = true))
        }
    }
    
    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun deleteTransactionById(id: Long) {
        viewModelScope.launch {
            repository.deleteTransactionById(id)
        }
    }

    fun deleteAllTransactions(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.deleteAllTransactions()
            _scanNotice.value = "Dhammaan xisaabaadkii hore waa la tirtiray"
            onComplete?.invoke()
        }
    }

    fun clearAllHistory(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.clearAllHistory()
            _scanNotice.value = "Dhammaan xogta iyo SMS-yada waa la nadiifiyay. App-ku hadda waa diyaar."
            onComplete?.invoke()
        }
    }

    fun clearSmsCache(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.deleteAllSmsRecords()
            _scanNotice.value = "Kaydka SMS-yada waa la nadiifiyay"
            onComplete?.invoke()
        }
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private val _selectedFilter = MutableStateFlow("ALL")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    fun setSelectedFilter(filter: String) {
        _selectedFilter.value = filter
    }
    
    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }
    
    suspend fun getTransactionById(id: Long): TransactionEntity? {
        return repository.getTransactionById(id)
    }

    private var lastScanTimestamp = 0L

    fun scanDeviceInbox(silent: Boolean = false, onFinished: ((SmsInboxScanner.ScanResult) -> Unit)? = null) {
        val now = System.currentTimeMillis()
        // Prevent concurrent or hyper-frequent auto-scans (minimum 3 seconds between scans)
        if (_isScanning.value || (silent && now - lastScanTimestamp < 3000)) return
        lastScanTimestamp = now

        viewModelScope.launch {
            _isScanning.value = true
            if (!silent) {
                _scanNotice.value = "Scanning phone SMS messages..."
            }
            val result = inboxScanner.scanInbox(limit = 1000)
            _isScanning.value = false

            if (result.errorMessage != null) {
                if (!silent) {
                    _scanNotice.value = "Error: ${result.errorMessage}"
                }
            } else {
                if (result.newImported > 0) {
                    _scanNotice.value = "${result.newImported} xawilaadood oo cusub ayaa toos loo diiwaangeliyay"
                } else if (!silent) {
                    _scanNotice.value = "Xogtaadu waa mid cusub (Dhammaan fariimaha waa la baaray)"
                }
            }
            onFinished?.invoke(result)
        }
    }

    fun clearScanNotice() {
        _scanNotice.value = null
    }

    fun processSyntheticSms(sender: String, body: String) {
        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()
            val parserEngine = com.example.data.parser.SmsParserEngine()
            
            // Check duplicate SMS
            val existingSms = repository.getSmsRecordByContent(body, timestamp)
            if (existingSms != null) return@launch

            // Parse SMS
            val result = parserEngine.parse(sender, body, timestamp)

            // Save SMS Record
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

            // Save Transaction if financial
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
                if (isDup) return@launch

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
                    com.example.data.events.TransactionEventBus.postTransaction(savedTx)
                }
            }
        }
    }
}
