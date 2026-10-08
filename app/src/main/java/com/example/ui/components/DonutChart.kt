package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.viewmodel.CategoryExpenseSummary

@Composable
fun DonutChart(
    summaries: List<CategoryExpenseSummary>,
    totalExpense: Double,
    modifier: Modifier = Modifier,
    chartSize: Dp = 180.dp,
    strokeWidth: Dp = 26.dp
) {
    val totalSpent = if (totalExpense > 0) totalExpense else summaries.sumOf { it.totalSpent }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .testTag("donut_chart")
            .size(chartSize)
    ) {
        Canvas(modifier = Modifier.size(chartSize)) {
            val strokePx = strokeWidth.toPx()

            // Draw base track
            drawCircle(
                color = Color.LightGray.copy(alpha = 0.2f),
                style = Stroke(width = strokePx)
            )

            if (totalSpent > 0 && summaries.isNotEmpty()) {
                var startAngle = -90f
                for (summary in summaries) {
                    val sweepAngle = (summary.totalSpent / totalSpent).toFloat() * 360f
                    if (sweepAngle > 0.5f) {
                        drawArc(
                            color = summary.categoryInfo.color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = strokePx, cap = StrokeCap.Butt)
                        )
                        startAngle += sweepAngle
                    }
                }
            }
        }

        // Center total label
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Total Spent",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = String.format("$%.2f", totalSpent),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
