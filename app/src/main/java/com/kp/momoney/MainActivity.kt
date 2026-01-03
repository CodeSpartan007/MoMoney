package com.kp.momoney

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kp.momoney.presentation.navigation.AppNavHost
import com.kp.momoney.presentation.navigation.Screen
import com.kp.momoney.ui.theme.AppThemeConfig
import com.kp.momoney.ui.theme.SunYellow
import com.kp.momoney.ui.theme.MoMoneyTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var themeConfig by remember { 
                mutableStateOf(AppThemeConfig(seedColor = SunYellow, isDark = false))
            }
            
            MoMoneyTheme(themeConfig = themeConfig) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        themeConfig = themeConfig,
                        onThemeChanged = { newConfig ->
                            themeConfig = newConfig
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MainScreen(
    themeConfig: AppThemeConfig,
    onThemeChanged: (AppThemeConfig) -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val startDestination = Screen.Splash.route
    val isAuthRoute = currentRoute == Screen.Login.route || currentRoute == Screen.Register.route || currentRoute == Screen.Splash.route
    
    // Determine bottom bar visibility based on current route
    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Reports.route,
        Screen.Budget.route
    )

    Scaffold(
        floatingActionButton = {
            if (!isAuthRoute && currentRoute == Screen.Home.route) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(Screen.AddTransaction.route)
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Transaction"
                    )
                }
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    val destinations = listOf(
                        Screen.Home to Pair("Home", Icons.Default.Home),
                        Screen.Budget to Pair("Budgets", Icons.Default.DateRange),
                        Screen.Reports to Pair("Reports", Icons.AutoMirrored.Filled.List)
                    )

                    destinations.forEach { (screen, labelIcon) ->
                        NavigationBarItem(
                            selected = currentRoute == screen.route,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(Screen.Home.route) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = labelIcon.second,
                                    contentDescription = labelIcon.first
                                )
                            },
                            label = {
                                Text(text = labelIcon.first)
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            innerPadding = innerPadding,
            startDestination = startDestination,
            themeConfig = themeConfig,
            onThemeChanged = onThemeChanged
        )
    }
}