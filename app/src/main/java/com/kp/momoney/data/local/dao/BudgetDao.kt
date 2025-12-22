package com.kp.momoney.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kp.momoney.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    
    @Query("SELECT * FROM budgets WHERE category_id = :categoryId")
    fun getBudgetByCategory(categoryId: Int): Flow<BudgetEntity?>
    
    @Query("SELECT * FROM budgets WHERE category_id = :categoryId LIMIT 1")
    suspend fun getBudgetForCategory(categoryId: Int): BudgetEntity?
    
    @Query("SELECT * FROM budgets")
    fun getAllBudgets(): Flow<List<BudgetEntity>>
    
    @Query("SELECT * FROM budgets WHERE id = :budgetId")
    suspend fun getBudgetById(budgetId: Long): BudgetEntity?
    
    @Query("SELECT * FROM budgets WHERE firestore_id = :firestoreId")
    suspend fun getBudgetByFirestoreId(firestoreId: String): BudgetEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity): Long
    
    @Update
    suspend fun updateBudget(budget: BudgetEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBudget(budget: BudgetEntity): Long
    
    @Delete
    suspend fun deleteBudget(budget: BudgetEntity)
    
    @Query("DELETE FROM budgets WHERE id = :budgetId")
    suspend fun deleteBudgetById(budgetId: Long)
}

