package com.kp.momoney.data.repository
import com.kp.momoney.data.local.dao.BudgetDao
import com.kp.momoney.data.local.dao.CategoryDao
import com.kp.momoney.data.local.dao.TransactionDao
import com.kp.momoney.data.local.entity.TransactionEntity
import com.kp.momoney.data.local.entity.TransactionWithCategory
import com.kp.momoney.domain.model.BudgetState
import com.kp.momoney.domain.model.Category
import com.kp.momoney.domain.model.Transaction
import com.kp.momoney.domain.repository.CategoryRepository
import com.kp.momoney.domain.repository.TransactionRepository
import com.kp.momoney.util.DateUtils
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import android.util.Log
import kotlinx.coroutines.flow.map

class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val budgetDao: BudgetDao,
    private val categoryRepository: CategoryRepository
) : TransactionRepository {
    
    override fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions().map { transactions ->
            transactions.map { it.toDomain() }
        }
    }
    
    override suspend fun insertTransaction(transaction: Transaction) {
        val categoryId = resolveCategoryId(transaction)
        val entity = transaction.toEntity(categoryId)
        transactionDao.insertTransaction(entity)
    }
    
    override suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransactionById(transaction.id)
    }
    
    private suspend fun resolveCategoryId(transaction: Transaction): Int? {
        if (transaction.categoryName.isBlank()) return null
        return categoryDao.getCategoryByName(transaction.categoryName)?.id
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
    
    private fun Transaction.toEntity(categoryId: Int?): TransactionEntity {
        return TransactionEntity(
            id = id,
            amount = amount,
            date = date.time,
            note = note,
            type = type,
            paymentMethod = "",
            tags = null,
            categoryId = categoryId
        )
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

            val spendingMap = spending.associateBy { it.categoryId }
            val budgetMap = budgets.associateBy { it.categoryId }

            categories.map { category ->
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

