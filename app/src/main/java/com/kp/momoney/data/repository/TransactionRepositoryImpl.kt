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
        
        val categorySpendingFlow = transactionDao.getCategorySpendingByDateRange(startDate, endDate)
        val budgetsFlow = budgetDao.getAllBudgets()
        val categoriesFlow = categoryRepository.getAllCategories()
        
        return combine(categoriesFlow, budgetsFlow, categorySpendingFlow) { categories, budgets, spending ->
            // Create a map of categoryId to spending
            val spendingMap = spending.associateBy { it.categoryId }
            
            // Create a map of categoryId to budget
            val budgetMap = budgets.associateBy { it.categoryId }
            
            // For each category, create a BudgetState
            categories.map { category ->
                val spent = spendingMap[category.id]?.total ?: 0.0
                val budget = budgetMap[category.id]
                val limit = budget?.limitAmount ?: 0.0
                val percentUsed = if (limit > 0) (spent / limit) * 100.0 else 0.0
                
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

