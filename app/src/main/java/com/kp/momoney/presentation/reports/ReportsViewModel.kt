package com.kp.momoney.presentation.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kp.momoney.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class CategoryPercentage(
    val categoryName: String,
    val colorHex: String,
    val totalAmount: Double,
    val percentage: Float
)

data class ReportsUiState(
    val items: List<CategoryPercentage> = emptyList(),
    val totalExpense: Double = 0.0,
    val isLoading: Boolean = true
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        observeTransactions()
    }

    private fun observeTransactions() {
        viewModelScope.launch {
            transactionRepository.getAllTransactions().collect { transactions ->
                val expenseTransactions = transactions.filter {
                    it.type.equals("Expense", ignoreCase = true)
                }

                val totalExpense = expenseTransactions.sumOf { it.amount }

                if (totalExpense <= 0.0) {
                    _uiState.value = ReportsUiState(
                        items = emptyList(),
                        totalExpense = 0.0,
                        isLoading = false
                    )
                    return@collect
                }

                val grouped = expenseTransactions
                    .groupBy { it.categoryName.ifBlank { "Uncategorized" } }

                val items = grouped.map { (categoryName, list) ->
                    val total = list.sumOf { it.amount }
                    val rawPercentage = if (totalExpense > 0.0) {
                        (total / totalExpense * 100.0)
                    } else {
                        0.0
                    }

                    val colorHex = list.firstOrNull()?.categoryColor.orEmpty()

                    CategoryPercentage(
                        categoryName = categoryName,
                        colorHex = colorHex,
                        totalAmount = total,
                        percentage = rawPercentage.toFloat()
                    )
                }.sortedByDescending { it.totalAmount }

                _uiState.value = ReportsUiState(
                    items = items,
                    totalExpense = totalExpense,
                    isLoading = false
                )
            }
        }
    }
}


