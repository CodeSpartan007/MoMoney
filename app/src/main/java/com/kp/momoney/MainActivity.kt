package com.kp.momoney

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
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
                    val navController = rememberNavController()
                    androidx.compose.material3.Scaffold(
                        bottomBar = {
                            NavigationBar {
                                val destinations = listOf(
                                    Screen.Home to Pair("Home", Icons.Filled.Home),
                                    Screen.Budget to Pair("Budget", Icons.Filled.Add),
                                    Screen.Reports to Pair("Reports", Icons.Filled.Add)
                                )

                                val navBackStackEntry by navController.currentBackStackEntryAsState()
                                val currentRoute = navBackStackEntry?.destination?.route

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
                    ) { paddingValues ->
                        AppNavHost(
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}