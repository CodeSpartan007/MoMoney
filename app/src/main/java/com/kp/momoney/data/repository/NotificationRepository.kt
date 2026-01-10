package com.kp.momoney.data.repository

import com.kp.momoney.data.local.dao.NotificationDao
import com.kp.momoney.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NotificationRepository @Inject constructor(
    private val notificationDao: NotificationDao
) {
    
    val notifications: Flow<List<NotificationEntity>>
        get() = notificationDao.getAllNotifications()
    
    val unreadCount: Flow<Int>
        get() = notificationDao.getUnreadCount()
    
    suspend fun logNotification(title: String, message: String, type: String) {
        val notification = NotificationEntity(
            title = title,
            message = message,
            type = type,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        notificationDao.insertNotification(notification)
    }
    
    suspend fun markAllRead() {
        notificationDao.markAllAsRead()
    }
}

