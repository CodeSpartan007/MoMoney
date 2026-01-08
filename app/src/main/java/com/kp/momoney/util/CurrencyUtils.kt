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
