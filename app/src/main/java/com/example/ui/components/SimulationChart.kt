package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AssetSuggestionEngine
import java.util.*

@Composable
fun SimulationChart(
    timeline: List<AssetSuggestionEngine.SimulationYear>,
    targetCapitalNeeded: Double,
    modifier: Modifier = Modifier
) {
    // Filter to key checkpoint years: 1, 5, 10, 15, 20, 25
    val visibleMilestones = timeline.filter { it.year in listOf(1, 5, 10, 15, 20, 25) }
    
    if (visibleMilestones.isEmpty()) return

    val maxAmount = (visibleMilestones.maxOf { it.totalAccumulated } * 1.15).coerceAtLeast(targetCapitalNeeded * 1.15).coerceAtLeast(1000.0)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Evolução do Patrimônio (Projeção 25 anos)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(vertical = 8.dp)
        ) {
            val primaryColor = MaterialTheme.colorScheme.primary
            val secondaryColor = MaterialTheme.colorScheme.secondary
            val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            val textColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)

            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val paddingBottom = 25f
                val paddingLeft = 10f
                val paddingRight = 10f
                val graphHeight = height - paddingBottom
                val graphWidth = width - paddingLeft - paddingRight

                // Draw Grid & Target Threshold Line representation
                val targetRatio = (targetCapitalNeeded / maxAmount).toFloat()
                val targetY = graphHeight - (targetRatio * graphHeight)

                if (targetCapitalNeeded > 0 && targetY in 0f..graphHeight) {
                    // Draw target capital horizontal line
                    drawLine(
                        color = secondaryColor.copy(alpha = 0.6f),
                        start = Offset(paddingLeft, targetY),
                        end = Offset(width - paddingRight, targetY),
                        strokeWidth = 4f,
                        pathEffect = null
                    )
                }

                // Compile coordinates for the path
                val points = visibleMilestones.mapIndexed { index, m ->
                    val x = paddingLeft + (index.toFloat() / (visibleMilestones.size - 1)) * graphWidth
                    val ratio = (m.totalAccumulated / maxAmount).toFloat()
                    val y = graphHeight - (ratio * graphHeight)
                    Offset(x, y)
                }

                // Draw area gradient / filled area below spline
                if (points.isNotEmpty()) {
                    val fillPath = Path().apply {
                        moveTo(points.first().x, graphHeight)
                        points.forEach { lineTo(it.x, it.y) }
                        lineTo(points.last().x, graphHeight)
                        close()
                    }
                    drawPath(
                        path = fillPath,
                        color = primaryColor.copy(alpha = 0.12f)
                    )

                    // Draw main line path
                    val strokePath = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        points.forEach { lineTo(it.x, it.y) }
                    }
                    drawPath(
                        path = strokePath,
                        color = primaryColor,
                        style = Stroke(width = 8f)
                    )

                    // Draw point markers
                    points.forEachIndexed { index, offset ->
                        // Draw outer circle
                        drawCircle(
                            color = primaryColor,
                            radius = 10f,
                            center = offset
                        )
                        // Draw inner white center
                        drawCircle(
                            color = Color.White,
                            radius = 5f,
                            center = offset
                        )
                    }
                }

                // Base Line
                drawLine(
                    color = gridColor,
                    start = Offset(paddingLeft, graphHeight),
                    end = Offset(width - paddingRight, graphHeight),
                    strokeWidth = 3f
                )
            }
        }

        // Horizontal axis labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            visibleMilestones.forEach { milestone ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Ano ${milestone.year}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = if (milestone.totalAccumulated >= 1000000) {
                            String.format(Locale("pt", "BR"), "R$ %.1fM", milestone.totalAccumulated / 1000000.0)
                        } else {
                            String.format(Locale("pt", "BR"), "R$ %.0fk", milestone.totalAccumulated / 1000.0)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Threshold explanation badge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = String.format(
                    Locale("pt", "BR"),
                    "Patrimônio necessário para cobrir despesas permanentes: R$ %,.2f (linha amarela)",
                    targetCapitalNeeded
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
