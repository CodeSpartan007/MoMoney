package com.kp.momoney.presentation.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kp.momoney.data.local.entity.BudgetEntity
import com.kp.momoney.domain.model.BudgetState
import com.kp.momoney.domain.repository.BudgetRepository
import com.kp.momoney.domain.repository.TransactionRepository
import com.kp.momoney.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()
    
    init {
        loadBudgets()
    }
    
    private fun loadBudgets() {
        transactionRepository.getBudgetsWithSpending()
            .onEach { budgets ->
                _uiState.value = _uiState.value.copy(
                    budgets = budgets,
                    isLoading = false
                )
            }
            .launchIn(viewModelScope)
    }
    
    fun updateBudgetLimit(categoryId: Int, limitAmount: Double) {
        viewModelScope.launch {
            try {
                val startDate = DateUtils.getCurrentMonthStart()
                val endDate = DateUtils.getCurrentMonthEnd()
                
                val existingBudget = budgetRepository.getBudgetForCategory(categoryId)
                
                val budget = if (existingBudget != null) {
                    existingBudget.copy(limitAmount = limitAmount)
                } else {
                    BudgetEntity(
                        categoryId = categoryId,
                        limitAmount = limitAmount,
                        startDate = startDate,
                        endDate = endDate
                    )
                }
                
                budgetRepository.upsertBudget(budget)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update budget"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class BudgetUiState(
    val budgets: List<BudgetState> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

