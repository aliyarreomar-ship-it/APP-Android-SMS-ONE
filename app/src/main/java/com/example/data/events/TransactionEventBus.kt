package com.example.data.events

import com.example.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object TransactionEventBus {
    private val _newTransactionEvent = MutableSharedFlow<TransactionEntity>(extraBufferCapacity = 10)
    val newTransactionEvent: SharedFlow<TransactionEntity> = _newTransactionEvent.asSharedFlow()

    fun postTransaction(transaction: TransactionEntity) {
        _newTransactionEvent.tryEmit(transaction)
    }
}
