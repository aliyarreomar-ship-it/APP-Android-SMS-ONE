package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.PendingReminderEntity

@Dao
interface PendingReminderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(reminder: PendingReminderEntity): Long

    @Query("SELECT * FROM pending_reminders WHERE transactionId = :transactionId LIMIT 1")
    suspend fun getByTransactionId(transactionId: Long): PendingReminderEntity?

    @Query("DELETE FROM pending_reminders WHERE transactionId = :transactionId")
    suspend fun deleteByTransactionId(transactionId: Long)

    @Query("SELECT * FROM pending_reminders WHERE nextReminderTime <= :currentTime")
    suspend fun getDueReminders(currentTime: Long): List<PendingReminderEntity>
}
