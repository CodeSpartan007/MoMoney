package com.kp.momoney.presentation.add_transaction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kp.momoney.domain.model.Category
import com.kp.momoney.domain.model.Recurrence
import com.kp.momoney.domain.model.Transaction
import com.kp.momoney.data.repository.NotificationRepository
import com.kp.momoney.domain.repository.BudgetRepository
import com.kp.momoney.domain.repository.CategoryRepository
import com.kp.momoney.domain.repository.CurrencyRepository
import com.kp.momoney.domain.repository.TransactionRepository
import com.kp.momoney.util.DateUtils
import com.kp.momoney.util.toBaseCurrency
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.abs
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

sealed class AddTransactionEvent {
    object Success : AddTransactionEvent()
    data class Error(val message: String) : AddTransactionEvent()
    data class MapsBackWithResult(val message: String?) : AddTransactionEvent()
}

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository,
    private val currencyRepository: CurrencyRepository,
    private val notificationRepository: NotificationRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val amount = MutableStateFlow("")
    val note = MutableStateFlow("")
    val selectedCategory = MutableStateFlow<Category?>(null)
    val transactionDate = MutableStateFlow(System.currentTimeMillis())
    val recurrence = MutableStateFlow<Recurrence>(Recurrence.NEVER)
    
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _event = MutableStateFlow<AddTransactionEvent?>(null)
    val event: StateFlow<AddTransactionEvent?> = _event.asStateFlow()
    
    private val _currencySymbol = MutableStateFlow("KSh")
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()
    
    private val transactionId: Long? = savedStateHandle.get<Long>("transactionId")?.takeIf { it > 0 }
    val isEditMode: Boolean = transactionId != null
    
    init {
        loadCategories()
        observeCurrencyPreference()
        if (transactionId != null) {
            loadTransaction(transactionId)
        }
    }
    
    private fun observeCurrencyPreference() {
        viewModelScope.launch {
            currencyRepository.getCurrencyPreference()
                .catch { exception ->
                    exception.printStackTrace()
                    // Default to KES if error
                    _currencySymbol.value = "KSh"
                }
                .collect { currencyPreference ->
                    _currencySymbol.value = currencyPreference.currencySymbol
                }
        }
    }
    
    private fun loadTransaction(id: Long) {
        viewModelScope.launch {
            try {
                val transaction = transactionRepository.getTransactionById(id)
                if (transaction != null) {
                    // Get current currency preference
                    val currencyPreference = currencyRepository.getCurrencyPreference()
                        .catch { emit(com.kp.momoney.data.local.CurrencyPreference("KES", "KSh", 1.0f)) }
                        .first()
                    
                    // Convert from KES (base) to selected currency for display
                    // transaction.amount is in KES, we need to show it in selected currency
                    val convertedAmount = if (currencyPreference.currencyCode != "KES" && currencyPreference.exchangeRate > 0) {
                        transaction.amount * currencyPreference.exchangeRate
                    } else {
                        transaction.amount
                    }
                    
                    amount.value = convertedAmount.toString()
                    note.value = transaction.note
                    transactionDate.value = transaction.date.time
                    recurrence.value = transaction.recurrence
                    
                    // Find and set the category
                    val categories = categoryRepository.getAllCategories()
                        .catch { emit(emptyList()) }
                        .first()
                    val category = categories.find { it.id == transaction.categoryId }
                    selectedCategory.value = category
                }
            } catch (e: Exception) {
                _event.value = AddTransactionEvent.Error("Failed to load transaction: ${e.message}")
            }
        }
    }
    
    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getAllCategories()
                .catch { exception ->
                    exception.printStackTrace()
                }
                .collect { categories ->
                    _categories.value = categories
                }
        }
    }
    
    fun saveTransaction() {
        val amountValue = amount.value.trim()
        val noteValue = note.value.trim()
        val category = selectedCategory.value
        
        // Validation
        if (amountValue.isEmpty()) {
            _event.value = AddTransactionEvent.Error("Amount cannot be empty")
            return
        }
        
        val amountDouble = try {
            amountValue.toDouble()
        } catch (e: NumberFormatException) {
            _event.value = AddTransactionEvent.Error("Invalid amount format")
            return
        }
        
        if (amountDouble <= 0) {
            _event.value = AddTransactionEvent.Error("Amount must be greater than 0")
            return
        }
        
        if (category == null) {
            _event.value = AddTransactionEvent.Error("Please select a category")
            return
        }
        
        // Convert from selected currency to base currency (KES) before saving
        viewModelScope.launch {
            try {
                val currencyPreference = currencyRepository.getCurrencyPreference()
                    .catch { emit(com.kp.momoney.data.local.CurrencyPreference("KES", "KSh", 1.0f)) }
                    .first()
                
                // Convert user input (in selected currency) to base currency (KES)
                val finalAmount = if (currencyPreference.currencyCode != "KES" && currencyPreference.exchangeRate > 0) {
                    amountDouble.toBaseCurrency(currencyPreference.exchangeRate)
                } else {
                    amountDouble
                }
                
                // Determine transaction type from category
                val transactionType = category.type
                
                val transaction = Transaction(
                    id = transactionId ?: 0, // Use existing ID if editing, 0 for new
                    amount = finalAmount,
                    date = Date(transactionDate.value),
                    note = noteValue,
                    type = transactionType,
                    categoryId = category.id,
                    categoryName = category.name,
                    categoryColor = category.color,
                    categoryIcon = category.icon,
                    recurrence = recurrence.value
                )
                
                saveTransactionInternal(transaction, transactionType, category.id)
            } catch (e: Exception) {
                _event.value = AddTransactionEvent.Error("Failed to convert currency: ${e.message}")
            }
        }
    }
    
    private fun saveTransactionInternal(
        transaction: Transaction,
        transactionType: String,
        categoryId: Int
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Update or insert based on edit mode
                if (isEditMode && transactionId != null) {
                    transactionRepository.updateTransaction(transaction)
                } else {
                    transactionRepository.insertTransaction(transaction)
                }
                
                // Repository now returns immediately after Room write
                // No need for artificial delay
                
                // Check budget for expense transactions only (in background to not block UI)
                if (transactionType.equals("Expense", ignoreCase = true)) {
                    viewModelScope.launch(Dispatchers.IO) {
                        try {
                            val budget = budgetRepository.getBudgetForCategory(categoryId)
                            if (budget != null && budget.limitAmount > 0) {
                                val startDate = DateUtils.getCurrentMonthStart()
                                val endDate = DateUtils.getCurrentMonthEnd()
                                
                                // Get current spending for this category (includes the transaction we just saved)
                                val totalSpending = transactionRepository.getCategorySpendingForCategory(
                                    categoryId,
                                    startDate,
                                    endDate
                                )
                                
                                // Log notifications based on 3-tier thresholds
                                val limit = budget.limitAmount
                                when {
                                    // Tier 1: Exceeded
                                    totalSpending > limit -> {
                                        notificationRepository.logNotification(
                                            title = "Budget Exceeded",
                                            message = "You have overspent on ${transaction.categoryName}.",
                                            type = "BUDGET"
                                        )
                                    }
                                    // Tier 2: Reached (exactly 100% - using float safety)
                                    abs(totalSpending - limit) < 0.1 -> {
                                        notificationRepository.logNotification(
                                            title = "Budget Reached",
                                            message = "You have hit exactly 100% of your ${transaction.categoryName} budget.",
                                            type = "BUDGET"
                                        )
                                    }
                                    // Tier 3: Near (>= 90% but < 100%)
                                    totalSpending >= (limit * 0.9) && totalSpending < limit -> {
                                        notificationRepository.logNotification(
                                            title = "Budget Alert",
                                            message = "You are close to the limit for ${transaction.categoryName}.",
                                            type = "BUDGET"
                                        )
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            // Log error but don't block transaction save
                            e.printStackTrace()
                        }
                    }
                }
                
                // Emit success event
                _event.value = AddTransactionEvent.Success
                
                // Clear form only if not in edit mode
                if (!isEditMode) {
                    amount.value = ""
                    note.value = ""
                    selectedCategory.value = null
                    transactionDate.value = System.currentTimeMillis()
                    recurrence.value = Recurrence.NEVER
                }
            } catch (e: Exception) {
                _event.value = AddTransactionEvent.Error("Failed to save transaction: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearEvent() {
        _event.value = null
    }
    
    fun onDateChange(newDate: Long) {
        transactionDate.value = newDate
    }
    
    fun onRecurrenceChange(newRecurrence: Recurrence) {
        recurrence.value = newRecurrence
    }
}

