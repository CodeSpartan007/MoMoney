package com.kp.momoney.presentation.reports.components

import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Typeface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kp.momoney.presentation.reports.DailyPoint

@Composable
fun DailyTrendChart(
    data: List<DailyPoint>,
    modifier: Modifier = Modifier
) {
    val chartHeight = 200.dp
    val padding = 60.dp
    val bottomPadding = 40.dp // Extra space for X-axis labels
    val gridColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val lineColor = MaterialTheme.colorScheme.primary
    val labelTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(chartHeight + padding + bottomPadding)
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
                    .height(chartHeight + bottomPadding)
            ) {
                val canvasHeight = size.height - bottomPadding.toPx()
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

                // Step 1: Draw Grid Lines (3 lines at 25%, 50%, 75%)
                val gridPercentages = listOf(0.25f, 0.50f, 0.75f)
                val dashPathEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
                
                gridPercentages.forEach { percentage ->
                    val y = canvasHeight - verticalPadding - (availableHeight * percentage)
                    val gridPaint = android.graphics.Paint().apply {
                        color = gridColor.toArgb()
                        style = android.graphics.Paint.Style.STROKE
                        strokeWidth = 1.dp.toPx()
                        pathEffect = dashPathEffect
                    }
                    drawContext.canvas.nativeCanvas.drawLine(
                        horizontalPadding,
                        y,
                        canvasWidth - horizontalPadding,
                        y,
                        gridPaint
                    )
                }

                // Step 2: Draw smooth line with Bezier curves and gradient fill
                if (points.size > 1) {
                    // Create smooth path using cubic Bezier curves
                    val smoothPath = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        
                        for (i in 0 until points.size - 1) {
                            val current = points[i]
                            val next = points[i + 1]
                            
                            // Calculate control points (midpoint approach for smoothness)
                            val controlPoint1X = current.x + (next.x - current.x) / 3f
                            val controlPoint1Y = current.y
                            val controlPoint2X = current.x + 2 * (next.x - current.x) / 3f
                            val controlPoint2Y = next.y
                            
                            cubicTo(
                                x1 = controlPoint1X,
                                y1 = controlPoint1Y,
                                x2 = controlPoint2X,
                                y2 = controlPoint2Y,
                                x3 = next.x,
                                y3 = next.y
                            )
                        }
                    }

                    // Create filled path (closed to bottom)
                    val filledPath = Path().apply {
                        // Copy the smooth path
                        addPath(smoothPath)
                        // Close to bottom
                        lineTo(points.last().x, canvasHeight - verticalPadding)
                        lineTo(points.first().x, canvasHeight - verticalPadding)
                        close()
                    }

                    // Draw gradient fill
                    val gradientBrush = Brush.verticalGradient(
                        colors = listOf(
                            lineColor.copy(alpha = 0.4f),
                            lineColor.copy(alpha = 0.0f)
                        ),
                        startY = points.minOfOrNull { it.y } ?: 0f,
                        endY = canvasHeight - verticalPadding
                    )
                    drawPath(
                        path = filledPath,
                        brush = gradientBrush
                    )

                    // Draw main line with rounded cap
                    drawPath(
                        path = smoothPath,
                        color = lineColor,
                        style = Stroke(
                            width = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    )
                }

                // Step 3: Draw X-axis date labels
                val textPaint = Paint().apply {
                    color = labelTextColor.toArgb()
                    textSize = 10.sp.toPx()
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    isAntiAlias = true
                }

                // Draw date labels (show every few days to avoid crowding)
                val labelInterval = maxOf(1, dataSize / 5) // Show ~5 labels
                points.forEachIndexed { index, point ->
                    if (index % labelInterval == 0 || index == points.size - 1) {
                        val dayLabel = data[index].day.toString()
                        drawContext.canvas.nativeCanvas.drawText(
                            dayLabel,
                            point.x,
                            canvasHeight + 20.dp.toPx(),
                            textPaint
                        )
                    }
                }

                // Step 4: Draw points (hollow circle for last point)
                points.forEachIndexed { index, point ->
                    if (index == points.size - 1) {
                        // Draw hollow circle for current day (last point)
                        drawCircle(
                            color = lineColor,
                            radius = 6.dp.toPx(),
                            center = point,
                            style = Stroke(width = 2.dp.toPx())
                        )
                        // Inner fill
                        drawCircle(
                            color = lineColor.copy(alpha = 0.3f),
                            radius = 3.dp.toPx(),
                            center = point
                        )
                    } else {
                        // Regular filled circles for other points
                        drawCircle(
                            color = lineColor,
                            radius = 4.dp.toPx(),
                            center = point
                        )
                    }
                }
            }
        }
    }
}

