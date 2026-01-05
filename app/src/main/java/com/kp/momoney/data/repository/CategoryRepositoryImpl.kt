package com.kp.momoney.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kp.momoney.data.local.dao.CategoryDao
import com.kp.momoney.data.local.entity.CategoryEntity
import com.kp.momoney.data.mapper.toCategoryEntity
import com.kp.momoney.data.mapper.toFirestoreMap
import com.kp.momoney.domain.model.Category
import com.kp.momoney.domain.repository.CategoryRepository
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import android.util.Log

class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : CategoryRepository {
    
    private val currentUserId: String?
        get() = auth.currentUser?.uid
    
    override fun getAllCategories(): Flow<List<Category>> {
        // Pass empty string for unauthenticated users - they will only see default categories (userId IS NULL)
        // Authenticated users will see default categories + their own (userId IS NULL OR userId = currentUserId)
        val userId = currentUserId ?: ""
        return categoryDao.getAllCategories(userId).map { categories ->
            categories.map { it.toDomain() }
        }
    }
    
    override suspend fun getCategoryById(id: Int): Category? {
        return categoryDao.getCategoryById(id)?.toDomain()
    }
    
    override suspend fun addUserCategory(name: String, type: String, color: String) {
        val userId = currentUserId ?: throw IllegalStateException("User must be authenticated to add custom categories")
        
        val category = CategoryEntity(
            name = name,
            type = type,
            colorHex = color,
            iconName = "spanner", // Hardcoded as per requirements
            userId = userId,
            firestoreId = UUID.randomUUID().toString()
        )
        
        // Save to Room first
        val roomId = categoryDao.insertCategory(category)
        
        // Sync to Firestore
        try {
            val firestoreMap = category.toFirestoreMap()
            firestore.collection("users")
                .document(userId)
                .collection("categories")
                .document(category.firestoreId)
                .set(firestoreMap)
                .await()
            Log.d("CategoryRepo", "Category synced to Firestore: ${category.firestoreId}")
        } catch (e: Exception) {
            Log.e("CategoryRepo", "Failed to sync category to Firestore", e)
            // Continue even if Firestore sync fails - local data is saved
        }
    }
    
    override suspend fun syncCategories() {
        val userId = currentUserId ?: run {
            Log.d("CategoryRepo", "No authenticated user, skipping category sync")
            return
        }
        
        try {
            Log.d("CategoryRepo", "Starting category sync from Firestore")
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("categories")
                .get()
                .await()
            
            val firestoreCategories = snapshot.documents.mapNotNull { document ->
                document.toCategoryEntity()
            }
            
            Log.d("CategoryRepo", "Fetched ${firestoreCategories.size} categories from Firestore")
            
            // Insert/update categories in Room using REPLACE strategy
            firestoreCategories.forEach { entity ->
                // Check if category with this firestoreId already exists
                val existing = categoryDao.getCategoryByFirestoreId(entity.firestoreId)
                
                if (existing != null) {
                    // Update existing category, preserving Room ID
                    val updatedEntity = entity.copy(id = existing.id)
                    categoryDao.upsertCategory(updatedEntity)
                } else {
                    // Insert new category
                    categoryDao.upsertCategory(entity)
                }
            }
            
            Log.d("CategoryRepo", "Successfully synced ${firestoreCategories.size} categories to Room")
        } catch (e: Exception) {
            Log.e("CategoryRepo", "Failed to sync categories from Firestore", e)
            throw e
        }
    }
    
    override suspend fun deleteCategory(categoryId: Int) {
        categoryDao.deleteCategory(categoryId)
        
        // Also delete from Firestore if user is authenticated
        currentUserId?.let { userId ->
            val category = categoryDao.getCategoryById(categoryId)
            category?.firestoreId?.let { firestoreId ->
                try {
                    firestore.collection("users")
                        .document(userId)
                        .collection("categories")
                        .document(firestoreId)
                        .delete()
                        .await()
                    Log.d("CategoryRepo", "Category deleted from Firestore: $firestoreId")
                } catch (e: Exception) {
                    Log.e("CategoryRepo", "Failed to delete category from Firestore", e)
                    // Continue even if Firestore delete fails - local data is deleted
                }
            }
        }
    }
    
    private fun com.kp.momoney.data.local.entity.CategoryEntity.toDomain(): Category {
        return Category(
            id = id,
            name = name,
            icon = iconName,
            color = colorHex,
            type = type
        )
    }
}

