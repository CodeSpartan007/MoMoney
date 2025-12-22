package com.kp.momoney.data.mapper

import com.google.firebase.firestore.DocumentSnapshot
import com.kp.momoney.data.local.entity.TransactionEntity
import com.kp.momoney.domain.model.Transaction
import java.util.Date
import java.util.UUID

/**
 * Extension function to convert Domain Transaction to Firestore HashMap
 */
fun Transaction.toFirestoreMap(firestoreId: String? = null): HashMap<String, Any> {
    val map = hashMapOf<String, Any>()
    
    // Use provided firestoreId or generate new one
    map["firestoreId"] = firestoreId ?: UUID.randomUUID().toString()
    map["amount"] = amount
    map["date"] = date.time // Store as Long timestamp
    map["note"] = note
    map["type"] = type
    map["categoryName"] = categoryName
    map["categoryColor"] = categoryColor
    map["categoryIcon"] = categoryIcon
    map["paymentMethod"] = "" // Default empty, can be extended later
    map["tags"] = emptyList<String>() // Default empty list
    
    return map
}

/**
 * Data class to hold Firestore transaction data with category name
 */
data class FirestoreTransactionData(
    val entity: TransactionEntity,
    val categoryName: String?
)

/**
 * Extension function to convert Firestore DocumentSnapshot to FirestoreTransactionData
 * Returns both Entity and categoryName for later resolution
 */
fun DocumentSnapshot.toFirestoreTransactionData(): FirestoreTransactionData? {
    return try {
        val firestoreId = getString("firestoreId") ?: UUID.randomUUID().toString()
        val amount = getDouble("amount") ?: return null
        val dateTimestamp = getLong("date") ?: return null
        val note = getString("note")
        val type = getString("type") ?: return null
        val paymentMethod = getString("paymentMethod") ?: ""
        val categoryName = getString("categoryName")
        
        // Handle tags - can be List<String> or null
        val tagsList = get("tags") as? List<*>
        val tagsString = if (tagsList.isNullOrEmpty()) {
            null
        } else {
            tagsList.joinToString(",") { it.toString() }
        }
        
        val entity = TransactionEntity(
            id = 0, // Room will auto-generate
            amount = amount,
            date = dateTimestamp,
            note = note,
            type = type,
            paymentMethod = paymentMethod,
            tags = tagsString,
            categoryId = null, // Will be resolved from categoryName
            firestoreId = firestoreId
        )
        
        FirestoreTransactionData(entity, categoryName)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Extension function to convert TransactionEntity to Firestore HashMap
 * (Used when syncing existing Room data to Firestore)
 */
fun TransactionEntity.toFirestoreMap(): HashMap<String, Any> {
    val map = hashMapOf<String, Any>()
    
    map["firestoreId"] = firestoreId ?: UUID.randomUUID().toString()
    map["amount"] = amount
    map["date"] = date
    map["note"] = note ?: ""
    map["type"] = type
    map["paymentMethod"] = paymentMethod
    map["tags"] = tags?.split(",")?.filter { it.isNotBlank() } ?: emptyList<String>()
    
    // Note: categoryName, categoryColor, categoryIcon are not stored in Entity
    // They are resolved from CategoryEntity via join. For Firestore, we'd need to
    // either store them or resolve them separately. For now, using empty strings.
    map["categoryName"] = ""
    map["categoryColor"] = ""
    map["categoryIcon"] = ""
    
    return map
}

