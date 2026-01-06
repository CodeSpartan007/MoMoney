package com.kp.momoney.presentation.reports

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kp.momoney.presentation.common.AppLoadingAnimation
import com.kp.momoney.presentation.reports.components.IncomeExpenseBarChart
import com.kp.momoney.presentation.reports.components.DailyTrendChart
import com.kp.momoney.presentation.reports.components.ReportFilterSheet
import com.kp.momoney.util.toCurrency
import com.kp.momoney.R
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val filterDateRange by viewModel.filterDateRange.collectAsState()
    val selectedTabIndex = remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    var showBottomSheet by remember { mutableStateOf(false) }

    // Format title based on filter
    val titleText = remember(filterDateRange) {
        val range = filterDateRange
        if (range == null) {
            "Reports"
        } else {
            val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            val startFormatted = dateFormat.format(java.util.Date(range.first))
            val endFormatted = dateFormat.format(java.util.Date(range.second))
            "Reports ($startFormatted - $endFormatted)"
        }
    }

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
                        Text(text = titleText)
                    }
                },
                actions = {
                    IconButton(onClick = { showBottomSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Filter by Date"
                        )
                    }
                    IconButton(onClick = { viewModel.exportData(context) }) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Export"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val tabs = listOf("Overview", "Trends")

            TabRow(selectedTabIndex = selectedTabIndex.intValue) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex.intValue == index,
                        onClick = { selectedTabIndex.intValue = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTabIndex.intValue) {
                0 -> ReportsOverviewTab(uiState, viewModel)
                1 -> ReportsTrendsTab(viewModel)
            }
        }
    }

    // Filter Bottom Sheet
    if (showBottomSheet) {
        ReportFilterSheet(
            onDismiss = { showBottomSheet = false },
            onApply = { startDate, endDate ->
                val range = if (startDate != null && endDate != null) {
                    Pair(startDate, endDate)
                } else {
                    null
                }
                viewModel.onDateRangeChanged(range)
            },
            onReset = {
                viewModel.onDateRangeChanged(null)
            },
            currentDateRange = filterDateRange
        )
    }
}

@Composable
private fun ReportsOverviewTab(
    uiState: ReportsUiState,
    viewModel: ReportsViewModel
) {
    val incomeExpenseState by viewModel.incomeExpenseState.collectAsState()

    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AppLoadingAnimation(modifier = Modifier.align(Alignment.Center))
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Section 1: Spending by Category
        if (uiState.items.isNotEmpty() && uiState.totalExpense > 0.0) {
            Text(
                text = "Spending by Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(16.dp))

            DonutChart(
                items = uiState.items,
                modifier = Modifier.size(220.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.items.forEach { item ->
                    CategoryLegendRow(item = item)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Section 2: Income vs Expense
        Text(
            text = "Income vs Expense",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(16.dp))

        IncomeExpenseBarChart(
            income = incomeExpenseState.income,
            expense = incomeExpenseState.expense
        )

        if (uiState.items.isEmpty() && incomeExpenseState.income == 0.0 && 
            incomeExpenseState.expense == 0.0) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No data available.\nAdd some transactions to see your reports.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReportsTrendsTab(
    viewModel: ReportsViewModel
) {
    val dailyTrendState by viewModel.dailyTrendState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Daily Spending Pattern",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Last 30 Days",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        DailyTrendChart(data = dailyTrendState)

        if (dailyTrendState.isEmpty() || dailyTrendState.all { it.amount == 0.0 }) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No data available.\nAdd some transactions to see your trends.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DonutChart(
    items: List<CategoryPercentage>,
    modifier: Modifier = Modifier
) {
    val total = items.sumOf { it.totalAmount }.toFloat().coerceAtLeast(0.01f)
    val fallbackColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.18f
        val diameter = size.minDimension - strokeWidth
        val topLeft = Offset(
            (size.width - diameter) / 2f,
            (size.height - diameter) / 2f
        )

        val arcRect = Rect(topLeft, androidx.compose.ui.geometry.Size(diameter, diameter))

        var startAngle = -90f

        items.forEach { item ->
            val sweep = (item.totalAmount.toFloat() / total) * 360f
            val color = parseColorOrDefault(item.colorHex, fallbackColor)

            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = strokeWidth),
                topLeft = arcRect.topLeft,
                size = arcRect.size
            )

            startAngle += sweep
        }
    }
}

@Composable
private fun CategoryLegendRow(
    item: CategoryPercentage
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(parseColorOrDefault(item.colorHex, MaterialTheme.colorScheme.primary))
        )

        Spacer(modifier = Modifier.size(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.categoryName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = item.totalAmount.toCurrency(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = "${"%.1f".format(item.percentage)}%",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun parseColorOrDefault(colorHex: String, fallback: Color): Color {
    return try {
        if (colorHex.isBlank()) {
            fallback
        } else {
            val normalized = if (colorHex.startsWith("#")) colorHex else "#$colorHex"
            Color(android.graphics.Color.parseColor(normalized))
        }
    } catch (e: Exception) {
        fallback
    }
}


