package com.kp.momoney.presentation.navigation

/**
 * Sealed class representing all navigation destinations in the app
 */
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object AddTransaction : Screen("add_transaction")
    object Budget : Screen("budget")
    object Reports : Screen("reports")
    object Settings : Screen("settings")
    object Login : Screen("login")
    object Register : Screen("register")
}

