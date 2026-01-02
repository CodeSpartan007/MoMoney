package com.kp.momoney

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kp.momoney.presentation.navigation.AppNavHost
import com.kp.momoney.presentation.navigation.Screen
import com.kp.momoney.ui.theme.MoMoneyTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MoMoneyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
private fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val startDestination = remember {
        if (Firebase.auth.currentUser == null) Screen.Login.route else Screen.Home.route
    }
    val isAuthRoute = currentRoute == Screen.Login.route || currentRoute == Screen.Register.route
    
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
                        Screen.Home to Pair("Home", Icons.Filled.Home),
                        Screen.Budget to Pair("Budget", Icons.Filled.Home),
                        Screen.Reports to Pair("Reports", Icons.Filled.Home)
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
            startDestination = startDestination
        )
    }
}