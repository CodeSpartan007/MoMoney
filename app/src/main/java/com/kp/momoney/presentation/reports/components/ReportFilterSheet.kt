package com.kp.momoney.presentation.reports.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportFilterSheet(
    onDismiss: () -> Unit,
    onApply: (startDate: Long?, endDate: Long?) -> Unit,
    onReset: () -> Unit,
    currentDateRange: Pair<Long, Long>?
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()
    
    // Local state for date range
    var startDate by remember { mutableStateOf<Long?>(currentDateRange?.first) }
    var endDate by remember { mutableStateOf<Long?>(currentDateRange?.second) }
    
    // Date range picker with interaction source
    val dateInteractionSource = remember { MutableInteractionSource() }
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Format date range string for display
    val dateRangeString = remember(startDate, endDate) {
        val start = startDate
        val end = endDate
        if (start != null && end != null) {
            formatDateRange(start, end)
        } else {
            "All Time"
        }
    }
    
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
                text = "Filter Reports by Date",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Date Range Section
            Text(
                text = "Date Range",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Date Input Field with interaction source
            LaunchedEffect(dateInteractionSource) {
                dateInteractionSource.interactions.collect { interaction ->
                    if (interaction is PressInteraction.Release) {
                        showDatePicker = true
                    }
                }
            }
            
            OutlinedTextField(
                value = dateRangeString,
                onValueChange = { },
                label = { Text("Select Date Range") },
                readOnly = true, // CRITICAL: Prevents keyboard
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                    }
                },
                interactionSource = dateInteractionSource,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        startDate = null
                        endDate = null
                        onReset()
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reset")
                }
                
                Button(
                    onClick = {
                        onApply(startDate, endDate)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Apply")
                }
            }
        }
    }
    
    // Date Range Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = startDate,
            initialSelectedEndDateMillis = endDate
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                        // Update state here
                        val start = datePickerState.selectedStartDateMillis
                        val end = datePickerState.selectedEndDateMillis
                        if (start != null && end != null) {
                            startDate = start
                            endDate = end
                        } else {
                            startDate = null
                            endDate = null
                        }
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DateRangePicker(
                state = datePickerState,
                modifier = Modifier.height(500.dp) // Sometimes needed to prevent layout clip
            )
        }
    }
}

private fun formatDateRange(startDate: Long, endDate: Long): String {
    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    val startFormatted = dateFormat.format(java.util.Date(startDate))
    val endFormatted = dateFormat.format(java.util.Date(endDate))
    return "$startFormatted - $endFormatted"
}

