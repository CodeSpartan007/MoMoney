package com.kp.momoney.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kp.momoney.domain.model.Transaction
import com.kp.momoney.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val transactions: List<Transaction> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        collectTransactions()
    }

    private fun collectTransactions() {
        viewModelScope.launch {
            transactionRepository.getAllTransactions()
                .catch { exception ->
                    // Handle error - could emit error state
                    exception.printStackTrace()
                }
                .collect { transactions ->
                    val income = transactions
                        .filter { it.type.equals("Income", ignoreCase = true) }
                        .sumOf { it.amount }
                    
                    val expense = transactions
                        .filter { it.type.equals("Expense", ignoreCase = true) }
                        .sumOf { it.amount }
                    
                    _uiState.value = HomeUiState(
                        transactions = transactions,
                        totalIncome = income,
                        totalExpense = expense
                    )
                }
        }
    }
}

