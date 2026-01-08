package com.kp.momoney.presentation.reports.components

import android.graphics.Paint
import android.graphics.Typeface
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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kp.momoney.util.toCurrency

@Composable
fun IncomeExpenseBarChart(
    income: Double,
    expense: Double,
    currencyPreference: com.kp.momoney.data.local.CurrencyPreference,
    modifier: Modifier = Modifier
) {
    val chartHeight = 200.dp
    val barWidth = 80.dp
    val spacing = 40.dp
    val padding = 60.dp
    val leftAxisPadding = 50.dp // Space for axis labels
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
                    val chartAreaStartX = leftAxisPadding.toPx()
                    val chartAreaWidth = canvasWidth - chartAreaStartX

                    // Calculate bar positions (centered in chart area)
                    val chartCenterX = chartAreaStartX + chartAreaWidth / 2f
                    val leftBarX = chartCenterX - barWidth.toPx() / 2f - spacing.toPx() / 2f
                    val rightBarX = chartCenterX + spacing.toPx() / 2f

                    // Calculate bar heights
                    val incomeHeight = (income / maxVal * canvasHeight).toFloat().coerceAtLeast(0f)
                    val expenseHeight = (expense / maxVal * canvasHeight).toFloat().coerceAtLeast(0f)

                    // Grid line color (transparent gray)
                    val gridColor = Color.Gray.copy(alpha = 0.3f)

                    // Step A: Draw Grid Lines (5 steps: 0%, 25%, 50%, 75%, 100%)
                    val gridSteps = 5
                    for (i in 0 until gridSteps) {
                        val percentage = i / (gridSteps - 1).toFloat() // 0.0, 0.25, 0.5, 0.75, 1.0
                        val y = canvasHeight - (percentage * canvasHeight)

                        // Draw horizontal grid line
                        drawLine(
                            color = gridColor,
                            start = Offset(chartAreaStartX, y),
                            end = Offset(canvasWidth, y),
                            strokeWidth = 1.dp.toPx()
                        )

                        // Step B: Draw Text Labels using nativeCanvas
                        val labelValue = maxVal * percentage
                        val labelText = labelValue.toCurrency(currencyPreference.exchangeRate, currencyPreference.currencySymbol)

                        // Setup Paint for text
                        val textPaint = Paint().apply {
                            color = textColor.toArgb()
                            textSize = 12.sp.toPx()
                            textAlign = Paint.Align.RIGHT
                            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                            isAntiAlias = true
                        }

                        // Draw text label slightly above the grid line
                        drawContext.canvas.nativeCanvas.drawText(
                            labelText,
                            chartAreaStartX - 8.dp.toPx(), // Position to the left of chart area
                            y - 4.dp.toPx(), // Slightly above the line
                            textPaint
                        )
                    }

                    // Step C: Draw Bars (on top of grid lines)
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
                        text = income.toCurrency(currencyPreference.exchangeRate, currencyPreference.currencySymbol),
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
                        text = expense.toCurrency(currencyPreference.exchangeRate, currencyPreference.currencySymbol),
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

