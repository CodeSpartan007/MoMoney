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
import com.kp.momoney.domain.model.Recurrence
import com.kp.momoney.domain.model.Transaction
import com.kp.momoney.domain.repository.CategoryRepository
import com.kp.momoney.domain.repository.TransactionRepository
import com.kp.momoney.util.DateUtils
import java.util.Calendar
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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
    
    override fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>> {
        val transactionsFlow = transactionDao.getTransactionsByDateRange(startDate, endDate)
        val categoriesFlow = categoryRepository.getAllCategories()
        
        return combine(transactionsFlow, categoriesFlow) { transactions, categories ->
            val categoryMap = categories.associateBy { it.id }
            transactions.map { entity ->
                val category = entity.categoryId?.let { categoryMap[it] }
                Transaction(
                    id = entity.id,
                    amount = entity.amount,
                    date = Date(entity.date),
                    note = entity.note.orEmpty(),
                    type = entity.type,
                    categoryId = entity.categoryId,
                    categoryName = category?.name.orEmpty(),
                    categoryColor = category?.color.orEmpty(),
                    categoryIcon = category?.icon.orEmpty(),
                    recurrence = Recurrence.fromString(entity.recurrence)
                )
            }
        }
    }
    
    override suspend fun getTransactionById(id: Long): Transaction? {
        val entity = transactionDao.getTransactionById(id) ?: return null
        // Need to get category info
        val category = entity.categoryId?.let { categoryDao.getCategoryById(it) }
        return Transaction(
            id = entity.id,
            amount = entity.amount,
            date = Date(entity.date),
            note = entity.note.orEmpty(),
            type = entity.type,
            categoryId = entity.categoryId,
            categoryName = category?.name.orEmpty(),
            categoryColor = category?.colorHex.orEmpty(),
            categoryIcon = category?.iconName.orEmpty(),
            recurrence = Recurrence.fromString(entity.recurrence)
        )
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
    
    override suspend fun updateTransaction(transaction: Transaction) {
        // Get existing entity to preserve firestoreId
        val existingEntity = transactionDao.getTransactionById(transaction.id)
        val firestoreId = existingEntity?.firestoreId ?: UUID.randomUUID().toString()
        
        val categoryId = resolveCategoryId(transaction)
        val entity = transaction.toEntity(categoryId, firestoreId).copy(
            id = transaction.id,
            firestoreId = firestoreId
        )
        
        // Update in Room
        transactionDao.updateTransaction(entity)
        
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
                Log.d("TransactionRepo", "Transaction updated in Firestore: $firestoreId")
            } catch (e: Exception) {
                Log.e("TransactionRepo", "Failed to update transaction in Firestore", e)
                // Continue even if Firestore sync fails - local data is updated
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
            categoryId = transaction.categoryId,
            categoryName = category?.name.orEmpty(),
            categoryColor = category?.colorHex.orEmpty(),
            categoryIcon = category?.iconName.orEmpty(),
            recurrence = Recurrence.fromString(transaction.recurrence)
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
            firestoreId = firestoreId,
            recurrence = recurrence.name
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
    
    override suspend fun getCategorySpendingForCategory(categoryId: Int, startDate: Long, endDate: Long): Double {
        return transactionDao.getCategorySpendingForCategory(categoryId, startDate, endDate)
    }
    
    override suspend fun processRecurringTransactions() {
        try {
            Log.d("RecurrenceWorker", "Starting to process recurring transactions")
            val recurringEntities = transactionDao.getRecurringTransactions()
            Log.d("RecurrenceWorker", "Found ${recurringEntities.size} recurring transactions")
            
            val currentTime = System.currentTimeMillis()
            val userId = currentUserId
            
            recurringEntities.forEach { originalEntity ->
                val recurrence = Recurrence.fromString(originalEntity.recurrence)
                if (recurrence == Recurrence.NEVER) return@forEach
                
                val originalDate = originalEntity.date
                val nextDueDate = calculateNextDate(originalDate, recurrence)
                
                // Check if the next due date has passed (transaction is due)
                if (nextDueDate <= currentTime) {
                    Log.d("RecurrenceWorker", "Processing recurring transaction ID: ${originalEntity.id}, Original date: $originalDate, Next due date: $nextDueDate")
                    
                    // Get category info for the history record
                    val category = originalEntity.categoryId?.let { categoryDao.getCategoryById(it) }
                    val note = originalEntity.note ?: ""
                    
                    // Step A: Idempotency Check - Check if history transaction already exists
                    val existingHistory = transactionDao.findDuplicateHistoryTransaction(
                        note = note,
                        amount = originalEntity.amount,
                        date = originalDate
                    )
                    
                    if (existingHistory != null) {
                        Log.d("RecurrenceWorker", "History transaction already exists for transaction ID: ${originalEntity.id}, skipping duplicate creation")
                        // Still need to update the parent transaction if it's not already updated
                        val currentEntity = transactionDao.getTransactionById(originalEntity.id)
                        if (currentEntity != null && currentEntity.date == originalDate) {
                            // Parent hasn't been updated yet, update it now
                            val updatedEntity = originalEntity.copy(date = nextDueDate)
                            transactionDao.updateTransaction(updatedEntity)
                            
                            // Immediately sync parent update to Firestore
                            userId?.let { uid ->
                                originalEntity.firestoreId?.let { firestoreId ->
                                    try {
                                        val parentTransaction = Transaction(
                                            id = updatedEntity.id,
                                            amount = updatedEntity.amount,
                                            date = Date(updatedEntity.date),
                                            note = note,
                                            type = updatedEntity.type,
                                            categoryId = updatedEntity.categoryId,
                                            categoryName = category?.name.orEmpty(),
                                            categoryColor = category?.colorHex.orEmpty(),
                                            categoryIcon = category?.iconName.orEmpty(),
                                            recurrence = Recurrence.fromString(updatedEntity.recurrence)
                                        )
                                        val firestoreMap = parentTransaction.toFirestoreMap(firestoreId)
                                        firestore.collection("users")
                                            .document(uid)
                                            .collection("transactions")
                                            .document(firestoreId)
                                            .set(firestoreMap)
                                            .await()
                                        Log.d("RecurrenceWorker", "Synced updated parent transaction to Firestore: $firestoreId")
                                    } catch (e: Exception) {
                                        Log.e("RecurrenceWorker", "Failed to sync parent transaction to Firestore", e)
                                    }
                                }
                            }
                        }
                        return@forEach
                    }
                    
                    // Step B: Create History Record (Date = Old Date, Recurrence = NEVER)
                    val historyFirestoreId = UUID.randomUUID().toString()
                    val historyRecord = Transaction(
                        id = 0, // Room will generate a new ID
                        amount = originalEntity.amount,
                        date = Date(originalDate), // The OLD date
                        note = note,
                        type = originalEntity.type,
                        categoryId = originalEntity.categoryId,
                        categoryName = category?.name.orEmpty(),
                        categoryColor = category?.colorHex.orEmpty(),
                        categoryIcon = category?.iconName.orEmpty(),
                        recurrence = Recurrence.NEVER // History doesn't recur
                    )
                    
                    // Step C: Update Parent Transaction (Date = New Date, Recurrence = MONTHLY/WEEKLY)
                    val updatedEntity = originalEntity.copy(date = nextDueDate)
                    
                    // Step D: Save to Room first
                    val categoryId = resolveCategoryId(historyRecord)
                    val historyEntity = historyRecord.toEntity(categoryId, historyFirestoreId)
                    transactionDao.insertTransaction(historyEntity)
                    transactionDao.updateTransaction(updatedEntity)
                    
                    Log.d("RecurrenceWorker", "Created history record for original transaction ID: ${originalEntity.id} with date: $originalDate")
                    Log.d("RecurrenceWorker", "Updated original transaction ID: ${originalEntity.id} to next due date: $nextDueDate")
                    
                    // Step E: Immediately sync BOTH records to Firestore
                    userId?.let { uid ->
                        try {
                            // Sync history record
                            val historyFirestoreMap = historyRecord.toFirestoreMap(historyFirestoreId)
                            firestore.collection("users")
                                .document(uid)
                                .collection("transactions")
                                .document(historyFirestoreId)
                                .set(historyFirestoreMap)
                                .await()
                            Log.d("RecurrenceWorker", "Synced history transaction to Firestore: $historyFirestoreId")
                            
                            // Sync updated parent transaction
                            originalEntity.firestoreId?.let { parentFirestoreId ->
                                val parentTransaction = Transaction(
                                    id = updatedEntity.id,
                                    amount = updatedEntity.amount,
                                    date = Date(updatedEntity.date),
                                    note = note,
                                    type = updatedEntity.type,
                                    categoryId = updatedEntity.categoryId,
                                    categoryName = category?.name.orEmpty(),
                                    categoryColor = category?.colorHex.orEmpty(),
                                    categoryIcon = category?.iconName.orEmpty(),
                                    recurrence = Recurrence.fromString(updatedEntity.recurrence)
                                )
                                val parentFirestoreMap = parentTransaction.toFirestoreMap(parentFirestoreId)
                                firestore.collection("users")
                                    .document(uid)
                                    .collection("transactions")
                                    .document(parentFirestoreId)
                                    .set(parentFirestoreMap)
                                    .await()
                                Log.d("RecurrenceWorker", "Synced updated parent transaction to Firestore: $parentFirestoreId")
                            } ?: run {
                                Log.w("RecurrenceWorker", "Parent transaction has no firestoreId, cannot sync to Firestore")
                            }
                        } catch (e: Exception) {
                            Log.e("RecurrenceWorker", "Failed to sync transactions to Firestore", e)
                            // Continue even if Firestore sync fails - local data is saved
                        }
                    } ?: run {
                        Log.w("RecurrenceWorker", "No authenticated user, skipping Firestore sync")
                    }
                }
            }
            
            Log.d("RecurrenceWorker", "Finished processing recurring transactions")
        } catch (e: Exception) {
            Log.e("RecurrenceWorker", "Error processing recurring transactions", e)
            // Don't throw - we don't want to crash the app if recurrence processing fails
        }
    }
    
    /**
     * Calculate the next occurrence date based on the recurrence type
     */
    private fun calculateNextDate(originalDate: Long, recurrence: Recurrence): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = originalDate
        }
        
        when (recurrence) {
            Recurrence.WEEKLY -> {
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
            }
            Recurrence.MONTHLY -> {
                calendar.add(Calendar.MONTH, 1)
            }
            Recurrence.NEVER -> {
                // Should not happen, but return original date
                return originalDate
            }
        }
        
        return calendar.timeInMillis
    }
}

