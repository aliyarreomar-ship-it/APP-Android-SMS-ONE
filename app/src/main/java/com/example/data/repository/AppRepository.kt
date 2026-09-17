package com.example.data.repository

import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.SmsRecordDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.SmsRecordEntity
import com.example.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val smsRecordDao: SmsRecordDao
) {
    // Transactions
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val verifiedTransactions: Flow<List<TransactionEntity>> = transactionDao.getVerifiedTransactions()
    val unverifiedTransactions: Flow<List<TransactionEntity>> = transactionDao.getUnverifiedTransactions()
    
    fun getTransactionByIdFlow(id: Long): Flow<TransactionEntity?> = transactionDao.getTransactionByIdFlow(id)
    
    suspend fun getTransactionById(id: Long): TransactionEntity? = transactionDao.getTransactionById(id)

    suspend fun getTransactionByReference(refId: String, provider: String): TransactionEntity? =
        transactionDao.getTransactionByReference(refId, provider)

    suspend fun getTransactionBySmsBody(smsBody: String): TransactionEntity? =
        transactionDao.getTransactionBySmsBody(smsBody)
    
    suspend fun insertTransaction(transaction: TransactionEntity): Long = transactionDao.insertTransaction(transaction)
    
    suspend fun updateTransaction(transaction: TransactionEntity) = transactionDao.updateTransaction(transaction)
    
    suspend fun deleteTransaction(transaction: TransactionEntity) = transactionDao.deleteTransaction(transaction)

    suspend fun deleteTransactionById(id: Long) = transactionDao.deleteTransactionById(id)

    suspend fun deleteAllTransactions() = transactionDao.deleteAllTransactions()

    suspend fun deleteAllSmsRecords() = smsRecordDao.deleteAllSmsRecords()

    suspend fun clearAllHistory() {
        transactionDao.deleteAllTransactions()
        smsRecordDao.deleteAllSmsRecords()
    }

    suspend fun isDuplicate(
        provider: String,
        referenceId: String?,
        smsBody: String,
        type: String,
        amount: Double,
        timestamp: Long
    ): Boolean {
        // 1. Check exact reference match if available
        if (!referenceId.isNullOrBlank() && !referenceId.startsWith("REF_")) {
            val byRef = transactionDao.findDuplicateByReference(referenceId.trim(), provider)
            if (byRef != null) return true
        }

        // 2. Check identical SMS body
        val byBody = transactionDao.findDuplicateBySmsBody(smsBody.trim())
        if (byBody != null) return true

        // 3. Check identical provider, type, amount within 2 minutes
        val similar = transactionDao.findDuplicateSimilar(provider, type, amount, timestamp)
        return similar != null
    }
    
    fun getTotalIncome(): Flow<Double?> = transactionDao.getTotalIncome()
    
    fun getTotalExpense(): Flow<Double?> = transactionDao.getTotalExpense()
    
    // Categories
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    
    suspend fun insertCategory(category: CategoryEntity) = categoryDao.insertCategory(category)
    
    // SMS Records
    val allSmsRecords: Flow<List<SmsRecordEntity>> = smsRecordDao.getAllSmsRecords()
    
    suspend fun insertSmsRecord(record: SmsRecordEntity): Long = smsRecordDao.insertSmsRecord(record)
    
    suspend fun getSmsRecordByContent(body: String, timestamp: Long): SmsRecordEntity? = smsRecordDao.getRecordByContent(body, timestamp)
}
