package com.kp.momoney.data.repository

import com.kp.momoney.data.local.dao.BudgetDao
import com.kp.momoney.data.local.entity.BudgetEntity
import com.kp.momoney.domain.repository.BudgetRepository
import javax.inject.Inject

class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao
) : BudgetRepository {
    
    override suspend fun getBudgetForCategory(categoryId: Int): BudgetEntity? {
        return budgetDao.getBudgetForCategory(categoryId)
    }
    
    override suspend fun upsertBudget(budget: BudgetEntity): Long {
        return budgetDao.upsertBudget(budget)
    }
}

