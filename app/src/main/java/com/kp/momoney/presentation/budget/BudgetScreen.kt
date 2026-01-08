package com.kp.momoney.presentation.budget

import androidx.compose.material3.CenterAlignedTopAppBar
import com.kp.momoney.util.getIconByName
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.* // <--- CRITICAL IMPORT for collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kp.momoney.domain.model.BudgetState
import com.kp.momoney.presentation.common.AppLoadingAnimation
import java.text.NumberFormat
import java.util.Locale
import com.kp.momoney.util.toCurrency
import com.kp.momoney.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = hiltViewModel()
) {
    // FIX 2: Use collectAsState().
    // .value reads the data ONCE. collectAsState listens for UPDATES.
    val uiState by viewModel.uiState.collectAsState()

    var showEditDialog by remember { mutableStateOf<BudgetState?>(null) }
    var editAmountText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
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
                        Text(text = "Budgets")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    AppLoadingAnimation(modifier = Modifier.align(Alignment.Center))
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Error: ${uiState.error}",
                            color = MaterialTheme.colorScheme.error
                        )
                        // Retry is implicit now with Flow, but you can trigger a refresh if needed
                    }
                }
            }
            uiState.budgets.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No budgets set.\nTap Edit to set a budget for a category.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.budgets) { budget ->
                        BudgetItem(
                            budget = budget,
                            currencyPreference = uiState.currencyPreference,
                            onEditClick = {
                                showEditDialog = budget
                                editAmountText = if (budget.limitAmount > 0.0) {
                                    String.format("%.2f", budget.limitAmount)
                                } else {
                                    ""
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Edit Dialog Logic (Kept exactly as you had it)
    showEditDialog?.let { budget ->
        AlertDialog(
            onDismissRequest = { showEditDialog = null },
            title = { Text("Edit Budget: ${budget.category.name}") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = editAmountText,
                        onValueChange = { editAmountText = it },
                        label = { Text("Budget Limit") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        prefix = { Text("Ksh ") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = editAmountText.toDoubleOrNull() ?: 0.0
                        // Allow 0.0 to clear budget
                        if (amount >= 0) {
                            viewModel.updateBudgetLimit(budget.category.id, amount)
                            showEditDialog = null
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Your BudgetItem composable and helper functions remain exactly the same below...
// (I did not modify them as they looked perfect)

@Composable
fun BudgetItem(
    budget: BudgetState,
    currencyPreference: com.kp.momoney.data.local.CurrencyPreference,
    onEditClick: () -> Unit
) {
    val progress = if (budget.limitAmount > 0) (budget.spentAmount / budget.limitAmount) else 0.0
    val progressColor = when {
        progress >= 1.0 -> Color(0xFFF44336) // Red
        progress >= 0.9 -> Color(0xFFFF9800) // Orange/Yellow
        else -> Color(0xFF4CAF50) // Green
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Category Icon Placeholders (Same logic as yours)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(try { Color(android.graphics.Color.parseColor(budget.category.color)) } catch(e:Exception) { MaterialTheme.colorScheme.primary }),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getIconByName(budget.category.icon),
                            contentDescription = budget.category.name,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = budget.category.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${budget.spentAmount.toCurrency(currencyPreference.exchangeRate, currencyPreference.currencySymbol)} / ${budget.limitAmount.toCurrency(currencyPreference.exchangeRate, currencyPreference.currencySymbol)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Budget"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { progress.toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Percentage Text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${String.format("%.1f", (progress * 100))}% used",
                    style = MaterialTheme.typography.bodySmall,
                    color = progressColor,
                    fontWeight = FontWeight.Medium
                )

                if (budget.limitAmount > 0) {
                    val remaining = budget.limitAmount - budget.spentAmount
                    Text(
                        text = if (remaining >= 0) {
                            "${remaining.toCurrency(currencyPreference.exchangeRate, currencyPreference.currencySymbol)} remaining"
                        } else {
                            "${(-remaining).toCurrency(currencyPreference.exchangeRate, currencyPreference.currencySymbol)} over budget"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (remaining >= 0) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            Color(0xFFF44336)
                        },
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}