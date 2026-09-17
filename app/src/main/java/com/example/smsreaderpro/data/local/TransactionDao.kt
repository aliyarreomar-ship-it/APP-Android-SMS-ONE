package com.example.smsreaderpro.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY transactionDate DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isVerified = 0 ORDER BY transactionDate DESC")
    fun getUnverifiedTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE referenceId = :ref LIMIT 1")
    suspend fun findByReference(ref: String): TransactionEntity?

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("UPDATE transactions SET isVerified = 1 WHERE id = :id")
    suspend fun verifyTransaction(id: Long)

    @Query("UPDATE transactions SET category = :category, notes = :notes, isVerified = 1 WHERE id = :id")
    suspend fun updateCategoryAndNotes(id: Long, category: String, notes: String)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}
