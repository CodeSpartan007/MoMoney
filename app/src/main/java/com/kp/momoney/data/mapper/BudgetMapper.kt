package com.kp.momoney.data.mapper

import com.google.firebase.firestore.DocumentSnapshot
import com.kp.momoney.data.local.entity.BudgetEntity
import java.util.UUID

/**
 * Extension function to convert BudgetEntity to Firestore HashMap
 */
fun BudgetEntity.toFirestoreMap(): HashMap<String, Any> {
    val map = hashMapOf<String, Any>()
    
    map["firestoreId"] = firestoreId ?: UUID.randomUUID().toString()
    map["categoryId"] = categoryId
    map["limitAmount"] = limitAmount
    map["startDate"] = startDate
    map["endDate"] = endDate
    
    return map
}

/**
 * Extension function to convert Firestore DocumentSnapshot to BudgetEntity
 */
fun DocumentSnapshot.toBudgetEntity(): BudgetEntity? {
    return try {
        val firestoreId = getString("firestoreId") ?: UUID.randomUUID().toString()
        val categoryId = getLong("categoryId")?.toInt() ?: return null
        val limitAmount = getDouble("limitAmount") ?: return null
        val startDate = getLong("startDate") ?: return null
        val endDate = getLong("endDate") ?: return null
        
        BudgetEntity(
            id = 0, // Room will auto-generate
            categoryId = categoryId,
            limitAmount = limitAmount,
            startDate = startDate,
            endDate = endDate,
            firestoreId = firestoreId
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

