package com.kp.momoney.domain.model

enum class Recurrence {
    NEVER,
    WEEKLY,
    MONTHLY;
    
    fun toDisplayString(): String {
        return when (this) {
            NEVER -> "Never"
            WEEKLY -> "Weekly"
            MONTHLY -> "Monthly"
        }
    }
    
    companion object {
        fun fromString(value: String?): Recurrence {
            return when (value?.uppercase()) {
                "WEEKLY" -> WEEKLY
                "MONTHLY" -> MONTHLY
                else -> NEVER
            }
        }
    }
}

