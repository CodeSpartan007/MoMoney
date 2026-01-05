package com.kp.momoney.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "categories",
    indices = [
        Index(value = ["firestore_id"], unique = true),
        Index(value = ["user_id"])
    ]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,
    
    @ColumnInfo(name = "firestore_id")
    val firestoreId: String = UUID.randomUUID().toString(), // Unique ID for syncing
    
    @ColumnInfo(name = "user_id")
    val userId: String? = null, // NULL = Default System Category, VALUE = User's Custom Category
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "type")
    val type: String, // "Income" or "Expense"
    
    @ColumnInfo(name = "icon_name")
    val iconName: String,
    
    @ColumnInfo(name = "color_hex")
    val colorHex: String
)

