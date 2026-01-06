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
            userId = userId, // Set to current user's UID
            firestoreId = UUID.randomUUID().toString()
        )
        
        // Step 1: Save to Room immediately (offline-first approach)
        categoryDao.insertCategory(category)
        Log.d("CategoryRepo", "Category saved to Room: ${category.name} (firestoreId: ${category.firestoreId})")
        
        // Step 2: Save to Firestore users/{uid}/categories immediately
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
            // The category will be synced on next sync operation
        }
    }
    
    override suspend fun syncCategories() {
        val userId = currentUserId ?: run {
            Log.d("CategoryRepo", "No authenticated user, skipping category sync")
            return
        }
        
        try {
            Log.d("CategoryRepo", "Starting category sync from Firestore for user: $userId")
            
            // Step 1: Query Firestore users/{uid}/categories
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("categories")
                .get()
                .await()
            
            Log.d("CategoryRepo", "Fetched ${snapshot.documents.size} category documents from Firestore")
            
            // Step 2: Loop through documents
            // Step 3: Map to CategoryEntity, ensuring userId is set to current uid
            val firestoreCategories = snapshot.documents.mapNotNull { document ->
                try {
                    val firestoreId = document.getString("firestoreId") ?: document.id
                    val name = document.getString("name") ?: return@mapNotNull null
                    val type = document.getString("type") ?: return@mapNotNull null
                    val colorHex = document.getString("colorHex") ?: return@mapNotNull null
                    val iconName = document.getString("iconName") ?: "spanner"
                    
                    // Step 3: Ensure userId is set to the current uid
                    CategoryEntity(
                        id = 0, // Room will auto-generate
                        firestoreId = firestoreId,
                        userId = userId, // Always set to current user's UID
                        name = name,
                        type = type,
                        iconName = iconName,
                        colorHex = colorHex
                    )
                } catch (e: Exception) {
                    Log.e("CategoryRepo", "Error mapping category document ${document.id}", e)
                    null
                }
            }
            
            Log.d("CategoryRepo", "Mapped ${firestoreCategories.size} categories from Firestore")
            
            // Step 4: Insert/Update in Room
            firestoreCategories.forEach { entity ->
                // Check if category with this firestoreId already exists
                val existing = categoryDao.getCategoryByFirestoreId(entity.firestoreId)
                
                if (existing != null) {
                    // Update existing category, preserving Room ID and ensuring userId is correct
                    val updatedEntity = entity.copy(
                        id = existing.id,
                        userId = userId // Ensure userId is set correctly
                    )
                    categoryDao.upsertCategory(updatedEntity)
                    Log.d("CategoryRepo", "Updated category: ${entity.name} (firestoreId: ${entity.firestoreId})")
                } else {
                    // Insert new category
                    categoryDao.upsertCategory(entity)
                    Log.d("CategoryRepo", "Inserted new category: ${entity.name} (firestoreId: ${entity.firestoreId})")
                }
            }
            
            Log.d("CategoryRepo", "Successfully synced ${firestoreCategories.size} categories to Room")
        } catch (e: Exception) {
            Log.e("CategoryRepo", "Failed to sync categories from Firestore", e)
            throw e
        }
    }
    
    override suspend fun deleteCategory(categoryId: Int) {
        // Get category before deleting to access name and firestoreId
        val category = categoryDao.getCategoryById(categoryId)
            ?: throw IllegalArgumentException("Category with id $categoryId not found")
        
        val categoryName = category.name
        val firestoreId = category.firestoreId
        
        // Delete from Room first (cascading delete will handle related transactions/budgets locally)
        categoryDao.deleteCategory(categoryId)
        
        // Also delete from Firestore if user is authenticated
        currentUserId?.let { userId ->
            firestoreId?.let { fsId ->
                try {
                    // Use batched write for atomic deletion
                    val batch = firestore.batch()
                    
                    // Step 1: Delete the category document
                    val categoryRef = firestore.collection("users")
                        .document(userId)
                        .collection("categories")
                        .document(fsId)
                    batch.delete(categoryRef)
                    
                    // Step 2: Query and delete related transactions (by categoryName)
                    val transactionsSnapshot = firestore.collection("users")
                        .document(userId)
                        .collection("transactions")
                        .whereEqualTo("categoryName", categoryName)
                        .get()
                        .await()
                    
                    transactionsSnapshot.documents.forEach { doc ->
                        batch.delete(doc.reference)
                    }
                    Log.d("CategoryRepo", "Found ${transactionsSnapshot.documents.size} transactions to delete")
                    
                    // Step 3: Query and delete related budgets (by categoryId - local ID)
                    val budgetsSnapshot = firestore.collection("users")
                        .document(userId)
                        .collection("budgets")
                        .whereEqualTo("categoryId", categoryId.toLong())
                        .get()
                        .await()
                    
                    budgetsSnapshot.documents.forEach { doc ->
                        batch.delete(doc.reference)
                    }
                    Log.d("CategoryRepo", "Found ${budgetsSnapshot.documents.size} budgets to delete")
                    
                    // Commit the batch
                    batch.commit().await()
                    Log.d("CategoryRepo", "Category and related data deleted from Firestore: $fsId")
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

