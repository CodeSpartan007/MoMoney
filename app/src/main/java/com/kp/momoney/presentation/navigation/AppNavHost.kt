package com.kp.momoney.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kp.momoney.presentation.add_transaction.AddTransactionScreen
import com.kp.momoney.presentation.home.HomeScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        
        composable(Screen.AddTransaction.route) {
            AddTransactionScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

