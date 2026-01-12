package com.kp.momoney.data.local.entity

import androidx.room.Embedded
data class TransactionWithCategory(
    @Embedded val transaction: TransactionEntity,
    @Embedded(prefix = "cat_") val category: CategoryEntity?
)

