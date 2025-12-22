package com.kp.momoney.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kp.momoney.data.local.dao.BudgetDao
import com.kp.momoney.data.local.entity.BudgetEntity
import com.kp.momoney.data.mapper.toBudgetEntity
import com.kp.momoney.data.mapper.toFirestoreMap
import com.kp.momoney.domain.repository.BudgetRepository
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.tasks.await
import android.util.Log

class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : BudgetRepository {
    
    private val currentUserId: String?
        get() = auth.currentUser?.uid
    
    override suspend fun getBudgetForCategory(categoryId: Int): BudgetEntity? {
        return budgetDao.getBudgetForCategory(categoryId)
    }
    
    override suspend fun upsertBudget(budget: BudgetEntity): Long {
        // Generate firestoreId if not present
        val firestoreId = budget.firestoreId ?: UUID.randomUUID().toString()
        val budgetWithFirestoreId = budget.copy(firestoreId = firestoreId)
        
        // Save to Room first
        val roomId = budgetDao.upsertBudget(budgetWithFirestoreId)
        
        // Sync to Firestore if user is authenticated
        currentUserId?.let { userId ->
            try {
                val firestoreMap = budgetWithFirestoreId.toFirestoreMap()
                firestore.collection("users")
                    .document(userId)
                    .collection("budgets")
                    .document(firestoreId)
                    .set(firestoreMap)
                    .await()
                Log.d("BudgetRepo", "Budget synced to Firestore: $firestoreId")
            } catch (e: Exception) {
                Log.e("BudgetRepo", "Failed to sync budget to Firestore", e)
                // Continue even if Firestore sync fails - local data is saved
            }
        }
        
        return roomId
    }
    
    override suspend fun syncBudgets() {
        val userId = currentUserId ?: run {
            Log.d("BudgetRepo", "No authenticated user, skipping budget sync")
            return
        }
        
        try {
            Log.d("BudgetRepo", "Starting budget sync from Firestore")
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("budgets")
                .get()
                .await()
            
            val firestoreBudgets = snapshot.documents.mapNotNull { document ->
                document.toBudgetEntity()
            }
            
            Log.d("BudgetRepo", "Fetched ${firestoreBudgets.size} budgets from Firestore")
            
            // Insert/update budgets in Room using REPLACE strategy
            firestoreBudgets.forEach { entity ->
                // Check if budget with this firestoreId already exists
                val existing = entity.firestoreId?.let { 
                    budgetDao.getBudgetByFirestoreId(it) 
                }
                
                if (existing != null) {
                    // Update existing budget, preserving Room ID
                    val updatedEntity = entity.copy(id = existing.id)
                    budgetDao.upsertBudget(updatedEntity)
                } else {
                    // Insert new budget
                    budgetDao.upsertBudget(entity)
                }
            }
            
            Log.d("BudgetRepo", "Successfully synced ${firestoreBudgets.size} budgets to Room")
        } catch (e: Exception) {
            Log.e("BudgetRepo", "Failed to sync budgets from Firestore", e)
            throw e
        }
    }
}

