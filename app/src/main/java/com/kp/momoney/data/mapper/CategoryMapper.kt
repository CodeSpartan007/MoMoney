package com.kp.momoney.data.mapper

import com.google.firebase.firestore.DocumentSnapshot
import com.kp.momoney.data.local.entity.CategoryEntity
import java.util.UUID

/**
 * Extension function to convert CategoryEntity to Firestore HashMap
 */
fun CategoryEntity.toFirestoreMap(): HashMap<String, Any> {
    val map = hashMapOf<String, Any>()
    
    map["firestoreId"] = firestoreId
    map["name"] = name
    map["type"] = type
    map["colorHex"] = colorHex
    map["iconName"] = iconName
    map["userId"] = userId ?: ""
    
    return map
}

/**
 * Extension function to convert Firestore DocumentSnapshot to CategoryEntity
 */
fun DocumentSnapshot.toCategoryEntity(): CategoryEntity? {
    return try {
        val firestoreId = getString("firestoreId") ?: UUID.randomUUID().toString()
        val name = getString("name") ?: return null
        val type = getString("type") ?: return null
        val colorHex = getString("colorHex") ?: return null
        val iconName = getString("iconName") ?: "spanner" // Default to "spanner" if missing
        val userId = getString("userId")?.takeIf { it.isNotBlank() } // Convert empty string to null
        
        CategoryEntity(
            id = 0, // Room will auto-generate
            name = name,
            type = type,
            colorHex = colorHex,
            iconName = iconName,
            userId = userId,
            firestoreId = firestoreId
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

