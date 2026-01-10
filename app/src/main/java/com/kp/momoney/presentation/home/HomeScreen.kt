package com.kp.momoney.presentation.home

import androidx.compose.material3.CenterAlignedTopAppBar
import com.kp.momoney.util.getIconByName
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.kp.momoney.domain.model.Transaction
import java.text.SimpleDateFormat
import java.util.Locale
import com.kp.momoney.util.toCurrency
import com.kp.momoney.R
import com.kp.momoney.presentation.home.components.SearchFilterBar
import com.kp.momoney.presentation.home.components.FilterSheet
import com.kp.momoney.presentation.common.AppLoadingAnimation
import com.kp.momoney.presentation.common.NotificationBell
import com.kp.momoney.presentation.notifications.NotificationViewModel


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit = {},
    onNavigateToEditTransaction: (Long) -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    navController: NavController? = null,
    viewModel: HomeViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel()
) {
    // FIX: Use collectAsState() to listen for real-time updates
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterDateRange by viewModel.filterDateRange.collectAsState()
    val filterCategories by viewModel.filterCategories.collectAsState()
    val filterType by viewModel.filterType.collectAsState()
    val filterMinAmount by viewModel.filterMinAmount.collectAsState()
    val filterMaxAmount by viewModel.filterMaxAmount.collectAsState()
    val selectedTransactionId by viewModel.selectedTransactionId.collectAsState()
    val isLoadingAction by viewModel.isLoadingAction.collectAsState()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()
    
    var showFilterSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    // Observe budget alert from savedStateHandle
    var budgetAlertMessage by remember { mutableStateOf<String?>(null) }
    var showBudgetAlert by remember { mutableStateOf(false) }
    
    // Check for budget alert when screen resumes or is first composed
    val savedStateHandle = navController?.currentBackStackEntry?.savedStateHandle
    val budgetAlertLiveData = savedStateHandle?.getLiveData<String>("budget_alert")
    
    // Observe the LiveData and show dialog when message is received
    DisposableEffect(budgetAlertLiveData) {
        val observer = androidx.lifecycle.Observer<String?> { message ->
            if (message != null) {
                budgetAlertMessage = message
                showBudgetAlert = true
                // Clear the savedStateHandle to prevent showing again on rotation
                savedStateHandle?.remove<String>("budget_alert")
            }
        }
        budgetAlertLiveData?.observeForever(observer)
        onDispose {
            budgetAlertLiveData?.removeObserver(observer)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Contextual Top Bar when transaction is selected
            if (selectedTransactionId != null) {
                TopAppBar(
                    title = { Text("1 Selected") },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close"
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                selectedTransactionId?.let { id ->
                                    onNavigateToEditTransaction(id)
                                    viewModel.clearSelection()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit"
                            )
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete"
                            )
                        }
                    }
                )
            } else {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_logo_mini),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(end = 8.dp)
                            )
                            Text(text = "MoMoney")
                        }
                    },
                    actions = {
                        NotificationBell(
                            unreadCount = unreadCount,
                            onClick = onNavigateToNotifications
                        )
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Settings"
                            )
                        }
                    }
                )
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        TotalBalanceCard(
                            totalIncome = uiState.totalIncome,
                            totalExpense = uiState.totalExpense,
                            currencyPreference = uiState.currencyPreference
                        )
                    }
                    
                    item {
                        SearchFilterBar(
                            searchQuery = searchQuery,
                            onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                            onFilterClick = { showFilterSheet = true }
                        )
                    }

                    if (uiState.transactions.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No transactions yet.\nTap + to add your first transaction!",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(uiState.transactions) { transaction ->
                            val isSelected = transaction.id == selectedTransactionId
                            TransactionItem(
                                transaction = transaction,
                                isSelected = isSelected,
                                currencyPreference = uiState.currencyPreference,
                                onLongClick = { viewModel.onTransactionLongClick(transaction.id) },
                                onClick = {
                                    if (selectedTransactionId != null) {
                                        viewModel.clearSelection()
                                    }
                                }
                            )
                        }
                    }
                }
            }
            
            // Filter Sheet
            if (showFilterSheet) {
                FilterSheet(
                    onDismiss = { showFilterSheet = false },
                    onApply = { startDate, endDate, type, categories, minAmount, maxAmount ->
                        viewModel.updateFilterDateRange(startDate, endDate)
                        viewModel.updateFilterType(type)
                        viewModel.updateFilterCategories(categories)
                        viewModel.updateFilterAmountRange(minAmount, maxAmount)
                    },
                    onReset = {
                        viewModel.resetFilters()
                    },
                    categoryRepository = viewModel.categoryRepository,
                    currentDateRange = filterDateRange,
                    currentType = filterType,
                    currentCategories = filterCategories,
                    currentMinAmount = filterMinAmount,
                    currentMaxAmount = filterMaxAmount
                )
            }
            
            // Delete Confirmation Dialog
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Transaction") },
                    text = { Text("Are you sure you want to delete this transaction?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.deleteSelectedTransaction()
                                showDeleteDialog = false
                            }
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
            
            // Budget Alert Dialog
            if (showBudgetAlert && budgetAlertMessage != null) {
                AlertDialog(
                    onDismissRequest = { 
                        showBudgetAlert = false
                    },
                    title = { Text("Budget Alert") },
                    text = { Text(budgetAlertMessage ?: "") },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "Warning",
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showBudgetAlert = false
                            }
                        ) {
                            Text("OK")
                        }
                    }
                )
            }
        }

        // Loading Overlay - This sits ON TOP of the Scaffold
        if (isLoadingAction) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = false) {}, // Block touches
                contentAlignment = Alignment.Center
            ) {
                AppLoadingAnimation()
            }
        }
    }
}

