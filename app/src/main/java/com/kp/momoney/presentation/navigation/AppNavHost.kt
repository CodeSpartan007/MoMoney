package com.kp.momoney.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kp.momoney.presentation.add_transaction.AddTransactionScreen
import com.kp.momoney.presentation.budget.BudgetScreen
import com.kp.momoney.presentation.home.HomeScreen
import com.kp.momoney.presentation.reports.ReportsScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    innerPadding: PaddingValues,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable(Screen.Home.route) {
            HomeScreen()
        }
        
        composable(Screen.AddTransaction.route) {
            AddTransactionScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Budget.route) {
            BudgetScreen()
        }
        
        composable(Screen.Reports.route) {
            ReportsScreen()
        }
    }
}

