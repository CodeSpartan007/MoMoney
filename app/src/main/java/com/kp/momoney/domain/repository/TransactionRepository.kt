package com.kp.momoney.domain.repository

import com.kp.momoney.domain.model.BudgetState
import com.kp.momoney.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
    suspend fun getTransactionById(id: Long): Transaction?
    suspend fun insertTransaction(transaction: Transaction)
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)
    fun getBudgetsWithSpending(): Flow<List<BudgetState>>
    suspend fun syncTransactions() // Sync transactions from Firestore to Room
}

