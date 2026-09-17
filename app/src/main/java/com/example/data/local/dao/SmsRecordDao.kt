package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.SmsRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsRecordDao {
    @Query("SELECT * FROM sms_records ORDER BY timestamp DESC")
    fun getAllSmsRecords(): Flow<List<SmsRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSmsRecord(record: SmsRecordEntity): Long
    
    @Query("SELECT * FROM sms_records WHERE body = :body AND timestamp = :timestamp LIMIT 1")
    suspend fun getRecordByContent(body: String, timestamp: Long): SmsRecordEntity?
    
    @Query("DELETE FROM sms_records")
    suspend fun deleteAllSmsRecords()
}
