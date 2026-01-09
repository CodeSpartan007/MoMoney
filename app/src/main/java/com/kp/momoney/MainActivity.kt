package com.kp.momoney

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kp.momoney.data.local.AppTheme
import com.kp.momoney.data.repository.AppLockRepository
import com.kp.momoney.presentation.MainViewModel
import com.kp.momoney.presentation.common.OfflineBanner
import com.kp.momoney.presentation.navigation.AppNavHost
import com.kp.momoney.presentation.navigation.Screen
import com.kp.momoney.ui.theme.AppThemeConfig
import com.kp.momoney.ui.theme.SunYellow
import com.kp.momoney.ui.theme.MoMoneyTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity(), DefaultLifecycleObserver {
    
    @Inject
    lateinit var appLockRepository: AppLockRepository
    
    private lateinit var mainViewModel: MainViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super<ComponentActivity>.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Register lifecycle observer
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        
        setContent {
            val viewModel: MainViewModel = hiltViewModel()
            mainViewModel = viewModel // Store reference for lifecycle observer
            val appTheme by viewModel.theme.collectAsState(initial = AppTheme.LIGHT)
            val seedColor by viewModel.seedColor.collectAsState(initial = SunYellow)
            val isAppLocked by viewModel.isAppLocked.collectAsState()
            val isAppLockEnabled by appLockRepository.isAppLockEnabled().collectAsState(initial = false)
            
            // Determine isDarkTheme based on AppTheme
            val isDarkTheme = when (appTheme) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.SYSTEM -> isSystemInDarkTheme()
            }
            
            // Create themeConfig with DataStore-controlled isDark and seedColor
            val themeConfig = remember(isDarkTheme, seedColor) { 
                AppThemeConfig(seedColor = seedColor, isDark = isDarkTheme)
            }
            
            MoMoneyTheme(themeConfig = themeConfig) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Strict Gatekeeper: Conditionally render based on lock state
                    // When locked, ONLY show AppLockScreen - AppNavHost is NOT composed
                    if (isAppLocked && isAppLockEnabled) {
                        // ONLY show the lock screen. The Navigation Host is NOT composed.
                        com.kp.momoney.presentation.auth.AppLockScreen(
                            onUnlockSuccess = { viewModel.unlockApp() }
                        )
                    } else {
                        // Show the App normally
                        MainScreen(
                            viewModel = viewModel,
                            appLockRepository = appLockRepository,
                            themeConfig = themeConfig,
                            onThemeChanged = { newConfig ->
                                // This will be handled by SettingsViewModel
                                // The seedColor is already persisted via DataStore
                            }
                        )
                    }
                }
            }
        }
    }
    
    override fun onStop(owner: LifecycleOwner) {
        super<DefaultLifecycleObserver>.onStop(owner)
        // Lock the app when it goes to background if app lock is enabled
        lifecycleScope.launch {
            val isAppLockEnabled = appLockRepository.isAppLockEnabled().first()
            if (isAppLockEnabled) {
                mainViewModel.lockApp()
            }
        }
    }
}

@Composable
private fun MainScreen(
    viewModel: MainViewModel,
    appLockRepository: AppLockRepository,
    themeConfig: AppThemeConfig,
    onThemeChanged: (AppThemeConfig) -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val startDestination = Screen.Splash.route
    val isAuthRoute = currentRoute == Screen.Login.route || currentRoute == Screen.Register.route || currentRoute == Screen.Splash.route
    
    // Get connectivity status
    val isOffline by viewModel.isOffline.collectAsState()
    
    // Determine bottom bar visibility based on current route
    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Reports.route,
        Screen.Budget.route,
        Screen.Categories.route
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
                        Screen.Reports to Pair("Reports", Icons.AutoMirrored.Filled.List),
                        Screen.Categories to Pair("Categories", Icons.Default.Label)
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
        Box(modifier = Modifier.fillMaxSize()) {
            AppNavHost(
                navController = navController,
                innerPadding = innerPadding,
                startDestination = startDestination,
                themeConfig = themeConfig,
                onThemeChanged = onThemeChanged
            )
            
            // Offline banner positioned at the bottom, above navigation bar
            OfflineBanner(
                isOffline = isOffline,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}