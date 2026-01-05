package com.kp.momoney.domain.repository

import com.kp.momoney.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAllCategories(): Flow<List<Category>>
    suspend fun getCategoryById(id: Int): Category?
    suspend fun addUserCategory(name: String, type: String, color: String)
    suspend fun syncCategories()
    suspend fun deleteCategory(categoryId: Int)
}

