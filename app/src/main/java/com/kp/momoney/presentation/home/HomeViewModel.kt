package com.kp.momoney.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kp.momoney.domain.model.Transaction
import com.kp.momoney.domain.repository.BudgetRepository
import com.kp.momoney.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val transactions: List<Transaction> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val isLoading: Boolean = true // Added loading state just in case
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    init {
        // Sync data from Firestore when ViewModel is created
        viewModelScope.launch {
            try {
                // Sync transactions first, then budgets
                transactionRepository.syncTransactions()
                budgetRepository.syncBudgets()
            } catch (e: Exception) {
                // Log error but don't crash - app can work offline
                e.printStackTrace()
            }
        }
    }

    // TRANSFORMING THE FLOW DIRECTLY
    val uiState: StateFlow<HomeUiState> = transactionRepository.getAllTransactions()
        .map { transactions ->
            val income = transactions
                .filter { it.type.equals("Income", ignoreCase = true) }
                .sumOf { it.amount }

            val expense = transactions
                .filter { it.type.equals("Expense", ignoreCase = true) }
                .sumOf { it.amount }

            HomeUiState(
                transactions = transactions,
                totalIncome = income,
                totalExpense = expense,
                isLoading = false
            )
        }
        .catch { exception ->
            exception.printStackTrace()
            // In a real app, you'd emit an error state here
            emit(HomeUiState(isLoading = false))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // <--- THE MAGIC FIX
            initialValue = HomeUiState(isLoading = true)
        )
}