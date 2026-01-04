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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kp.momoney.domain.model.Category
import com.kp.momoney.domain.repository.CategoryRepository
import com.kp.momoney.util.getIconByName
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheet(
    onDismiss: () -> Unit,
    onApply: (
        startDate: Long?,
        endDate: Long?,
        selectedType: String?,
        selectedCategories: List<Int>
    ) -> Unit,
    onReset: () -> Unit,
    categoryRepository: CategoryRepository,
    currentDateRange: Pair<Long, Long>?,
    currentType: String?,
    currentCategories: List<Int>
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    // Local state for filters
    var startDate by remember { mutableStateOf<Long?>(currentDateRange?.first) }
    var endDate by remember { mutableStateOf<Long?>(currentDateRange?.second) }
    var selectedType by remember { mutableStateOf<String?>(currentType) }
    var selectedCategories by remember { mutableStateOf<List<Int>>(currentCategories) }
    
    // Load categories
    val categories by categoryRepository.getAllCategories().collectAsState(initial = emptyList())
    
    // Date picker states
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
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
            
            // Date Pickers
            if (showStartDatePicker) {
                DatePickerDialog(
                    onDateSelected = { date ->
                        startDate = date
                        showStartDatePicker = false
                    },
                    onDismiss = { showStartDatePicker = false }
                )
            }
            
            if (showEndDatePicker) {
                DatePickerDialog(
                    onDateSelected = { date ->
                        endDate = date
                        showEndDatePicker = false
                    },
                    onDismiss = { showEndDatePicker = false }
                )
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
                        onReset()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reset")
                }
                
                Button(
                    onClick = {
                        onApply(startDate, endDate, selectedType, selectedCategories)
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

@Composable
private fun DatePickerDialog(
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    // Simple date picker - for production, use Material3's DatePickerDialog
    // or a library like date-time-picker
    val calendar = Calendar.getInstance()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Select Date",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Simple date selection buttons
            // Note: For a full date picker, use Material3 DatePickerDialog or a date picker library
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        onDateSelected(calendar.timeInMillis)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Today")
                }
                
                Button(
                    onClick = {
                        calendar.add(Calendar.DAY_OF_MONTH, -7)
                        onDateSelected(calendar.timeInMillis)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("7 Days Ago")
                }
                
                Button(
                    onClick = {
                        calendar.add(Calendar.MONTH, -1)
                        onDateSelected(calendar.timeInMillis)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("1 Month Ago")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}

