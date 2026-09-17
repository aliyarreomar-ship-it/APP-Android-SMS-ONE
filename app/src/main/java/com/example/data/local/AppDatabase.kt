package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.SmsRecordDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.SmsRecordEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.dao.PendingReminderDao
import com.example.data.local.entity.PendingReminderEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [TransactionEntity::class, CategoryEntity::class, SmsRecordEntity::class, PendingReminderEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun smsRecordDao(): SmsRecordDao
    abstract fun pendingReminderDao(): PendingReminderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sms_reader_pro_database"
                )
                .addCallback(AppDatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
    
    private class AppDatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateInitialCategories(database.categoryDao())
                }
            }
        }
        
        suspend fun populateInitialCategories(categoryDao: CategoryDao) {
            val defaultCategories = listOf(
                CategoryEntity("Food", "restaurant", "#FF5722", true),
                CategoryEntity("Transport", "directions_car", "#2196F3", true),
                CategoryEntity("Bills", "receipt", "#9C27B0", true),
                CategoryEntity("Airtime", "phone_iphone", "#4CAF50", true),
                CategoryEntity("Internet", "wifi", "#03A9F4", true),
                CategoryEntity("Shopping", "shopping_cart", "#E91E63", true),
                CategoryEntity("Family", "family_restroom", "#FF9800", true),
                CategoryEntity("Business", "business_center", "#795548", true),
                CategoryEntity("Other", "category", "#607D8B", true)
            )
            categoryDao.insertCategories(defaultCategories)
        }
    }
}
