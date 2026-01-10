package com.kp.momoney.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kp.momoney.data.local.entity.NotificationEntity
import com.kp.momoney.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    val uiState: StateFlow<List<NotificationEntity>> = notificationRepository.notifications
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val unreadCount: StateFlow<Int> = notificationRepository.unreadCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog.asStateFlow()

    fun markRead() {
        viewModelScope.launch {
            notificationRepository.markAllRead()
        }
    }
    
    fun onDeleteClicked() {
        _showDeleteDialog.value = true
    }
    
    fun onDeleteConfirmed() {
        viewModelScope.launch {
            notificationRepository.deleteReadNotifications()
            _showDeleteDialog.value = false
        }
    }
    
    fun onDeleteDismissed() {
        _showDeleteDialog.value = false
    }
}

