package com.kp.momoney.domain.repository

import com.kp.momoney.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAllCategories(): Flow<List<Category>>
    suspend fun getCategoryById(id: Int): Category?
}

