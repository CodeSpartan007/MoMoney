package com.kp.momoney.presentation.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kp.momoney.data.util.ConnectivityObserver
import com.kp.momoney.domain.model.Category
import com.kp.momoney.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val categoryRepository: CategoryRepository,
    connectivityObserver: ConnectivityObserver
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

    // Selection state for deletion
    private val _selectedCategoryId = MutableStateFlow<Int?>(null)
    val selectedCategoryId: StateFlow<Int?> = _selectedCategoryId.asStateFlow()

    // Loading state for deletion
    private val _isLoadingDelete = MutableStateFlow(false)
    val isLoadingDelete: StateFlow<Boolean> = _isLoadingDelete.asStateFlow()

    // Track last action type for message display
    private var lastActionType: String? = null // "create" or "delete"

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

    /**
     * Boolean state indicating if the device is offline
     * True when status is Lost or Unavailable
     */
    val isOffline: StateFlow<Boolean> = connectivityObserver.observe()
        .map { status ->
            status == ConnectivityObserver.Status.Lost || 
            status == ConnectivityObserver.Status.Unavailable
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = false
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
                
                // Repository returns immediately (optimistic update)
                // Reset form immediately
                categoryName.value = ""
                categoryType.value = "Expense"
                selectedColor.value = "FF9800"
                
                // Set loading to false immediately after repository call returns
                _isLoading.value = false
                
                // Show success event
                lastActionType = "create"
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

    /**
     * Called when a message has been shown to the user.
     * Resets the event state to prevent it from showing again.
     */
    fun onMessageShown() {
        _event.value = null
        lastActionType = null
    }

    /**
     * Gets the last action type for determining the success message.
     */
    fun getLastActionType(): String? = lastActionType

    fun onNameChanged(value: String) {
        categoryName.value = value
    }

    fun onTypeChanged(value: String) {
        categoryType.value = value
    }

    fun onColorSelected(color: String) {
        selectedColor.value = color
    }

    fun onCategoryLongClick(id: Int) {
        // Toggle selection: if already selected, deselect; otherwise select
        _selectedCategoryId.value = if (_selectedCategoryId.value == id) null else id
    }

    fun clearSelection() {
        _selectedCategoryId.value = null
    }

    fun deleteSelectedCategory() {
        val categoryId = _selectedCategoryId.value ?: return

        viewModelScope.launch {
            try {
                _isLoadingDelete.value = true
                
                categoryRepository.deleteCategory(categoryId)
                
                // Repository returns immediately (optimistic update)
                // Set loading to false immediately after repository call returns
                _isLoadingDelete.value = false
                _selectedCategoryId.value = null
                
                // Show success event
                lastActionType = "delete"
                _event.value = CategoryEvent.Success
            } catch (e: Exception) {
                _isLoadingDelete.value = false
                _event.value = CategoryEvent.Error(e.message ?: "Failed to delete category")
            }
        }
    }
}

