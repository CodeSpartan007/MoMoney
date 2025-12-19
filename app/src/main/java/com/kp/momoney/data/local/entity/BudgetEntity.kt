package com.kp.momoney.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "budgets",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    
    @ColumnInfo(name = "category_id")
    val categoryId: Int,
    
    @ColumnInfo(name = "limit_amount")
    val limitAmount: Double,
    
    @ColumnInfo(name = "start_date")
    val startDate: Long, // Timestamp in milliseconds
    
    @ColumnInfo(name = "end_date")
    val endDate: Long // Timestamp in milliseconds
)

