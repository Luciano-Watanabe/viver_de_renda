package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InvestmentSettings
import com.example.data.model.PurchasedAsset
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MonthlyIncomeBarChart(
    purchasedAssets: List<PurchasedAsset>,
    settings: InvestmentSettings,
    totalFixedCosts: Double,
    modifier: Modifier = Modifier
) {
    // Generate 12-month projections
    val totalInvested = purchasedAssets.sumOf { it.quantity * it.purchasePrice }
    val totalMonthlyReturn = purchasedAssets.sumOf { (it.quantity * it.purchasePrice * it.annualYield) / 12.0 }
    
    // Choose appropriate yield rate
    val portfolioYield = if (totalInvested > 0) {
        (totalMonthlyReturn * 12.0) / totalInvested
    } else {
        0.085 // 8.5% fallback
    }
    
    val monthlyYieldFactor = portfolioYield / 12.0
    val monthlyContribution = settings.monthlyContribution
    val startCapital = if (totalInvested > 0) totalInvested else settings.initialInvestment

    // Calculate month data point projection
    val dataPoints = remember(startCapital, monthlyContribution, monthlyYieldFactor, purchasedAssets.size) {
        val list = mutableListOf<MonthProjection>()
        val calendar = Calendar.getInstance()
        var currentCapital = startCapital

        val monthFormat = SimpleDateFormat("MMM/yy", Locale("pt", "BR"))

        for (i in 1..12) {
            calendar.add(Calendar.MONTH, 1)
            // Reinvest dividends and add contribution
            val monthlyYieldValue = currentCapital * monthlyYieldFactor
            currentCapital = currentCapital + monthlyYieldValue + monthlyContribution
            
            list.add(
                MonthProjection(
                    label = monthFormat.format(calendar.time).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pt", "BR")) else it.toString() },
                    estimatedIncome = monthlyYieldValue,
                    projectedCapital = currentCapital
                )
            )
        }
        list
    }

    var selectedIndex by remember { mutableStateOf(5) } // default highlight middle

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
            .testTag("monthly_income_bar_chart"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Projeção Mensal Próximos 12 Meses",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Compounding de proventos + aportes estimados",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            // Format badge info
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Recharts Style",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Active highlighted month card
        val highlightedPoint = dataPoints.getOrNull(selectedIndex) ?: dataPoints.last()
        val coveragePercentage = if (totalFixedCosts > 0) {
            (highlightedPoint.estimatedIncome / totalFixedCosts) * 100.0
        } else {
            100.0
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Mês Selecionado: ${highlightedPoint.label}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = String.format(Locale("pt", "BR"), "Contas de R$ %,.2f", totalFixedCosts),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = String.format(Locale("pt", "BR"), "R$ %,.2f", highlightedPoint.estimatedIncome),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = String.format(Locale("pt", "BR"), "%.1f%% Coberto", coveragePercentage),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (coveragePercentage >= 100.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        // Custom chart canvas drawing standard layout
        val maxVal = (dataPoints.maxOf { it.estimatedIncome } * 1.2).coerceAtLeast(totalFixedCosts * 1.2).coerceAtLeast(100.0)
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(vertical = 4.dp)
        ) {
            val primaryColor = MaterialTheme.colorScheme.primary
            val secondaryColor = MaterialTheme.colorScheme.secondary
            val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            val hoverBgColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
            val baselineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(dataPoints) {
                        detectTapGestures { offset ->
                            val width = size.width
                            val paddingLeft = 40f
                            val paddingRight = 10f
                            val graphWidth = width - paddingLeft - paddingRight
                            val barSpacingWidth = graphWidth / dataPoints.size
                            
                            val relativeX = offset.x - paddingLeft
                            if (relativeX >= 0 && relativeX < graphWidth) {
                                val tappedIndex = (relativeX / barSpacingWidth).toInt().coerceIn(0, dataPoints.size - 1)
                                selectedIndex = tappedIndex
                            }
                        }
                    }
            ) {
                val width = size.width
                val height = size.height
                val paddingBottom = 40f
                val paddingLeft = 100f
                val paddingRight = 10f
                val graphHeight = height - paddingBottom
                val graphWidth = width - paddingLeft - paddingRight

                val barCount = dataPoints.size
                val barSpacingWidth = graphWidth / barCount
                val barWidth = barSpacingWidth * 0.6f

                // Horizontal dashed grid values
                val gridYCount = 4
                for (j in 0..gridYCount) {
                    val yVal = (maxVal / gridYCount) * j
                    val y = graphHeight - (yVal / maxVal).toFloat() * graphHeight
                    
                    drawLine(
                        color = gridColor,
                        start = Offset(paddingLeft, y),
                        end = Offset(width - paddingRight, y),
                        strokeWidth = 2f
                    )
                }

                // Reference despesas line if within limits
                if (totalFixedCosts > 0 && totalFixedCosts <= maxVal) {
                    val limitY = graphHeight - (totalFixedCosts / maxVal).toFloat() * graphHeight
                    drawLine(
                        color = secondaryColor.copy(alpha = 0.8f),
                        start = Offset(paddingLeft, limitY),
                        end = Offset(width - paddingRight, limitY),
                        strokeWidth = 4f
                    )
                }

                // Draw each bar
                dataPoints.forEachIndexed { i, pt ->
                    val xCenter = paddingLeft + (i * barSpacingWidth) + (barSpacingWidth / 2f)
                    val barLeft = xCenter - (barWidth / 2f)
                    
                    val heightRatio = (pt.estimatedIncome / maxVal).toFloat()
                    val barHeight = heightRatio * graphHeight
                    val barTop = graphHeight - barHeight

                    // Draw focus background bar
                    if (selectedIndex == i) {
                        drawRect(
                            color = hoverBgColor,
                            topLeft = Offset(paddingLeft + (i * barSpacingWidth), 0f),
                            size = Size(barSpacingWidth, graphHeight)
                        )
                    }

                    // Draw primary bar
                    if (barHeight > 0) {
                        val brush = Brush.verticalGradient(
                            colors = listOf(
                                primaryColor,
                                primaryColor.copy(alpha = 0.6f)
                            )
                        )
                        // Round top corners
                        drawRoundRect(
                            brush = brush,
                            topLeft = Offset(barLeft, barTop),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(8f, 8f)
                        )
                    }

                    // Dot at the top of active selected bar
                    if (selectedIndex == i) {
                        drawCircle(
                            color = primaryColor,
                            radius = 8f,
                            center = Offset(xCenter, barTop)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 4f,
                            center = Offset(xCenter, barTop)
                        )
                    }
                }

                // Base indicator baseline line
                drawLine(
                    color = baselineColor,
                    start = Offset(paddingLeft, graphHeight),
                    end = Offset(width - paddingRight, graphHeight),
                    strokeWidth = 3f
                )
            }
        }

        // Horizontal label names
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Pick subset of labels to prevent overlap clutter, e.g. alternate or quarters
            dataPoints.forEachIndexed { idx, pt ->
                // Show alternate months or index depending on responsive
                if (idx % 2 == 0) {
                    Text(
                        text = pt.label.replace("/26", "").replace("/27", ""),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selectedIndex == idx) FontWeight.Black else FontWeight.Bold,
                        color = if (selectedIndex == idx) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedIndex = idx }
                            .padding(vertical = 4.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // Chart legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Proventos Estimados",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .height(3.dp)
                        .background(MaterialTheme.colorScheme.secondary)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Custo Fixo Mapeado",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

data class MonthProjection(
    val label: String,
    val estimatedIncome: Double,
    val projectedCapital: Double
)
