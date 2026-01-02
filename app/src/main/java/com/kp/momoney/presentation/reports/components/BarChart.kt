package com.kp.momoney.presentation.reports.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun IncomeExpenseBarChart(
    income: Double,
    expense: Double,
    modifier: Modifier = Modifier
) {
    val chartHeight = 200.dp
    val barWidth = 80.dp
    val spacing = 40.dp
    val padding = 60.dp
    val textColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(chartHeight + padding)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (income == 0.0 && expense == 0.0) {
            Text(
                text = "No data available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        } else {
            Box(modifier = Modifier.fillMaxWidth()) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(chartHeight)
                ) {
                    val maxVal = maxOf(income, expense).coerceAtLeast(1.0)
                    val canvasHeight = size.height
                    val canvasWidth = size.width

                    // Calculate bar positions (centered)
                    val centerX = canvasWidth / 2f
                    val leftBarX = centerX - barWidth.toPx() / 2f - spacing.toPx() / 2f
                    val rightBarX = centerX + spacing.toPx() / 2f

                    // Calculate bar heights
                    val incomeHeight = (income / maxVal * canvasHeight).toFloat().coerceAtLeast(0f)
                    val expenseHeight = (expense / maxVal * canvasHeight).toFloat().coerceAtLeast(0f)

                    // Draw bars
                    val incomeColor = Color(0xFF4CAF50) // Green
                    val expenseColor = Color(0xFFF44336) // Red

                    // Income bar (left)
                    drawRect(
                        color = incomeColor,
                        topLeft = Offset(leftBarX, canvasHeight - incomeHeight),
                        size = androidx.compose.ui.geometry.Size(barWidth.toPx(), incomeHeight)
                    )

                    // Expense bar (right)
                    drawRect(
                        color = expenseColor,
                        topLeft = Offset(rightBarX, canvasHeight - expenseHeight),
                        size = androidx.compose.ui.geometry.Size(barWidth.toPx(), expenseHeight)
                    )
                }

                // Overlay text labels using Text composables
                val maxVal = maxOf(income, expense).coerceAtLeast(1.0)
                val incomeHeightDp = (income / maxVal * chartHeight.value).dp
                val expenseHeightDp = (expense / maxVal * chartHeight.value).dp
                val leftOffset = -spacing / 2 - barWidth / 2
                val rightOffset = spacing / 2 + barWidth / 2

                // Income value label
                if (income > 0) {
                    Text(
                        text = "$${"%.2f".format(income)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(x = leftOffset, y = chartHeight - incomeHeightDp - 24.dp)
                    )
                }

                // Expense value label
                if (expense > 0) {
                    Text(
                        text = "$${"%.2f".format(expense)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(x = rightOffset, y = chartHeight - expenseHeightDp - 24.dp)
                    )
                }

                // Bar labels below
                Text(
                    text = "Inc",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(x = leftOffset, y = (-8).dp)
                )

                Text(
                    text = "Exp",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(x = rightOffset, y = (-8).dp)
                )
            }
        }
    }
}

