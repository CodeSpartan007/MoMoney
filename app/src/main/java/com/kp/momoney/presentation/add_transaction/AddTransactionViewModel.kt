package com.kp.momoney.presentation.add_transaction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kp.momoney.domain.model.Category
import com.kp.momoney.domain.model.Transaction
import com.kp.momoney.domain.repository.CategoryRepository
import com.kp.momoney.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
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
}

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val amount = MutableStateFlow("")
    val note = MutableStateFlow("")
    val selectedCategory = MutableStateFlow<Category?>(null)
    val transactionDate = MutableStateFlow(System.currentTimeMillis())
    
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _event = MutableStateFlow<AddTransactionEvent?>(null)
    val event: StateFlow<AddTransactionEvent?> = _event.asStateFlow()
    
    private val transactionId: Long? = savedStateHandle.get<Long>("transactionId")?.takeIf { it > 0 }
    val isEditMode: Boolean = transactionId != null
    
    init {
        loadCategories()
        if (transactionId != null) {
            loadTransaction(transactionId)
        }
    }
    
    private fun loadTransaction(id: Long) {
        viewModelScope.launch {
            try {
                val transaction = transactionRepository.getTransactionById(id)
                if (transaction != null) {
                    amount.value = transaction.amount.toString()
                    note.value = transaction.note
                    transactionDate.value = transaction.date.time
                    
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
        
        // Determine transaction type from category
        val transactionType = category.type
        
        val transaction = Transaction(
            id = transactionId ?: 0, // Use existing ID if editing, 0 for new
            amount = amountDouble,
            date = Date(transactionDate.value),
            note = noteValue,
            type = transactionType,
            categoryId = category.id,
            categoryName = category.name,
            categoryColor = category.color,
            categoryIcon = category.icon
        )
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Update or insert based on edit mode
                if (isEditMode && transactionId != null) {
                    transactionRepository.updateTransaction(transaction)
                } else {
                    transactionRepository.insertTransaction(transaction)
                }
                
                // Add 3-second artificial delay
                delay(3000)
                
                // Emit success event
                _event.value = AddTransactionEvent.Success
                
                // Clear form only if not in edit mode
                if (!isEditMode) {
                    amount.value = ""
                    note.value = ""
                    selectedCategory.value = null
                    transactionDate.value = System.currentTimeMillis()
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
}

