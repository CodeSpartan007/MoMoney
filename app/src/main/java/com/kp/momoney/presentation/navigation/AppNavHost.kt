package com.kp.momoney.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kp.momoney.presentation.auth.LoginScreen
import com.kp.momoney.presentation.auth.RegisterScreen
import com.kp.momoney.presentation.add_transaction.AddTransactionScreen
import com.kp.momoney.presentation.budget.BudgetScreen
import com.kp.momoney.presentation.category.CategoryManagerScreen
import com.kp.momoney.presentation.home.HomeScreen
import com.kp.momoney.presentation.reports.ReportsScreen
import com.kp.momoney.presentation.settings.SettingsScreen
import com.kp.momoney.presentation.splash.SplashScreen
import com.kp.momoney.ui.theme.AppThemeConfig

@Composable
fun AppNavHost(
    navController: NavHostController,
    innerPadding: PaddingValues,
    startDestination: String = Screen.Splash.route,
    themeConfig: AppThemeConfig,
    onThemeChanged: (AppThemeConfig) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    // Check if user is logged in
                    val isLoggedIn = Firebase.auth.currentUser != null
                    val destination = if (isLoggedIn) {
                        Screen.Home.route
                    } else {
                        Screen.Login.route
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToEditTransaction = { transactionId ->
                    navController.navigate(Screen.AddTransaction.createRoute(transactionId))
                }
            )
        }
        
        // Route for adding new transaction (no ID)
        composable(Screen.AddTransaction.route) {
            AddTransactionScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Route for editing existing transaction (with ID as path parameter)
        composable(
            route = "${Screen.AddTransaction.route}/{transactionId}",
            arguments = listOf(
                navArgument("transactionId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
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
        
        composable(Screen.Categories.route) {
            CategoryManagerScreen()
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                themeConfig = themeConfig,
                onThemeChanged = onThemeChanged
            )
        }
    }
}

