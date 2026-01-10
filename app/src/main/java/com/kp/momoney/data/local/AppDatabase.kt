package com.kp.momoney.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kp.momoney.data.local.dao.BudgetDao
import com.kp.momoney.data.local.dao.CategoryDao
import com.kp.momoney.data.local.dao.NotificationDao
import com.kp.momoney.data.local.dao.TransactionDao
import com.kp.momoney.data.local.entity.BudgetEntity
import com.kp.momoney.data.local.entity.CategoryEntity
import com.kp.momoney.data.local.entity.NotificationEntity
import com.kp.momoney.data.local.entity.TransactionEntity

@Database(
    entities = [
        CategoryEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
        NotificationEntity::class
    ],
    version = 6, // Incremented to add notifications table
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun notificationDao(): NotificationDao
    
}

