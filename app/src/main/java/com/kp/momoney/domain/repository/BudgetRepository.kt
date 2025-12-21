package com.kp.momoney.domain.repository

import com.kp.momoney.data.local.entity.BudgetEntity

interface BudgetRepository {
    suspend fun getBudgetForCategory(categoryId: Int): BudgetEntity?
    suspend fun upsertBudget(budget: BudgetEntity): Long
}

