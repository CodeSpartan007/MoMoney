package com.kp.momoney.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kp.momoney.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    
    /**
     * Gets all categories that are either system defaults (userId IS NULL) 
     * or belong to the specified user.
     * This ensures users see Defaults + Their Own, but NOT other people's categories.
     */
    @Query("SELECT * FROM categories WHERE user_id IS NULL OR user_id = :currentUserId ORDER BY name ASC")
    fun getAllCategories(currentUserId: String): Flow<List<CategoryEntity>>
    
    /**
     * Gets the count of all categories in the table.
     * Used to check if the table is empty for seeding default categories.
     */
    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int
    
    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: Int): CategoryEntity?
    
    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    suspend fun getCategoryByName(name: String): CategoryEntity?
    
    @Query("SELECT * FROM categories WHERE firestore_id = :firestoreId")
    suspend fun getCategoryByFirestoreId(firestoreId: String): CategoryEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long
    
    @Update
    suspend fun updateCategory(category: CategoryEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCategory(category: CategoryEntity): Long
    
    @Delete
    suspend fun deleteCategory(category: CategoryEntity)
    
    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteCategory(categoryId: Int)
    
    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteCategoryById(categoryId: Int)
}

