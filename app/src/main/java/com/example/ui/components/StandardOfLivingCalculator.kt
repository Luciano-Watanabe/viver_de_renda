package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InvestmentSettings
import com.example.data.model.PurchasedAsset
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StandardOfLivingCalculator(
    purchasedAssets: List<PurchasedAsset>,
    settings: InvestmentSettings,
    totalFixedCosts: Double,
    modifier: Modifier = Modifier
) {
    // Current portfolio capital worth
    val totalInvestedPortfolio = purchasedAssets.sumOf { it.quantity * it.purchasePrice }

    // Prefined standards of living goals (monthly passive income)
    val standardPresets = remember(totalFixedCosts) {
        listOf(
            LivingStandardPreset("Básico", 3500.0, "🏠 Essencial", "Cobre moradia, alimentação básica e serviços utilitários básicos."),
            LivingStandardPreset("Confortável", 8000.0, "🚗 Conforto", "Inclui lazer, veículo, assinaturas e saídas frequentes."),
            LivingStandardPreset("Próspero", 18000.0, "✈️ Independência", "Altos padrões de consumo, viagens recorrentes e estilo de vida premium."),
            LivingStandardPreset("Mapeado", if (totalFixedCosts > 0) totalFixedCosts else 2500.0, "🎯 Seus Gastos", "Calculado diretamente de suas Contas Fixas ativas atuais.")
        )
    }

    var selectedPresetIndex by remember { mutableStateOf(3) } // Default to "Mapeado" / "Seus Gastos"
    var customMonthlyTarget by remember { mutableStateOf(5000.0) }
    var useCustomTarget by remember { mutableStateOf(false) }

    // Expected annual yield rate input (default to portfolio average or fallback to 8.5%)
    val defaultYield = if (purchasedAssets.isNotEmpty()) {
        val annualEarnings = purchasedAssets.sumOf { it.quantity * it.purchasePrice * it.annualYield }
        val portfolioWorth = purchasedAssets.sumOf { it.quantity * it.purchasePrice }
        if (portfolioWorth > 0) annualEarnings / portfolioWorth else 0.085
    } else {
        0.085
    }

    var expectedAnnualYield by remember { mutableStateOf(defaultYield) }
    var yieldInputString by remember { mutableStateOf(String.format(Locale.US, "%.1f", defaultYield * 100)) }

    // Option to include configured Cash / Initial investment in Current Assets
    var includeInitialInvestment by remember { mutableStateOf(true) }

    // Final computations
    val activeMonthlyGoal = if (useCustomTarget) customMonthlyTarget else standardPresets[selectedPresetIndex].monthlyValue
    val targetCapitalNeeded = if (expectedAnnualYield > 0) {
        (activeMonthlyGoal * 12.0) / expectedAnnualYield
    } else {
        0.0
    }

    val currentCapital = totalInvestedPortfolio + (if (includeInitialInvestment) settings.initialInvestment else 0.0)
    val capitalGap = (targetCapitalNeeded - currentCapital).coerceAtLeast(0.0)
    val progressPercent = if (targetCapitalNeeded > 0) {
        (currentCapital / targetCapitalNeeded) * 100.0
    } else {
        100.0
    }

    // Compounding simulation months left
    // We assume realistic annual real return adjusted for inflation or user value
    val simulationRateAnnual = expectedAnnualYield
    val timeToGoalMonths = remember(currentCapital, targetCapitalNeeded, settings.monthlyContribution, simulationRateAnnual) {
        if (currentCapital >= targetCapitalNeeded) {
            0
        } else if (settings.monthlyContribution <= 0.0) {
            -1 // Impossible to reach without monthly savings
        } else {
            val monthlyRate = Math.pow(1.0 + simulationRateAnnual, 1.0 / 12.0) - 1.0
            var accum = currentCapital
            var months = 0
            val maxMonths = 12 * 100 // Cap at 100 years
            while (accum < targetCapitalNeeded && months < maxMonths) {
                accum = accum * (1.0 + monthlyRate) + settings.monthlyContribution
                months++
            }
            if (months >= maxMonths) -1 else months
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("standard_of_living_calculator")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Heading
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Standard of Living Map",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Calculadora de Alvo por Padrão de Vida",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Qual é o seu padrão ideal e o que falta para chegar lá?",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // Step 1: Select Living Standard Preset
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "1. Selecione o Padrão de Vida Desejado:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Grid of options
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    standardPresets.forEachIndexed { index, preset ->
                        val isSelected = !useCustomTarget && selectedPresetIndex == index
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                useCustomTarget = false
                                selectedPresetIndex = index
                            },
                            label = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text(preset.chipLabel, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(
                                        text = String.format(Locale("pt", "BR"), "R$ %,.0f", preset.monthlyValue),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("preset_chip_${preset.idName.lowercase()}")
                        )
                    }

                    // Custom input FilterChip
                    FilterChip(
                        selected = useCustomTarget,
                        onClick = { useCustomTarget = true },
                        label = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 4.dp)) {
                                Text("✍️ Personalizado", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(
                                    text = String.format(Locale("pt", "BR"), "R$ %,.0f", customMonthlyTarget),
                                    fontSize = 11.sp,
                                    color = if (useCustomTarget) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("preset_chip_custom")
                    )
                }

                // Show selected preset short description
                if (!useCustomTarget) {
                    val activePreset = standardPresets[selectedPresetIndex]
                    Text(
                        text = activePreset.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    )
                } else {
                    // Custom target value controller slider
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ajustar Renda Mensal Ideal:",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = String.format(Locale("pt", "BR"), "R$ %,.0f / mês", customMonthlyTarget),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Slider(
                            value = customMonthlyTarget.toFloat(),
                            onValueChange = { customMonthlyTarget = (it / 100).toInt() * 100.0 },
                            valueRange = 1000f..30000f,
                            modifier = Modifier.fillMaxWidth().testTag("custom_target_slider")
                        )
                    }
                }
            }

            // Step 2: Yield parameters & Capital source definition
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Yield Input Box
                Column(
                    modifier = Modifier.weight(1.2f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Rendimento Anual Médio (%)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OutlinedTextField(
                        value = yieldInputString,
                        onValueChange = { input ->
                            yieldInputString = input
                            val parsed = input.toDoubleOrNull()
                            if (parsed != null && parsed >= 1.0 && parsed <= 30.0) {
                                expectedAnnualYield = parsed / 100.0
                            }
                        },
                        placeholder = { Text("8.5") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().testTag("yield_input_calculator"),
                        suffix = { Text("% a.a.") }
                    )
                }

                // Initial Investment Source checkbox
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Patrimônio Atual",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                            .clickable { includeInitialInvestment = !includeInitialInvestment }
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Checkbox(
                            checked = includeInitialInvestment,
                            onCheckedChange = { includeInitialInvestment = it },
                            modifier = Modifier.testTag("include_initial_checkbox")
                        )
                        Column {
                            Text("Incluir Caixa", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Text(
                                text = String.format(Locale("pt", "BR"), "+ R$ %,.0f", settings.initialInvestment),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            // Step 3: Math results output dashboard
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Main metric remaining deficit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Capital Total Alvo",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = String.format(Locale("pt", "BR"), "R$ %,.0f", targetCapitalNeeded),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Aporte Mensal Atual",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = String.format(Locale("pt", "BR"), "R$ %,.0f/mês", settings.monthlyContribution),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                // The Gap Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Falta Investir (Déficit)",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (capitalGap > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = if (capitalGap > 0) {
                                String.format(Locale("pt", "BR"), "R$ %,.2f", capitalGap)
                            } else {
                                "R$ 0,00"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = if (capitalGap > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (capitalGap > 0) MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = String.format(Locale("pt", "BR"), "%.1f%% do alvo", progressPercent),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = if (capitalGap > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Dual progress Bar visualization (Current VS Gap remaining)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val progressRatio = (progressPercent / 100.0).toFloat().coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progressRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = if (capitalGap > 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = String.format(Locale("pt", "BR"), "Atual: R$ %,.0f", currentCapital),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        if (capitalGap > 0) {
                            Text(
                                text = String.format(Locale("pt", "BR"), "Falta: R$ %,.0f", capitalGap),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Completo", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(10.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Meta Atingida!", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            // Time timeline estimation message
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "⏱️", fontSize = 18.sp)
                Text(
                    text = when {
                        currentCapital >= targetCapitalNeeded -> {
                            "Status: Meta Batida! Parabéns, seu capital acumulado é autossuficiente para prover R$ ${String.format(Locale("pt", "BR"), "%,.2f", activeMonthlyGoal)}/mês a uma taxa média de ${String.format(Locale("pt", "BR"), "%.2f%%", expectedAnnualYield * 100.0)} ao ano."
                        }
                        settings.monthlyContribution <= 0.0 -> {
                            "⚠️ Você precisa configurar um valor de aporte mensal na aba '2. Simulador' para conseguir atingir esta meta se acumulando com os juros."
                        }
                        timeToGoalMonths == -1 -> {
                            "⚠️ Cuidado, no ritmo atual está levando muito tempo. Aumente o valor do aporte mensal ou selecione ativos com rendimento ligeiramente superior para acelerar."
                        }
                        else -> {
                            val years = timeToGoalMonths / 12
                            val remMonths = timeToGoalMonths % 12
                            val yearsPart = if (years > 0) "$years ${if (years == 1) "ano" else "anos"}" else ""
                            val monthSpacer = if (years > 0 && remMonths > 0) " e " else ""
                            val monthsPart = if (remMonths > 0) "$remMonths ${if (remMonths == 1) "mês" else "meses"}" else ""
                            
                            String.format(
                                Locale("pt", "BR"),
                                "Mantendo seus aportes mensais de R$ %,.2f com dividendos reinvestidos a %.1f%% de rendimento, você fechará este gap em aproximadamente %s%s%s.",
                                settings.monthlyContribution,
                                expectedAnnualYield * 100.0,
                                yearsPart,
                                monthSpacer,
                                monthsPart
                            )
                        }
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

data class LivingStandardPreset(
    val idName: String,
    val monthlyValue: Double,
    val chipLabel: String,
    val description: String
)
