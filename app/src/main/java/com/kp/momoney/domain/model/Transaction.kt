package com.kp.momoney.domain.model

import java.util.Date

data class Transaction(
    val id: Long,
    val amount: Double,
    val date: Date,
    val note: String,
    val type: String,
    val categoryName: String,
    val categoryColor: String,
    val categoryIcon: String
)

