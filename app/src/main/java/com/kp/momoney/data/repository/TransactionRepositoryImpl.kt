package com.kp.momoney.data.repository

import com.kp.momoney.data.local.dao.CategoryDao
import com.kp.momoney.data.local.dao.TransactionDao
import com.kp.momoney.data.local.entity.TransactionEntity
import com.kp.momoney.data.local.entity.TransactionWithCategory
import com.kp.momoney.domain.model.Transaction
import com.kp.momoney.domain.repository.TransactionRepository
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao
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
}

