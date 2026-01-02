package com.kp.momoney.presentation.reports.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kp.momoney.presentation.reports.DailyPoint

@Composable
fun DailyTrendChart(
    data: List<DailyPoint>,
    modifier: Modifier = Modifier
) {
    val chartHeight = 200.dp
    val padding = 60.dp
    val axisColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(chartHeight + padding)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (data.isEmpty() || data.all { it.amount == 0.0 }) {
            Text(
                text = "No data available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        } else {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(chartHeight)
            ) {
                val canvasHeight = size.height
                val canvasWidth = size.width
                val horizontalPadding = 20.dp.toPx()
                val verticalPadding = 20.dp.toPx()

                val maxAmount = data.maxOfOrNull { it.amount }?.coerceAtLeast(1.0) ?: 1.0
                val dataSize = data.size.coerceAtLeast(1)

                // Calculate X-axis step
                val availableWidth = canvasWidth - (horizontalPadding * 2)
                val xStep = if (dataSize > 1) {
                    availableWidth / (dataSize - 1)
                } else {
                    0f
                }

                // Calculate Y-axis scale
                val availableHeight = canvasHeight - (verticalPadding * 2)
                val yScale = availableHeight / maxAmount.toFloat()

                // Create points
                val points = data.mapIndexed { index, point ->
                    val x = horizontalPadding + (index * xStep)
                    val y = canvasHeight - verticalPadding - (point.amount * yScale).toFloat()
                    Offset(x, y)
                }

                // Draw line
                if (points.size > 1) {
                    val path = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        points.drop(1).forEach { point ->
                            lineTo(point.x, point.y)
                        }
                    }

                    drawPath(
                        path = path,
                        color = Color(0xFF2196F3), // Blue
                        style = Stroke(width = 3.dp.toPx())
                    )
                }

                // Draw circles at each point
                points.forEach { point ->
                    drawCircle(
                        color = Color(0xFF2196F3),
                        radius = 5.dp.toPx(),
                        center = point
                    )
                    // Draw inner white circle for better visibility
                    drawCircle(
                        color = Color.White,
                        radius = 2.dp.toPx(),
                        center = point
                    )
                }

                // Draw axes (optional, for better visualization)
                
                // X-axis (bottom line)
                drawLine(
                    color = axisColor,
                    start = Offset(horizontalPadding, canvasHeight - verticalPadding),
                    end = Offset(canvasWidth - horizontalPadding, canvasHeight - verticalPadding),
                    strokeWidth = 1.dp.toPx()
                )

                // Y-axis (left line)
                drawLine(
                    color = axisColor,
                    start = Offset(horizontalPadding, verticalPadding),
                    end = Offset(horizontalPadding, canvasHeight - verticalPadding),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }
    }
}

