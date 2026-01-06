package com.kp.momoney.presentation.category

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kp.momoney.R
import com.kp.momoney.domain.model.Category
import com.kp.momoney.presentation.common.LoadingOverlay
import com.kp.momoney.util.getIconByName
import java.util.Locale

// Preset colors for category selection
val PRESET_COLORS = listOf(
    "F44336" to "Red",
    "FF9800" to "Orange",
    "FFEB3B" to "Yellow",
    "4CAF50" to "Green",
    "2196F3" to "Blue",
    "9C27B0" to "Purple",
    "E91E63" to "Pink",
    "00BCD4" to "Cyan"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagerScreen(
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val categoryName by viewModel.categoryName.collectAsState()
    val categoryType by viewModel.categoryType.collectAsState()
    val selectedColor by viewModel.selectedColor.collectAsState()
    val userCategories by viewModel.userCategories.collectAsState()
    val event by viewModel.event.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()
    val isLoadingDelete by viewModel.isLoadingDelete.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Handle events
    LaunchedEffect(event) {
        val currentEvent = event
        when (currentEvent) {
            is CategoryEvent.Success -> {
                // Check if this is a delete or create event based on context
                if (selectedCategoryId == null) {
                    snackbarHostState.showSnackbar("Category created successfully!")
                } else {
                    snackbarHostState.showSnackbar("Category deleted successfully!")
                }
                viewModel.clearEvent()
            }
            is CategoryEvent.Error -> {
                snackbarHostState.showSnackbar(currentEvent.message)
                viewModel.clearEvent()
            }
            null -> {}
        }
    }

    Scaffold(
        topBar = {
            if (selectedCategoryId != null) {
                // Contextual TopBar when category is selected
                CenterAlignedTopAppBar(
                    title = {
                        Text(text = "Delete Category?")
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close"
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showDeleteDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            } else {
                // Normal TopBar
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
                            Text(text = "Manage Categories")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Creation Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Create New Category",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        // Name Input
                        OutlinedTextField(
                            value = categoryName,
                            onValueChange = { viewModel.onNameChanged(it) },
                            label = { Text("Category Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Type Selection (Income/Expense)
                        Text(
                            text = "Type",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { viewModel.onTypeChanged("Income") }
                            ) {
                                RadioButton(
                                    selected = categoryType == "Income",
                                    onClick = { viewModel.onTypeChanged("Income") }
                                )
                                Text(
                                    text = "Income",
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { viewModel.onTypeChanged("Expense") }
                            ) {
                                RadioButton(
                                    selected = categoryType == "Expense",
                                    onClick = { viewModel.onTypeChanged("Expense") }
                                )
                                Text(
                                    text = "Expense",
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }

                        // Color Picker
                        Text(
                            text = "Color",
                            style = MaterialTheme.typography.labelLarge
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(PRESET_COLORS) { (colorHex, colorName) ->
                                ColorSwatch(
                                    colorHex = colorHex,
                                    colorName = colorName,
                                    isSelected = selectedColor == colorHex,
                                    onClick = { viewModel.onColorSelected(colorHex) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Create Button
                        Button(
                            onClick = { viewModel.createCategory() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = categoryName.trim().isNotEmpty() && !isLoading
                        ) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Create Category")
                        }
                    }
                }
            }

            // List Section
            item {
                Text(
                    text = "Your Custom Categories",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (userCategories.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = "No custom categories yet. Create one above!",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(userCategories) { category ->
                    CategoryItem(
                        category = category,
                        isSelected = selectedCategoryId == category.id,
                        onLongClick = { viewModel.onCategoryLongClick(category.id) }
                    )
                }
            }
            }
            
            // Loading Overlay for creation
            LoadingOverlay(isLoading = isLoading)
            
            // Loading Overlay for deletion (full screen)
            if (isLoadingDelete) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    com.kp.momoney.presentation.common.AppLoadingAnimation()
                }
            }
        }
        
        // Confirmation Dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Category") },
                text = {
                    Text("Delete this category? This will permanently remove all related transactions and budgets.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteDialog = false
                            viewModel.deleteSelectedCategory()
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showDeleteDialog = false }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun ColorSwatch(
    colorHex: String,
    colorName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = Color(android.graphics.Color.parseColor("#$colorHex"))
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color)
                .then(
                    if (isSelected) {
                        Modifier.border(
                            width = 3.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                    } else {
                        Modifier.border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                    }
                )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = colorName,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun CategoryItem(
    category: Category,
    isSelected: Boolean = false,
    onLongClick: () -> Unit = {}
) {
    val color = try {
        Color(android.graphics.Color.parseColor("#${category.color}"))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    )
                } else {
                    Modifier
                }
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onLongClick = onLongClick
                ) {}
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Color indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            
            // Icon
            Icon(
                imageVector = getIconByName(category.icon),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = category.type,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

