package com.kp.momoney.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        androidx.room.Index(value = ["category_id"]),
        androidx.room.Index(value = ["firestore_id"], unique = true)
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    
    @ColumnInfo(name = "amount")
    val amount: Double,
    
    @ColumnInfo(name = "date")
    val date: Long, // Timestamp in milliseconds
    
    @ColumnInfo(name = "note")
    val note: String?,
    
    @ColumnInfo(name = "type")
    val type: String, // "Income" or "Expense"
    
    @ColumnInfo(name = "payment_method")
    val paymentMethod: String, // "Cash", "Card", "Transfer"
    
    @ColumnInfo(name = "tags")
    val tags: String?, // JSON string or comma-separated values
    
    @ColumnInfo(name = "category_id")
    val categoryId: Int?,
    
    @ColumnInfo(name = "firestore_id")
    val firestoreId: String? = null // Firestore document ID
)

