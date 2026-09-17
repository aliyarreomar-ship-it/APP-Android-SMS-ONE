package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY transactionDate DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isVerified = 1 ORDER BY transactionDate DESC")
    fun getVerifiedTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isVerified = 0 ORDER BY transactionDate DESC")
    fun getUnverifiedTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?
    
    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getTransactionByIdFlow(id: Long): Flow<TransactionEntity?>

    @Query("SELECT * FROM transactions WHERE referenceId = :refId AND provider = :provider LIMIT 1")
    suspend fun getTransactionByReference(refId: String, provider: String): TransactionEntity?
    
    @Query("SELECT * FROM transactions WHERE originalSms = :smsBody LIMIT 1")
    suspend fun getTransactionBySmsBody(smsBody: String): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    @Query("SELECT * FROM transactions WHERE referenceId = :refId AND LOWER(provider) = LOWER(:provider) LIMIT 1")
    suspend fun findDuplicateByReference(refId: String, provider: String): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE TRIM(LOWER(originalSms)) = TRIM(LOWER(:smsBody)) LIMIT 1")
    suspend fun findDuplicateBySmsBody(smsBody: String): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE LOWER(provider) = LOWER(:provider) AND transactionType = :type AND ABS(amount - :amount) < 0.001 AND ABS(transactionDate - :timestamp) < 120000 LIMIT 1")
    suspend fun findDuplicateSimilar(provider: String, type: String, amount: Double, timestamp: Long): TransactionEntity?

    @Query("SELECT SUM(amount) FROM transactions WHERE transactionType = 'INCOME' AND isDuplicate = 0")
    fun getTotalIncome(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE transactionType = 'EXPENSE' AND isDuplicate = 0")
    fun getTotalExpense(): Flow<Double?>
    
    @Query("SELECT * FROM transactions WHERE transactionDate >= :startDate AND transactionDate <= :endDate AND isVerified = 1 ORDER BY transactionDate DESC")
    fun getTransactionsBetween(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>
}
