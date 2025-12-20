package com.kp.momoney.data.repository

import com.kp.momoney.data.local.dao.CategoryDao
import com.kp.momoney.domain.model.Category
import com.kp.momoney.domain.repository.CategoryRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {
    
    override fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories().map { categories ->
            categories.map { it.toDomain() }
        }
    }
    
    override suspend fun getCategoryById(id: Int): Category? {
        return categoryDao.getCategoryById(id)?.toDomain()
    }
    
    private fun com.kp.momoney.data.local.entity.CategoryEntity.toDomain(): Category {
        return Category(
            id = id,
            name = name,
            icon = iconName,
            color = colorHex,
            type = type
        )
    }
}

