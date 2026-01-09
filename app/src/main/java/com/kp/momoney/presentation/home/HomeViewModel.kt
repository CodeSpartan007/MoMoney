package com.kp.momoney.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kp.momoney.data.local.CurrencyPreference
import com.kp.momoney.domain.model.Transaction
import com.kp.momoney.domain.repository.BudgetRepository
import com.kp.momoney.domain.repository.CategoryRepository
import com.kp.momoney.domain.repository.CurrencyRepository
import com.kp.momoney.domain.repository.TransactionRepository
import com.kp.momoney.util.toBaseCurrency
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
    val isLoading: Boolean = true, // Added loading state just in case
    val currencyPreference: CurrencyPreference = CurrencyPreference("KES", "KSh", 1.0f)
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    val categoryRepository: CategoryRepository,
    private val currencyRepository: CurrencyRepository
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
                // CRITICAL: Sync categories FIRST before transactions
                // Transactions have foreign key constraints to categories
                // If we sync a transaction that points to a category that doesn't exist yet,
                // the foreign key constraint will fail or show as unknown
                categoryRepository.syncCategories()
                
                // Then sync transactions (which depend on categories)
                transactionRepository.syncTransactions()
                
                // Process recurring transactions (create child transactions if due)
                transactionRepository.processRecurringTransactions()
                
                // Finally sync budgets (which may also depend on categories)
                budgetRepository.syncBudgets()
            } catch (e: Exception) {
                // Log error but don't crash - app can work offline
                e.printStackTrace()
            }
        }
    }

    // Get all transactions flow
    private val allTransactions = transactionRepository.getAllTransactions()

    // Get currency preference flow
    private val currencyPreference = currencyRepository.getCurrencyPreference()

    // Filtered transactions flow that applies all filters
    // Using nested combine since combine supports max 5 flows directly
    private val filteredTransactions = combine(
        allTransactions,
        searchQuery,
        filterDateRange,
        filterCategories,
        filterType
    ) { transactions, query, dateRange, categories, type ->
        combine(filterMinAmount, filterMaxAmount, currencyPreference) { minAmount, maxAmount, currency ->
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

                // Amount range filter - convert user input from selected currency to base currency (KES)
                val matchesMinAmount = minAmount == null || minAmount.isBlank() || run {
                    try {
                        val userInputAmount = minAmount.toDouble()
                        // Convert from selected currency to base currency (KES)
                        val minAmountInKES = if (currency.currencyCode != "KES" && currency.exchangeRate > 0) {
                            userInputAmount.toBaseCurrency(currency.exchangeRate)
                        } else {
                            userInputAmount
                        }
                        transaction.amount >= minAmountInKES
                    } catch (e: NumberFormatException) {
                        true // If invalid, don't filter out
                    }
                }

                val matchesMaxAmount = maxAmount == null || maxAmount.isBlank() || run {
                    try {
                        val userInputAmount = maxAmount.toDouble()
                        // Convert from selected currency to base currency (KES)
                        val maxAmountInKES = if (currency.currencyCode != "KES" && currency.exchangeRate > 0) {
                            userInputAmount.toBaseCurrency(currency.exchangeRate)
                        } else {
                            userInputAmount
                        }
                        transaction.amount <= maxAmountInKES
                    } catch (e: NumberFormatException) {
                        true // If invalid, don't filter out
                    }
                }

                matchesSearch && matchesDateRange && matchesCategory && matchesType && matchesMinAmount && matchesMaxAmount
            }
        }
    }.flatMapLatest { it }

    // TRANSFORMING THE FLOW DIRECTLY - Combine transactions with currency preference
    val uiState: StateFlow<HomeUiState> = combine(
        filteredTransactions,
        currencyPreference
    ) { transactions, currency ->
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
            isLoading = false,
            currencyPreference = currency
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