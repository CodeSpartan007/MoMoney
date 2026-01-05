package com.kp.momoney.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kp.momoney.domain.model.Transaction
import com.kp.momoney.domain.repository.BudgetRepository
import com.kp.momoney.domain.repository.CategoryRepository
import com.kp.momoney.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

data class HomeUiState(
    val transactions: List<Transaction> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val isLoading: Boolean = true // Added loading state just in case
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    val categoryRepository: CategoryRepository
) : ViewModel() {

    // Search and Filter State
    val searchQuery = MutableStateFlow("")
    val filterDateRange = MutableStateFlow<Pair<Long, Long>?>(null)
    val filterCategories = MutableStateFlow<List<Int>>(emptyList())
    val filterType = MutableStateFlow<String?>(null)
    val filterMinAmount = MutableStateFlow<String?>(null)
    val filterMaxAmount = MutableStateFlow<String?>(null)
    
    // Selection State
    val selectedTransactionId = MutableStateFlow<Long?>(null)
    val isLoadingAction = MutableStateFlow(false)

    init {
        // Sync data from Firestore when ViewModel is created
        viewModelScope.launch {
            try {
                // Sync transactions, budgets, and categories on startup
                transactionRepository.syncTransactions()
                budgetRepository.syncBudgets()
                categoryRepository.syncCategories()
            } catch (e: Exception) {
                // Log error but don't crash - app can work offline
                e.printStackTrace()
            }
        }
    }

    // Get all transactions flow
    private val allTransactions = transactionRepository.getAllTransactions()

    // Filtered transactions flow that applies all filters
    // Using nested combine since combine supports max 5 flows directly
    private val filteredTransactions = combine(
        allTransactions,
        searchQuery,
        filterDateRange,
        filterCategories,
        filterType
    ) { transactions, query, dateRange, categories, type ->
        combine(filterMinAmount, filterMaxAmount) { minAmount, maxAmount ->
            transactions.filter { transaction ->
                // Search filter
                val matchesSearch = query.isEmpty() || 
                    transaction.note.contains(query, ignoreCase = true) ||
                    transaction.categoryName.contains(query, ignoreCase = true)

                // Date range filter
                val matchesDateRange = dateRange == null || run {
                    val transactionTime = transaction.date.time
                    transactionTime >= dateRange.first && transactionTime <= dateRange.second
                }

                // Category filter
                val matchesCategory = categories.isEmpty() || 
                    (transaction.categoryId != null && transaction.categoryId in categories)

                // Type filter
                val matchesType = type == null || 
                    transaction.type.equals(type, ignoreCase = true)

                // Amount range filter
                val matchesMinAmount = minAmount == null || minAmount.isBlank() || run {
                    try {
                        transaction.amount >= minAmount.toDouble()
                    } catch (e: NumberFormatException) {
                        true // If invalid, don't filter out
                    }
                }

                val matchesMaxAmount = maxAmount == null || maxAmount.isBlank() || run {
                    try {
                        transaction.amount <= maxAmount.toDouble()
                    } catch (e: NumberFormatException) {
                        true // If invalid, don't filter out
                    }
                }

                matchesSearch && matchesDateRange && matchesCategory && matchesType && matchesMinAmount && matchesMaxAmount
            }
        }
    }.flatMapLatest { it }

    // TRANSFORMING THE FLOW DIRECTLY
    val uiState: StateFlow<HomeUiState> = filteredTransactions
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

    // Functions to update filters
    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun updateFilterDateRange(startDate: Long?, endDate: Long?) {
        filterDateRange.value = if (startDate != null && endDate != null) {
            Pair(startDate, endDate)
        } else {
            null
        }
    }

    fun updateFilterCategories(categories: List<Int>) {
        filterCategories.value = categories
    }

    fun updateFilterType(type: String?) {
        filterType.value = type
    }

    fun updateFilterAmountRange(minAmount: String?, maxAmount: String?) {
        filterMinAmount.value = minAmount
        filterMaxAmount.value = maxAmount
    }

    fun resetFilters() {
        searchQuery.value = ""
        filterDateRange.value = null
        filterCategories.value = emptyList()
        filterType.value = null
        filterMinAmount.value = null
        filterMaxAmount.value = null
    }
    
    // Selection functions
    fun onTransactionLongClick(id: Long) {
        selectedTransactionId.value = id
    }
    
    fun clearSelection() {
        selectedTransactionId.value = null
    }
    
    fun deleteSelectedTransaction() {
        val id = selectedTransactionId.value ?: return
        viewModelScope.launch {
            isLoadingAction.value = true
            try {
                // Get the transaction to delete
                val transaction = transactionRepository.getTransactionById(id)
                if (transaction != null) {
                    transactionRepository.deleteTransaction(transaction)
                    // Add 3-second delay to simulate sync
                    delay(3000)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoadingAction.value = false
                selectedTransactionId.value = null
            }
        }
    }
}