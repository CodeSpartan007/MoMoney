package com.kp.momoney.di

import com.kp.momoney.data.repository.CategoryRepositoryImpl
import com.kp.momoney.data.repository.TransactionRepositoryImpl
import com.kp.momoney.domain.repository.CategoryRepository
import com.kp.momoney.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        impl: TransactionRepositoryImpl
    ): TransactionRepository
    
    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        impl: CategoryRepositoryImpl
    ): CategoryRepository
}

