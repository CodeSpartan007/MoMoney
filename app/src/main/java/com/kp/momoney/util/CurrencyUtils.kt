package com.kp.momoney.util

import java.text.NumberFormat
import java.util.Locale

fun Double.toCurrency(): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    formatter.minimumFractionDigits = 2
    formatter.maximumFractionDigits = 2
    return "Ksh ${formatter.format(this)}"
}

/**
 * Converts a Double amount to a formatted currency string using the provided exchange rate and symbol.
 * 
 * @param rate The exchange rate to apply (e.g., 0.007 for KES to USD)
 * @param symbol The currency symbol to display (e.g., "$", "€", "KSh")
 * @return Formatted currency string (e.g., "$ 15.50", "€ 12.30")
 */
fun Double.toCurrency(rate: Float, symbol: String): String {
    val convertedAmount = this * rate
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    formatter.minimumFractionDigits = 2
    formatter.maximumFractionDigits = 2
    return "$symbol ${formatter.format(convertedAmount)}"
}

/**
 * Converts a Double amount from the selected currency back to base currency (KES).
 * 
 * @param rate The exchange rate from base currency to selected currency (e.g., 0.007 for KES to USD)
 * @return The amount in base currency (KES). If rate is 0 or invalid, returns the original amount.
 * 
 * Example: If rate is 0.007 (1 KES = 0.007 USD), then 10.0.toBaseCurrency(0.007f) = 1428.57 KES
 *          This means: 10 USD / 0.007 = 1428.57 KES
 */
fun Double.toBaseCurrency(rate: Float): Double {
    return if (rate > 0) {
        this / rate
    } else {
        // Safety check: if rate is 0 or negative, default to 1.0 (no conversion)
        this
    }
}
