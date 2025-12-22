package com.kp.momoney.data.repository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kp.momoney.data.local.dao.BudgetDao
import com.kp.momoney.data.local.dao.CategoryDao
import com.kp.momoney.data.local.dao.TransactionDao
import com.kp.momoney.data.local.entity.TransactionEntity
import com.kp.momoney.data.local.entity.TransactionWithCategory
import com.kp.momoney.data.mapper.toFirestoreMap
import com.kp.momoney.data.mapper.toFirestoreTransactionData
import com.kp.momoney.domain.model.BudgetState
import com.kp.momoney.domain.model.Category
import com.kp.momoney.domain.model.Transaction
import com.kp.momoney.domain.repository.CategoryRepository
import com.kp.momoney.domain.repository.TransactionRepository
import com.kp.momoney.util.DateUtils
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.tasks.await
import android.util.Log
import kotlinx.coroutines.flow.map

class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val budgetDao: BudgetDao,
    private val categoryRepository: CategoryRepository,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : TransactionRepository {
    
    private val currentUserId: String?
        get() = auth.currentUser?.uid
    
    override fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions().map { transactions ->
            transactions.map { it.toDomain() }
        }
    }
    
    override suspend fun insertTransaction(transaction: Transaction) {
        val categoryId = resolveCategoryId(transaction)
        val firestoreId = UUID.randomUUID().toString()
        val entity = transaction.toEntity(categoryId, firestoreId)
        
        // Save to Room first
        transactionDao.insertTransaction(entity)
        
        // Sync to Firestore if user is authenticated
        currentUserId?.let { userId ->
            try {
                val firestoreMap = transaction.toFirestoreMap(firestoreId)
                firestore.collection("users")
                    .document(userId)
                    .collection("transactions")
                    .document(firestoreId)
                    .set(firestoreMap)
                    .await()
                Log.d("TransactionRepo", "Transaction synced to Firestore: $firestoreId")
            } catch (e: Exception) {
                Log.e("TransactionRepo", "Failed to sync transaction to Firestore", e)
                // Continue even if Firestore sync fails - local data is saved
            }
        }
    }
    
    override suspend fun deleteTransaction(transaction: Transaction) {
        // Get the entity to retrieve firestoreId
        val entity = transactionDao.getTransactionById(transaction.id)
        
        // Delete from Room
        transactionDao.deleteTransactionById(transaction.id)
        
        // Delete from Firestore if user is authenticated and entity has firestoreId
        currentUserId?.let { userId ->
            entity?.firestoreId?.let { firestoreId ->
                try {
                    firestore.collection("users")
                        .document(userId)
                        .collection("transactions")
                        .document(firestoreId)
                        .delete()
                        .await()
                    Log.d("TransactionRepo", "Transaction deleted from Firestore: $firestoreId")
                } catch (e: Exception) {
                    Log.e("TransactionRepo", "Failed to delete transaction from Firestore", e)
                    // Continue even if Firestore delete fails - local data is deleted
                }
            }
        }
    }
    
    private suspend fun resolveCategoryId(transaction: Transaction): Int? {
        if (transaction.categoryName.isBlank()) return null
        return resolveCategoryIdByName(transaction.categoryName)
    }
    
    private suspend fun resolveCategoryIdByName(categoryName: String): Int? {
        return categoryDao.getCategoryByName(categoryName)?.id
    }
    
    private fun TransactionWithCategory.toDomain(): Transaction {
        val category = category
        return Transaction(
            id = transaction.id,
            amount = transaction.amount,
            date = Date(transaction.date),
            note = transaction.note.orEmpty(),
            type = transaction.type,
            categoryName = category?.name.orEmpty(),
            categoryColor = category?.colorHex.orEmpty(),
            categoryIcon = category?.iconName.orEmpty()
        )
    }
    
    private fun Transaction.toEntity(categoryId: Int?, firestoreId: String? = null): TransactionEntity {
        return TransactionEntity(
            id = id,
            amount = amount,
            date = date.time,
            note = note,
            type = type,
            paymentMethod = "",
            tags = null,
            categoryId = categoryId,
            firestoreId = firestoreId
        )
    }
    
    override suspend fun syncTransactions() {
        val userId = currentUserId ?: run {
            Log.d("TransactionRepo", "No authenticated user, skipping sync")
            return
        }
        
        try {
            Log.d("TransactionRepo", "Starting transaction sync from Firestore")
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("transactions")
                .get()
                .await()
            
            val firestoreTransactions = snapshot.documents.mapNotNull { document ->
                document.toFirestoreTransactionData()
            }
            
            Log.d("TransactionRepo", "Fetched ${firestoreTransactions.size} transactions from Firestore")
            
            // Resolve category IDs for each transaction
            val transactionsWithCategories = firestoreTransactions.map { data ->
                val categoryId = data.categoryName?.let { categoryName ->
                    if (categoryName.isNotBlank()) {
                        resolveCategoryIdByName(categoryName)
                    } else {
                        null
                    }
                }
                
                data.entity.copy(categoryId = categoryId)
            }
            
            // Insert/update transactions in Room using REPLACE strategy
            transactionsWithCategories.forEach { entity ->
                // Check if transaction with this firestoreId already exists
                val existing = entity.firestoreId?.let { 
                    transactionDao.getTransactionByFirestoreId(it) 
                }
                
                if (existing != null) {
                    // Update existing transaction, preserving Room ID
                    val updatedEntity = entity.copy(id = existing.id)
                    transactionDao.upsertTransaction(updatedEntity)
                } else {
                    // Insert new transaction
                    transactionDao.upsertTransaction(entity)
                }
            }
            
            Log.d("TransactionRepo", "Successfully synced ${transactionsWithCategories.size} transactions to Room")
        } catch (e: Exception) {
            Log.e("TransactionRepo", "Failed to sync transactions from Firestore", e)
            throw e
        }
    }

    override fun getBudgetsWithSpending(): Flow<List<BudgetState>> {
        val startDate = DateUtils.getCurrentMonthStart()
        val endDate = DateUtils.getCurrentMonthEnd()

        // 1. Log when we start requesting data
        Log.d("BudgetRepo", "Fetching data for: $startDate to $endDate")

        val categorySpendingFlow = transactionDao.getCategorySpendingByDateRange(startDate, endDate)
            .onEach { Log.d("BudgetRepo", "Spending loaded: ${it.size} items") }

        val budgetsFlow = budgetDao.getAllBudgets()
            .onEach { Log.d("BudgetRepo", "Budgets loaded: ${it.size} items") }

        val categoriesFlow = categoryRepository.getAllCategories()
            .onEach { Log.d("BudgetRepo", "Categories loaded: ${it.size} items") }

        return combine(categoriesFlow, budgetsFlow, categorySpendingFlow) { categories, budgets, spending ->
            Log.d("BudgetRepo", "Combining data...") // If you don't see this log, one of the flows above is stuck!

            // Only budgets for expense categories should be shown
            val expenseCategories = categories.filter { it.type.equals("Expense", ignoreCase = true) }

            val spendingMap = spending.associateBy { it.categoryId }
            val budgetMap = budgets.associateBy { it.categoryId }

            expenseCategories.map { category ->
                val spent = spendingMap[category.id]?.total ?: 0.0
                val budget = budgetMap[category.id]
                val limit = budget?.limitAmount ?: 0.0

                // Fix: Ensure we don't divide by zero, and cast to Float if needed for UI
                val percentUsed = if (limit > 0.0) (spent / limit) * 100.0 else 0.0

                BudgetState(
                    category = category,
                    spentAmount = spent,
                    limitAmount = limit,
                    percentUsed = percentUsed
                )
            }.sortedBy { it.category.name }
        }
    }
}

