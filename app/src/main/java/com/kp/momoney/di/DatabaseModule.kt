package com.kp.momoney.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kp.momoney.data.local.AppDatabase
import com.kp.momoney.data.local.dao.BudgetDao
import com.kp.momoney.data.local.dao.CategoryDao
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
                    // Check if categories table is empty and seed default categories
                    val cursor = db.query("SELECT COUNT(*) FROM categories")
                    val count = if (cursor.moveToFirst()) {
                        cursor.getInt(0)
                    } else {
                        0
                    }
                    cursor.close()
                    
                    if (count == 0) {
                        // Insert default categories with userId = NULL (system defaults)
                        // Using parameterized queries would be better, but execSQL with proper escaping works here
                        val foodId = java.util.UUID.randomUUID().toString()
                        val rentId = java.util.UUID.randomUUID().toString()
                        val salaryId = java.util.UUID.randomUUID().toString()
                        
                        db.execSQL(
                            "INSERT INTO categories (name, type, icon_name, color_hex, user_id, firestore_id) VALUES (?, ?, ?, ?, ?, ?)",
                            arrayOf("Food", "Expense", "food", "FF9800", null, foodId)
                        )
                        db.execSQL(
                            "INSERT INTO categories (name, type, icon_name, color_hex, user_id, firestore_id) VALUES (?, ?, ?, ?, ?, ?)",
                            arrayOf("Rent", "Expense", "rent", "F44336", null, rentId)
                        )
                        db.execSQL(
                            "INSERT INTO categories (name, type, icon_name, color_hex, user_id, firestore_id) VALUES (?, ?, ?, ?, ?, ?)",
                            arrayOf("Salary", "Income", "salary", "4CAF50", null, salaryId)
                        )
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
}

