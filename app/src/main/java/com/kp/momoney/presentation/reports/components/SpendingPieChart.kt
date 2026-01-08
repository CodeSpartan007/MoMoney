package com.kp.momoney.presentation.reports.components

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.kp.momoney.presentation.reports.CategoryPercentage

@Composable
fun SpendingPieChart(
    items: List<CategoryPercentage>,
    modifier: Modifier = Modifier
) {
    val total = items.sumOf { it.totalAmount }.toFloat().coerceAtLeast(0.01f)
    val fallbackColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        // Use the full size for the pie chart (no stroke width needed)
        val diameter = size.minDimension
        val topLeft = Offset(
            (size.width - diameter) / 2f,
            (size.height - diameter) / 2f
        )

        val arcRect = Rect(topLeft, androidx.compose.ui.geometry.Size(diameter, diameter))

        var startAngle = -90f

        items.forEach { item ->
            val sweep = (item.totalAmount.toFloat() / total) * 360f
            val color = parseColorOrDefault(item.colorHex, fallbackColor)

            // Draw filled arc (pie slice)
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true,
                topLeft = arcRect.topLeft,
                size = arcRect.size
            )

            // Draw white border between slices
            drawArc(
                color = Color.White,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true,
                style = Stroke(width = 2.dp.toPx()),
                topLeft = arcRect.topLeft,
                size = arcRect.size
            )

            startAngle += sweep
        }
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

