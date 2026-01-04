package com.kp.momoney.presentation.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kp.momoney.domain.model.Category
import com.kp.momoney.domain.repository.CategoryRepository
import com.kp.momoney.util.getIconByName
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheet(
    onDismiss: () -> Unit,
    onApply: (
        startDate: Long?,
        endDate: Long?,
        selectedType: String?,
        selectedCategories: List<Int>,
        minAmount: String?,
        maxAmount: String?
    ) -> Unit,
    onReset: () -> Unit,
    categoryRepository: CategoryRepository,
    currentDateRange: Pair<Long, Long>?,
    currentType: String?,
    currentCategories: List<Int>,
    currentMinAmount: String?,
    currentMaxAmount: String?
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()
    
    // Local state for filters
    var startDate by remember { mutableStateOf<Long?>(currentDateRange?.first) }
    var endDate by remember { mutableStateOf<Long?>(currentDateRange?.second) }
    var selectedType by remember { mutableStateOf<String?>(currentType) }
    var selectedCategories by remember { mutableStateOf<List<Int>>(currentCategories) }
    var minAmount by remember { mutableStateOf<String>(currentMinAmount ?: "") }
    var maxAmount by remember { mutableStateOf<String>(currentMaxAmount ?: "") }
    
    // Load categories
    val categories by categoryRepository.getAllCategories().collectAsState(initial = emptyList())
    
    // Date picker states
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    
    // Date picker states for Material 3 DatePicker
    val startDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = startDate
    )
    val endDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = endDate
    )
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Filter Transactions",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Date Range Section
            Text(
                text = "Date Range",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = startDate?.let { formatDate(it) } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showStartDatePicker = true },
                    label = { Text("Start Date") }
                )
                
                OutlinedTextField(
                    value = endDate?.let { formatDate(it) } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showEndDatePicker = true },
                    label = { Text("End Date") }
                )
            }
            
            // Amount Range Section
            Text(
                text = "Amount Range",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = minAmount,
                    onValueChange = { minAmount = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Min Amount") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )
                
                OutlinedTextField(
                    value = maxAmount,
                    onValueChange = { maxAmount = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Max Amount") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )
            }
            
            // Date Pickers
            if (showStartDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showStartDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                startDatePickerState.selectedDateMillis?.let {
                                    startDate = it
                                }
                                showStartDatePicker = false
                            }
                        ) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showStartDatePicker = false }
                        ) {
                            Text("Cancel")
                        }
                    }
                ) {
                    DatePicker(state = startDatePickerState)
                }
            }
            
            if (showEndDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showEndDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                endDatePickerState.selectedDateMillis?.let {
                                    endDate = it
                                }
                                showEndDatePicker = false
                            }
                        ) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showEndDatePicker = false }
                        ) {
                            Text("Cancel")
                        }
                    }
                ) {
                    DatePicker(state = endDatePickerState)
                }
            }
            
            // Transaction Type Section
            Text(
                text = "Transaction Type",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedType == null,
                    onClick = { selectedType = null },
                    label = { Text("All") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
                FilterChip(
                    selected = selectedType == "Income",
                    onClick = { selectedType = "Income" },
                    label = { Text("Income") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
                FilterChip(
                    selected = selectedType == "Expense",
                    onClick = { selectedType = "Expense" },
                    label = { Text("Expense") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
            
            // Categories Section
            Text(
                text = "Categories",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = category.id in selectedCategories,
                        onClick = {
                            selectedCategories = if (category.id in selectedCategories) {
                                selectedCategories - category.id
                            } else {
                                selectedCategories + category.id
                            }
                        },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = getIconByName(category.icon),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(category.name)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        startDate = null
                        endDate = null
                        selectedType = null
                        selectedCategories = emptyList()
                        minAmount = ""
                        maxAmount = ""
                        onReset()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reset")
                }
                
                Button(
                    onClick = {
                        onApply(
                            startDate,
                            endDate,
                            selectedType,
                            selectedCategories,
                            minAmount.takeIf { it.isNotBlank() },
                            maxAmount.takeIf { it.isNotBlank() }
                        )
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Apply")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}

