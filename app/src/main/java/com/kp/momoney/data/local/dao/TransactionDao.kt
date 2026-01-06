package com.kp.momoney.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.kp.momoney.data.local.entity.TransactionEntity
import com.kp.momoney.data.local.entity.TransactionWithCategory
import com.kp.momoney.data.local.model.CategorySpending
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Transaction
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionWithCategory>>
    
    @Query("SELECT * FROM transactions WHERE id = :transactionId")
    suspend fun getTransactionById(transactionId: Long): TransactionEntity?
    
    @Query("SELECT * FROM transactions WHERE firestore_id = :firestoreId")
    suspend fun getTransactionByFirestoreId(firestoreId: String): TransactionEntity?
    
    @Query("SELECT * FROM transactions WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>
    
    @Query("SELECT * FROM transactions WHERE category_id = :categoryId ORDER BY date DESC")
    fun getTransactionsByCategory(categoryId: Int): Flow<List<TransactionEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long
    
    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTransaction(transaction: TransactionEntity): Long
    
    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)
    
    @Query("DELETE FROM transactions WHERE id = :transactionId")
    suspend fun deleteTransactionById(transactionId: Long)
    
    @Query("SELECT category_id as categoryId, SUM(amount) as total FROM transactions WHERE date BETWEEN :startDate AND :endDate AND type = 'Expense' AND category_id IS NOT NULL GROUP BY category_id")
    fun getCategorySpendingByDateRange(startDate: Long, endDate: Long): Flow<List<CategorySpending>>
    
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE category_id = :categoryId AND date BETWEEN :startDate AND :endDate AND type = 'Expense'")
    suspend fun getCategorySpendingForCategory(categoryId: Int, startDate: Long, endDate: Long): Double
    
    @Query("SELECT * FROM transactions WHERE recurrence != 'NEVER'")
    suspend fun getRecurringTransactions(): List<TransactionEntity>
}

