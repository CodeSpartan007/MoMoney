package com.kp.momoney.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.kp.momoney.data.local.AppDatabase
import com.kp.momoney.data.local.entity.CategoryEntity
import com.kp.momoney.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SettingsEvent {
    object MapsToLogin : SettingsEvent()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val appDatabase: AppDatabase,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    val userEmail: String
        get() = firebaseAuth.currentUser?.email ?: ""

    private val _event = MutableStateFlow<SettingsEvent?>(null)
    val event: StateFlow<SettingsEvent?> = _event.asStateFlow()

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            // Step 1: Clear all local data
            appDatabase.clearAllTables()
            
            // Step 2: Re-seed default categories
            seedDefaultCategories()
            
            // Step 3: Sign out from Firebase
            authRepository.logout()
            
            // Step 4: Emit navigation event
            _event.value = SettingsEvent.MapsToLogin
        }
    }

    private suspend fun seedDefaultCategories() {
        val defaultCategories = listOf(
            CategoryEntity(
                name = "Salary",
                type = "Income",
                colorHex = "4CAF50", // Green
                iconName = "money_icon"
            ),
            CategoryEntity(
                name = "Food",
                type = "Expense",
                colorHex = "FF9800", // Orange
                iconName = "restaurant_icon"
            ),
            CategoryEntity(
                name = "Transport",
                type = "Expense",
                colorHex = "2196F3", // Blue
                iconName = "car_icon"
            ),
            CategoryEntity(
                name = "Rent",
                type = "Expense",
                colorHex = "F44336", // Red
                iconName = "home_icon"
            )
        )
        
        defaultCategories.forEach { category ->
            appDatabase.categoryDao().insertCategory(category)
        }
    }

    fun clearEvent() {
        _event.value = null
    }
}

