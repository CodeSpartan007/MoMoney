package com.kp.momoney.presentation.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kp.momoney.domain.model.Category
import com.kp.momoney.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CategoryEvent {
    object Success : CategoryEvent()
    data class Error(val message: String) : CategoryEvent()
}

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    // Form state
    val categoryName = MutableStateFlow("")
    val categoryType = MutableStateFlow("Expense") // Default to Expense
    val selectedColor = MutableStateFlow("FF9800") // Default to Orange

    // Event state for feedback
    private val _event = MutableStateFlow<CategoryEvent?>(null)
    val event: StateFlow<CategoryEvent?> = _event.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Get all categories and filter to show only user categories
    val userCategories: StateFlow<List<Category>> = categoryRepository.getAllCategories()
        .map { categories ->
            // Filter to show only custom user categories
            // User categories have "spanner" icon (hardcoded in addUserCategory)
            categories.filter { it.icon == "spanner" }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun createCategory() {
        val name = categoryName.value.trim()
        
        // Validation
        if (name.isEmpty()) {
            _event.value = CategoryEvent.Error("Category name cannot be empty")
            return
        }

        if (name.length > 50) {
            _event.value = CategoryEvent.Error("Category name is too long (max 50 characters)")
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                categoryRepository.addUserCategory(
                    name = name,
                    type = categoryType.value,
                    color = selectedColor.value
                )
                
                // Delay to allow user to enjoy the animation while background sync happens
                delay(3000)
                
                // Reset form
                categoryName.value = ""
                categoryType.value = "Expense"
                selectedColor.value = "FF9800"
                
                _isLoading.value = false
                
                // Show success event
                _event.value = CategoryEvent.Success
            } catch (e: Exception) {
                _isLoading.value = false
                _event.value = CategoryEvent.Error(e.message ?: "Failed to create category")
            }
        }
    }

    fun clearEvent() {
        _event.value = null
    }

    fun onNameChanged(value: String) {
        categoryName.value = value
    }

    fun onTypeChanged(value: String) {
        categoryType.value = value
    }

    fun onColorSelected(color: String) {
        selectedColor.value = color
    }
}

