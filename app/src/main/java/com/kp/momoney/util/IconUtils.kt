package com.kp.momoney.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Helper function to retrieve a Material Icon based on its string name.
 * This is useful because we store icon references as strings in the Room database.
 */
fun getIconByName(name: String): ImageVector {
    return when (name.lowercase()) {
        "salary" -> Icons.Default.AttachMoney
        "food" -> Icons.Default.ShoppingCart // Fallback to ShoppingCart if Restaurant not available/preferred
        "transport" -> Icons.Default.Place
        "rent" -> Icons.Default.Home
        else -> Icons.Default.Star
    }
}