// ... Keep your TotalBalanceCard and TransactionItem functions exactly as they were ...
// (I have omitted them here to save space, but you should keep them in the file)

@Composable
fun TotalBalanceCard(
    totalIncome: Double,
    totalExpense: Double,
    currencyPreference: com.kp.momoney.data.local.CurrencyPreference
) {
    val balance = totalIncome - totalExpense
    val balanceColor = if (balance >= 0) {
        Color(0xFF4CAF50) // Green
    } else {
        Color(0xFFF44336) // Red
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Total Balance",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = balance.toCurrency(currencyPreference.exchangeRate, currencyPreference.currencySymbol),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = balanceColor
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Income",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = totalIncome.toCurrency(currencyPreference.exchangeRate, currencyPreference.currencySymbol),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF4CAF50)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Expense",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = totalExpense.toCurrency(currencyPreference.exchangeRate, currencyPreference.currencySymbol),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFF44336)
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    isSelected: Boolean = false,
    currencyPreference: com.kp.momoney.data.local.CurrencyPreference,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    val isIncome = transaction.type.equals("Income", ignoreCase = true)
    val amountColor = if (isIncome) {
        Color(0xFF4CAF50) // Green
    } else {
        Color(0xFFF44336) // Red
    }

    val amountPrefix = if (isIncome) "+" else "-"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onLongClick = onLongClick,
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val categoryColor = try {
                    if (transaction.categoryColor.startsWith("#")) {
                        Color(android.graphics.Color.parseColor(transaction.categoryColor))
                    } else {
                        Color(android.graphics.Color.parseColor("#${transaction.categoryColor}"))
                    }
                } catch (e: Exception) {
                    MaterialTheme.colorScheme.primary
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(categoryColor),
                    contentAlignment = Alignment.Center
                ) {
                    // Using a simple circle with first letter of category as icon placeholder
                    // In production, you'd use actual icon resources based on categoryIcon
                    Icon(
                        imageVector = getIconByName(transaction.categoryIcon),
                        contentDescription = transaction.categoryName,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Transaction Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = transaction.note.ifEmpty { transaction.categoryName },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatDate(transaction.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Amount
            Text(
                text = "$amountPrefix${transaction.amount.toCurrency(currencyPreference.exchangeRate, currencyPreference.currencySymbol)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}

private fun formatDate(date: java.util.Date): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(date)
}