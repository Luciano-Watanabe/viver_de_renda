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
import androidx.compose.material.icons.filled.Star
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

data class WealthMonthProjection(
    val monthLabel: String,
    val accumulatedContributions: Double, // total from pocket
    val accumulatedGains: Double,         // compound earnings / dividends
    val totalWealth: Double,              // sum of both
    val monthEarnings: Double             // passive income earned in this month
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MonthlyWealthEvolutionChart(
    purchasedAssets: List<PurchasedAsset>,
    settings: InvestmentSettings,
    modifier: Modifier = Modifier
) {
    // 1. Calculate Portfolio Worth & Yield
    val totalInvested = purchasedAssets.sumOf { it.quantity * it.purchasePrice }
    val totalMonthlyReturn = purchasedAssets.sumOf { (it.quantity * it.purchasePrice * it.annualYield) / 12.0 }
    
    val portfolioYield = if (totalInvested > 0) {
        (totalMonthlyReturn * 12.0) / totalInvested
    } else {
        0.085 // 8.5% fallback standard yield if empty
    }

    val monthlyYieldFactor = portfolioYield / 12.0
    val initialInput = settings.initialInvestment
    val monthlyContribution = settings.monthlyContribution

    // 2. Generate 12 Months step-by-step evolution data
    val projections = remember(totalInvested, initialInput, monthlyContribution, monthlyYieldFactor, purchasedAssets.size) {
        val list = mutableListOf<WealthMonthProjection>()
        val calendar = Calendar.getInstance()
        val monthFormat = SimpleDateFormat("MMM/yy", Locale("pt", "BR"))

        var currentContributions = totalInvested + initialInput
        var currentGains = 0.0
        var currentCapital = currentContributions + currentGains

        // Start with Month 0 (Current state)
        list.add(
            WealthMonthProjection(
                monthLabel = "Hoje",
                accumulatedContributions = currentContributions,
                accumulatedGains = currentGains,
                totalWealth = currentCapital,
                monthEarnings = 0.0
            )
        )

        for (i in 1..12) {
            calendar.add(Calendar.MONTH, 1)
            val monthLabel = monthFormat.format(calendar.time).replaceFirstChar { 
                if (it.isLowerCase()) it.titlecase(Locale("pt", "BR")) else it.toString() 
            }

            // Passive earnings calculated on work done by current accumulated capital
            val yieldValue = currentCapital * monthlyYieldFactor
            
            // Add monthly contribution
            currentContributions += monthlyContribution
            currentGains += yieldValue
            currentCapital = currentContributions + currentGains

            list.add(
                WealthMonthProjection(
                    monthLabel = monthLabel,
                    accumulatedContributions = currentContributions,
                    accumulatedGains = currentGains,
                    totalWealth = currentCapital,
                    monthEarnings = yieldValue
                )
            )
        }
        list
    }

    var selectedIndex by remember { mutableStateOf(projections.lastIndex) } // defaults to final year projection
    val highlightedPoint = projections.getOrNull(selectedIndex) ?: projections.last()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("wealth_evolution_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Content
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Evolução do Patrimônio (12 Meses)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Aportes acumulados + proventos reinvestidos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Cards Grid (Initial vs Projected Final)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Starting Wealth
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Patrimônio Inicial",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = String.format(Locale("pt", "BR"), "R$ %,.2f", projections.first().totalWealth),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Carteira + Aporte Inicial",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }

                // Final Projected Wealth
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Patrimônio em 12 Meses",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = String.format(Locale("pt", "BR"), "R$ %,.2f", projections.last().totalWealth),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        val totalNetYield = projections.last().accumulatedGains
                        Text(
                            text = String.format(Locale("pt", "BR"), "Rendimentos: +R$ %,.2f", totalNetYield),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            // Interactive Selector View details
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)),
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
                            text = "Mês Selecionado: ${highlightedPoint.monthLabel}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = String.format(
                                Locale("pt", "BR"),
                                "Aportes: R$ %,.2f",
                                highlightedPoint.accumulatedContributions
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = String.format(
                                Locale("pt", "BR"),
                                "Rendimentos: +R$ %,.2f",
                                highlightedPoint.accumulatedGains
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Patrimônio Total",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format(Locale("pt", "BR"), "R$ %,.2f", highlightedPoint.totalWealth),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (highlightedPoint.monthEarnings > 0) {
                            Text(
                                text = String.format(Locale("pt", "BR"), "+R$ %,.2f renda este mês", highlightedPoint.monthEarnings),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            // Canvas Chart Drawing of Stacked Columns (Contributions in grey/blue, returns in green)
            val maxVal = (projections.maxOf { it.totalWealth } * 1.15).coerceAtLeast(100.0)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(vertical = 4.dp)
            ) {
                val primaryColor = MaterialTheme.colorScheme.primary
                val colorContributions = primaryColor.copy(alpha = 0.35f)
                val colorGains = MaterialTheme.colorScheme.secondary
                val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                val hoverBgColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
                val baselineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(projections) {
                            detectTapGestures { offset ->
                                val width = size.width
                                val paddingLeft = 50f
                                val paddingRight = 10f
                                val graphWidth = width - paddingLeft - paddingRight
                                val barSpacingWidth = graphWidth / projections.size
                                
                                val relativeX = offset.x - paddingLeft
                                if (relativeX >= 0 && relativeX < graphWidth) {
                                    val tappedIndex = (relativeX / barSpacingWidth).toInt().coerceIn(0, projections.size - 1)
                                    selectedIndex = tappedIndex
                                }
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val paddingBottom = 40f
                    val paddingLeft = 80f
                    val paddingRight = 10f
                    val graphHeight = height - paddingBottom
                    val graphWidth = width - paddingLeft - paddingRight

                    val barCount = projections.size
                    val barSpacingWidth = graphWidth / barCount
                    val barWidth = barSpacingWidth * 0.65f

                    // Draw Horizontal dashed lines
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

                    // Draw Stacked Bars
                    projections.forEachIndexed { i, pt ->
                        val xCenter = paddingLeft + (i * barSpacingWidth) + (barSpacingWidth / 2f)
                        val barLeft = xCenter - (barWidth / 2f)
                        
                        // Contributions bar
                        val contribRatio = (pt.accumulatedContributions / maxVal).toFloat()
                        val contribHeight = contribRatio * graphHeight
                        val contribTop = graphHeight - contribHeight

                        // Gains bar (stacked on top of contributions)
                        val gainsRatio = (pt.accumulatedGains / maxVal).toFloat()
                        val gainsHeight = gainsRatio * graphHeight
                        val gainsTop = contribTop - gainsHeight

                        // Selected hover highlight column
                        if (selectedIndex == i) {
                            drawRect(
                                color = hoverBgColor,
                                topLeft = Offset(paddingLeft + (i * barSpacingWidth), 0f),
                                size = Size(barSpacingWidth, graphHeight)
                            )
                        }

                        // Draw Contributions component
                        if (contribHeight > 0) {
                            drawRoundRect(
                                color = colorContributions,
                                topLeft = Offset(barLeft, contribTop),
                                size = Size(barWidth, contribHeight),
                                cornerRadius = CornerRadius(0f, 0f)
                            )
                        }

                        // Draw Gains component (fully stacked above, rounded at peak)
                        if (gainsHeight > 0) {
                            drawRoundRect(
                                color = colorGains,
                                topLeft = Offset(barLeft, gainsTop),
                                size = Size(barWidth, gainsHeight),
                                cornerRadius = CornerRadius(6f, 6f)
                            )
                        } else {
                            // If no gains yet, round the contributions bar top instead
                            if (contribHeight > 0) {
                                drawRoundRect(
                                    color = colorContributions,
                                    topLeft = Offset(barLeft, contribTop),
                                    size = Size(barWidth, contribHeight),
                                    cornerRadius = CornerRadius(6f, 6f)
                                )
                            }
                        }

                        // Pointer Dot on Top of selected bar
                        if (selectedIndex == i) {
                            val totalTop = if (gainsHeight > 0) gainsTop else contribTop
                            drawCircle(
                                color = primaryColor,
                                radius = 7f,
                                center = Offset(xCenter, totalTop)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 3.5f,
                                center = Offset(xCenter, totalTop)
                            )
                        }
                    }

                    // Base underline line
                    drawLine(
                        color = baselineColor,
                        start = Offset(paddingLeft, graphHeight),
                        end = Offset(width - paddingRight, graphHeight),
                        strokeWidth = 3f
                    )
                }
            }

            // Labels under columns
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 28.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                projections.forEachIndexed { idx, pt ->
                    // Alternate show labels to avoid overlapping on compact layouts
                    val showLabel = when {
                        projections.size <= 7 -> true
                        idx == 0 -> true
                        idx == projections.lastIndex -> true
                        idx % 2 == 0 && idx < projections.lastIndex - 1 -> true
                        else -> false
                    }

                    Text(
                        text = if (showLabel) pt.monthLabel else "",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = if (pt.monthLabel == "Hoje") 10.sp else 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Legend indicators below chart
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pocket Contributions
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                    )
                    Text(
                        text = "Aportes Acumulados",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Passive earnings
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.secondary)
                    )
                    Text(
                        text = "Rendimento Gerado",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            // Rentabilidade info notice
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = String.format(
                        Locale("pt", "BR"),
                        "Rendimento calculado com base na média ponderada da sua carteira: %.2f%% ao ano. Caso a carteira esteja vazia, simula o referencial de 8.50%% a.a.",
                        portfolioYield * 100.0
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    lineHeight = 11.sp
                )
            }
        }
    }
}
