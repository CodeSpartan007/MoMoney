package com.kp.momoney.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "icon_name")
    val iconName: String,
    
    @ColumnInfo(name = "color_hex")
    val colorHex: String,
    
    @ColumnInfo(name = "type")
    val type: String, // "Income" or "Expense"
    
    @ColumnInfo(name = "user_id")
    val userId: String? = null, // Null = System Default, Value = Specific User
    
    @ColumnInfo(name = "firestore_id")
    val firestoreId: String = UUID.randomUUID().toString() // Default to UUID string if creating new
)

