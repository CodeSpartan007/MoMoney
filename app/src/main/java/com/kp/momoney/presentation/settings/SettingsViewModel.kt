package com.kp.momoney.presentation.settings

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.kp.momoney.domain.repository.TransactionRepository
import com.kp.momoney.util.CsvUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    val userEmail: String
        get() = firebaseAuth.currentUser?.email ?: ""

    fun exportData(context: Context) {
        viewModelScope.launch {
            try {
                // Fetch all transactions
                val transactions = transactionRepository.getAllTransactions().first()
                
                // Generate CSV string
                val csvContent = CsvUtils.generateCsv(transactions)
                
                // Write to file
                val file = File(context.cacheDir, "finance_export.csv")
                file.writeText(csvContent)
                
                // Get URI using FileProvider
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                
                // Create intent to share
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                // Launch share chooser
                context.startActivity(Intent.createChooser(intent, "Export via"))
            } catch (e: Exception) {
                // Error handling - could show a toast or snackbar
                e.printStackTrace()
            }
        }
    }
}

