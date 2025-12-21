package com.kp.momoney.domain.model

data class BudgetState(
    val category: Category,
    val spentAmount: Double,
    val limitAmount: Double,
    val percentUsed: Double
) {
    val isOverBudget: Boolean
        get() = percentUsed >= 100.0
    
    val isNearLimit: Boolean
        get() = percentUsed >= 90.0 && percentUsed < 100.0
}

