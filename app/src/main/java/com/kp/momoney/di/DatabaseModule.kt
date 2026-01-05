package com.kp.momoney.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kp.momoney.data.local.AppDatabase
import com.kp.momoney.data.local.dao.BudgetDao
import com.kp.momoney.data.local.dao.CategoryDao
import com.kp.momoney.data.local.dao.TransactionDao
import com.kp.momoney.data.local.entity.CategoryEntity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        val database = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "momoney_database"
        )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // Database is created, we'll seed it after build
                }
            })
            .fallbackToDestructiveMigration() // For development; consider proper migrations for production
            .build()
        
        // Seed database after it's built if it's empty
        val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        applicationScope.launch {
            val categories = database.categoryDao().getAllCategories(null).first()
            if (categories.isEmpty()) {
                seedDatabase(database.categoryDao())
            }
        }
        
        return database
    }
    
    private suspend fun seedDatabase(categoryDao: CategoryDao) {
        val defaultCategories = listOf(
            CategoryEntity(
                name = "Salary",
                type = "Income",
                colorHex = "4CAF50", // Green
                iconName = "salary",
                userId = null, // System default
                firestoreId = UUID.randomUUID().toString()
            ),
            CategoryEntity(
                name = "Food",
                type = "Expense",
                colorHex = "FF9800", // Orange
                iconName = "food",
                userId = null, // System default
                firestoreId = UUID.randomUUID().toString()
            ),
            CategoryEntity(
                name = "Transport",
                type = "Expense",
                colorHex = "2196F3", // Blue
                iconName = "transport",
                userId = null, // System default
                firestoreId = UUID.randomUUID().toString()
            ),
            CategoryEntity(
                name = "Rent",
                type = "Expense",
                colorHex = "F44336", // Red
                iconName = "rent",
                userId = null, // System default
                firestoreId = UUID.randomUUID().toString()
            )
        )
        
        defaultCategories.forEach { category ->
            categoryDao.insertCategory(category)
        }
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

