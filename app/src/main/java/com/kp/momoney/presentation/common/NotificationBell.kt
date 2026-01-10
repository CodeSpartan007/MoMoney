package com.kp.momoney.presentation.common

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NotificationBell(
    unreadCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        if (unreadCount > 0) {
            BadgedBox(
                badge = {
                    Badge {
                        // Show count if <= 99, otherwise show "99+"
                        val badgeText = if (unreadCount > 99) "99+" else unreadCount.toString()
                        androidx.compose.material3.Text(
                            text = badgeText,
                            style = androidx.compose.material3.MaterialTheme.typography.labelSmall
                        )
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Notifications",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

