package com.example.smsreaderpro.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smsreaderpro.data.model.Transaction
import com.example.smsreaderpro.data.model.TransactionType
import com.example.smsreaderpro.data.parser.SmsParserEngine
import com.example.smsreaderpro.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TransactionViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    val transactions: StateFlow<List<Transaction>> = repository.allTransactions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val unverifiedTransactions: StateFlow<List<Transaction>> = repository.unverifiedTransactions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val totalIncome: StateFlow<Double> = transactions.map { list ->
        list.filter { it.transactionType == TransactionType.INCOME }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpense: StateFlow<Double> = transactions.map { list ->
        list.filter { it.transactionType != TransactionType.INCOME }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val netBalance: StateFlow<Double> = transactions.map { list ->
        val inc = list.filter { it.transactionType == TransactionType.INCOME }.sumOf { it.amount }
        val exp = list.filter { it.transactionType != TransactionType.INCOME }.sumOf { it.amount }
        inc - exp
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val _pendingPopupTx = MutableStateFlow<Transaction?>(null)
    val pendingPopupTx: StateFlow<Transaction?> = _pendingPopupTx.asStateFlow()

    private val _selectedDetailTx = MutableStateFlow<Transaction?>(null)
    val selectedDetailTx: StateFlow<Transaction?> = _selectedDetailTx.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun selectDetailTx(tx: Transaction?) {
        _selectedDetailTx.value = tx
    }

    fun dismissPopup() {
        _pendingPopupTx.value = null
    }

    fun savePopup(category: String, note: String) {
        val tx = _pendingPopupTx.value ?: return
        viewModelScope.launch {
            repository.updateCategoryAndNotes(tx.id, category, note)
            showToast("Xisaabta waxaa lagu diiwaangeliyay: $category")
            _pendingPopupTx.value = null
        }
    }

    fun verifyTransaction(id: Long) {
        viewModelScope.launch {
            repository.verifyTransaction(id)
            showToast("Xisaabta waa la ansixiyay")
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
            if (_selectedDetailTx.value?.id == id) {
                _selectedDetailTx.value = null
            }
            showToast("Xisaabta waa la tirtiray")
        }
    }

    fun deleteAllTransactions() {
        viewModelScope.launch {
            repository.deleteAllTransactions()
            _selectedDetailTx.value = null
            _pendingPopupTx.value = null
            showToast("Dhammaan xisaabaadkii waa la tirtiray")
        }
    }

    fun processSyntheticSms(sender: String, body: String, triggerPopup: Boolean = true) {
        viewModelScope.launch {
            val tx = repository.processIncomingSms(sender, body, isVerified = !triggerPopup)
            if (tx != null) {
                if (triggerPopup) {
                    _pendingPopupTx.value = tx
                } else {
                    showToast("Fariin cusub ayaa la qabtay (${tx.provider}: $${tx.amount})")
                }
            } else {
                showToast("Lama aqoonsan habka fariintan ama horay ayaa loo qabtay")
            }
        }
    }

    fun scanBatchSms(rawText: String): Pair<Int, Int> {
        val chunks = rawText.split(Regex("""\n\s*\n|(?=\[\+?\-?\$|\bWaxaad\b|\bYou have received\b)""", RegexOption.IGNORE_CASE))
            .map { it.trim() }
            .filter { it.length > 15 }

        var parsedCount = 0
        viewModelScope.launch {
            for (chunk in chunks) {
                val sender = when {
                    chunk.contains("edahab", ignoreCase = true) || chunk.contains("somtel", ignoreCase = true) -> "EDAHAB"
                    chunk.contains("jeeb", ignoreCase = true) || chunk.contains("premier", ignoreCase = true) -> "Jeeb"
                    chunk.contains("zaad", ignoreCase = true) -> "ZAAD"
                    chunk.contains("sahal", ignoreCase = true) -> "SAHAL"
                    else -> "EVCPlus"
                }
                val tx = repository.processIncomingSms(sender, chunk, isVerified = true)
                if (tx != null) parsedCount++
            }
            showToast("Waxaa la aqriyay ${chunks.size} fariimood, $parsedCount ayaa la qabtay!")
        }
        return Pair(chunks.size, parsedCount)
    }

    class Factory(private val repository: TransactionRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TransactionViewModel(repository) as T
        }
    }
}
