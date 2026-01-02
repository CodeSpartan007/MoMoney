package com.kp.momoney.util

import com.kp.momoney.domain.model.Transaction
import java.text.SimpleDateFormat
import java.util.Locale

object CsvUtils {
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    /**
     * Generates a CSV string from a list of transactions
     * Header: "Date,Category,Type,Amount,Note"
     */
    fun generateCsv(transactions: List<Transaction>): String {
        val csv = StringBuilder()
        
        // Add header
        csv.appendLine("Date,Category,Type,Amount,Note")
        
        // Add rows
        transactions.forEach { transaction ->
            val date = dateFormatter.format(transaction.date)
            val category = escapeCsvField(transaction.categoryName)
            val type = escapeCsvField(transaction.type)
            val amount = transaction.amount.toString()
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

