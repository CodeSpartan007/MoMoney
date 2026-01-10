package com.kp.momoney.util

import com.kp.momoney.data.local.CurrencyPreference
import com.kp.momoney.domain.model.Transaction
import java.text.SimpleDateFormat
import java.util.Locale

object CsvUtils {
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    /**
     * Generates a CSV string from a list of transactions
     * Header: "Date,Category,Type,Amount ($code),Note"
     * Amounts are converted using the provided currency exchange rate
     */
    fun generateCsv(transactions: List<Transaction>, currencyPreference: CurrencyPreference): String {
        val csv = StringBuilder()
        
        val rate = currencyPreference.exchangeRate.toDouble()
        val code = currencyPreference.currencyCode
        
        // Add header with currency code
        csv.appendLine("Date,Category,Type,Amount ($code),Note")
        
        // Add rows
        transactions.forEach { transaction ->
            val date = dateFormatter.format(transaction.date)
            val category = escapeCsvField(transaction.categoryName)
            val type = escapeCsvField(transaction.type)
            // Convert amount using exchange rate
            val exportAmount = transaction.amount * rate
            val amount = exportAmount.toString()
            val note = escapeCsvField(transaction.note)
            
            csv.appendLine("$date,$category,$type,$amount,$note")
        }
        
        return csv.toString()
    }
    
    /**
     * Escapes CSV fields by removing or replacing commas and newlines
     */
    private fun escapeCsvField(field: String): String {
        // Remove commas and newlines to avoid breaking CSV format
        return field.replace(",", " ").replace("\n", " ").replace("\r", " ")
    }
}

