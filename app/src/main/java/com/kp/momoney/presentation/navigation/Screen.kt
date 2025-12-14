package com.kp.momoney.presentation.navigation

/**
 * Sealed class representing all navigation destinations in the app
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddTransaction : Screen("add_transaction")
    object Reports : Screen("reports")
    object Settings : Screen("settings")
}

