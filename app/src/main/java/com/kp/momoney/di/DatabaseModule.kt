package com.kp.momoney.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kp.momoney.data.local.AppDatabase
import com.kp.momoney.data.local.dao.BudgetDao
import com.kp.momoney.data.local.dao.CategoryDao
import com.kp.momoney.data.local.dao.NotificationDao
import com.kp.momoney.data.local.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "momoney_database"
        )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    seedDefaultCategories(db)
                }
                
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    // Also seed on open to ensure defaults exist even if DB was created before
                    seedDefaultCategories(db)
                }
                
                /**
                 * Seeds default system categories using upsert logic (Insert or Ignore).
                 * This ensures that even if the database isn't empty, missing defaults are restored.
                 * All default categories have userId = NULL to mark them as system categories.
                 */
                private fun seedDefaultCategories(db: SupportSQLiteDatabase) {
                    // Define default categories with their properties
                    // Format: [name, type, icon_name, color_hex, firestore_id]
                    val defaultCategories = listOf(
                        // Salary (Income, Green)
                        arrayOf("Salary", "Income", "salary", "4CAF50", java.util.UUID.randomUUID().toString()),
                        // Food (Expense, Orange)
                        arrayOf("Food", "Expense", "food", "FF9800", java.util.UUID.randomUUID().toString()),
                        // Transport (Expense, Blue)
                        arrayOf("Transport", "Expense", "transport", "2196F3", java.util.UUID.randomUUID().toString()),
                        // Rent (Expense, Red)
                        arrayOf("Rent", "Expense", "rent", "F44336", java.util.UUID.randomUUID().toString()),
                        // Entertainment (Expense, Purple)
                        arrayOf("Entertainment", "Expense", "beer", "9C27B0", java.util.UUID.randomUUID().toString())
                    )
                    
                    // Upsert logic: Check if category exists, insert only if it doesn't
                    defaultCategories.forEach { category ->
                        val name = category[0] as String
                        val type = category[1] as String
                        val iconName = category[2] as String
                        val colorHex = category[3] as String
                        val firestoreId = category[4] as String
                        
                        // Check if this system category (name + user_id IS NULL) already exists
                        val cursor = db.query(
                            "SELECT COUNT(*) FROM categories WHERE name = ? AND user_id IS NULL",
                            arrayOf(name)
                        )
                        val exists = if (cursor.moveToFirst()) {
                            cursor.getInt(0) > 0
                        } else {
                            false
                        }
                        cursor.close()
                        
                        // Insert only if it doesn't exist
                        if (!exists) {
                            db.execSQL(
                                "INSERT INTO categories (name, type, icon_name, color_hex, user_id, firestore_id) VALUES (?, ?, ?, ?, ?, ?)",
                                arrayOf(name, type, iconName, colorHex, null, firestoreId)
                            )
                        }
                    }
                }
            })
            .fallbackToDestructiveMigration() // For development; consider proper migrations for production
            .build()
    }
    
    @Provides
    @Singleton
    fun provideCategoryDao(database: AppDatabase): CategoryDao {
        return database.categoryDao()
    }
    
    @Provides
    @Singleton
    fun provideTransactionDao(database: AppDatabase): TransactionDao {
        return database.transactionDao()
    }
    
    @Provides
    @Singleton
    fun provideBudgetDao(database: AppDatabase): BudgetDao {
        return database.budgetDao()
    }
    
    @Provides
    @Singleton
    fun provideNotificationDao(database: AppDatabase): NotificationDao {
        return database.notificationDao()
    }
}

