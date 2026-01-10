package com.kp.momoney.presentation.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kp.momoney.data.local.entity.BudgetEntity
import com.kp.momoney.data.local.CurrencyPreference
import com.kp.momoney.domain.model.BudgetState
import com.kp.momoney.domain.repository.BudgetRepository
import com.kp.momoney.domain.repository.CurrencyRepository
import com.kp.momoney.domain.repository.TransactionRepository
import com.kp.momoney.util.DateUtils
import com.kp.momoney.util.toBaseCurrency
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val currencyRepository: CurrencyRepository
) : ViewModel() {

    // Get currency preference flow
    private val currencyPreference = currencyRepository.getCurrencyPreference()

    // FIX 1: Use stateIn to automatically manage the connection to the repository.
    // This ensures that as soon as the UI subscribes, the DB is queried.
    val uiState: StateFlow<BudgetUiState> = combine(
        transactionRepository.getBudgetsWithSpending(),
        currencyPreference
    ) { budgets, currency ->
        BudgetUiState(
            budgets = budgets,
            isLoading = false,
            error = null,
            currencyPreference = currency
        )
    }
        .catch { e ->
            emit(BudgetUiState(isLoading = false, error = e.message ?: "Failed to load budgets"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Keeps data alive for 5s during rotation
            initialValue = BudgetUiState(isLoading = true)
        )

    fun updateBudgetLimit(categoryId: Int, limitAmount: Double) {
        viewModelScope.launch {
            try {
                // Get current currency state to convert from user currency to base currency
                val currencyState = currencyPreference.first()
                
                // Convert amount from user currency (e.g., USD) to base currency (KES)
                // The amount comes from the UI in the user's currency, so we need to convert it back
                val amountInBase = limitAmount.toBaseCurrency(currencyState.exchangeRate)
                
                val startDate = DateUtils.getCurrentMonthStart()
                val endDate = DateUtils.getCurrentMonthEnd()

                val existingBudget = budgetRepository.getBudgetForCategory(categoryId)

                val budget = if (existingBudget != null) {
                    existingBudget.copy(limitAmount = amountInBase)
                } else {
                    BudgetEntity(
                        categoryId = categoryId,
                        limitAmount = amountInBase,
                        startDate = startDate,
                        endDate = endDate
                    )
                }

                budgetRepository.upsertBudget(budget)
            } catch (e: Exception) {
                // Since uiState is now a ReadOnly flow from the DB, we can't manually set the error state easily.
                // For now, we just log it or you could use a separate "event" flow for toast messages.
                e.printStackTrace()
            }
        }
    }
}

data class BudgetUiState(
    val budgets: List<BudgetState> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val currencyPreference: CurrencyPreference = CurrencyPreference("KES", "KSh", 1.0f)
)