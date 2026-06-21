package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Asset
import com.example.data.model.AssetSuggestionEngine
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AssetSearchModule(
    onAddAssetClick: (Asset, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedAsset by remember { mutableStateOf<Asset?>(null) }
    val focusManager = LocalFocusManager.current

    val allAssets = AssetSuggestionEngine.availableAssets

    // Filtered assets list
    val filteredAssets = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            allAssets
        } else {
            val list = allAssets.filter {
                it.ticker.contains(searchQuery, ignoreCase = true) ||
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
            }.toMutableList()

            // If the search query has 3+ chars and doesn't exactly match any known asset, synthesize it!
            val trimmedQuery = searchQuery.trim().uppercase()
            if (trimmedQuery.length >= 3 && list.none { it.ticker == trimmedQuery }) {
                val isFii = trimmedQuery.endsWith("11")
                val isAcao = trimmedQuery.any { it.isDigit() } && !isFii
                val category = if (isFii) "FII" else if (isAcao) "Ação" else "Tesouro Direto"
                val annualYield = if (isFii) 0.102 else if (isAcao) 0.082 else 0.105
                val averagePrice = if (isFii) 85.00 else if (isAcao) 28.50 else 1000.00
                val frequency = if (isFii) "MENSAL" else if (isAcao) "TRIMESTRAL" else "MENSAL"
                val name = if (isFii) {
                    "$trimmedQuery - Fundo de Investimento Imobiliário"
                } else if (isAcao) {
                    "$trimmedQuery S.A."
                } else {
                    "$trimmedQuery Ativo"
                }
                val description = if (isFii) {
                    "Fundo imobiliário gerado dinamicamente para complementar sua simulação. Estimado com dividendo de 10.2% ao ano pago mensalmente."
                } else if (isAcao) {
                    "Ação corporativa gerada dinamicamente para simular dividend yield consistente de 8.2% ao ano pago trimestralmente."
                } else {
                    "Ativo financeiro de renda fixa gerado dinamicamente com rendimento estimado de 10.5% ao ano (ex: Tesouro Direto)."
                }
                
                list.add(
                    Asset(
                        ticker = trimmedQuery,
                        name = name,
                        category = category,
                        annualYield = annualYield,
                        averagePrice = averagePrice,
                        frequency = frequency,
                        description = description
                    )
                )
            }
            list
        }
    }

    // Default simulation units multiplier
    var simulationUnits by remember { mutableStateOf(100) }

    var editedPrice by remember { mutableStateOf("") }
    var editedYield by remember { mutableStateOf("") }

    LaunchedEffect(selectedAsset) {
        selectedAsset?.let {
            editedPrice = String.format(Locale.US, "%.2f", it.averagePrice)
            editedYield = String.format(Locale.US, "%.1f", it.annualYield * 100.0)
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("asset_search_module")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Pesquisar Ativos Brasileiros",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Ações pagadoras de dividendos, FIIs excelentes e títulos do Tesouro",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // Search input field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    // Auto-select first matching if exact match is written (including generated assets)
                    val trimmed = it.trim().uppercase()
                    val exactMatch = filteredAssets.firstOrNull { asset ->
                        asset.ticker.equals(trimmed, ignoreCase = true)
                    }
                    if (exactMatch != null) {
                        selectedAsset = exactMatch
                    }
                },
                placeholder = { Text("Pesquisar por ticker ou nome (ex: MXRF11, ITSA4...)") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                searchQuery = ""
                                focusManager.clearFocus()
                            },
                            modifier = Modifier.testTag("search_clear_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpar busca"
                            )
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("asset_search_input_field"),
                shape = RoundedCornerShape(12.dp)
            )

            // Horizontal quick suggestion tags
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Sugestões de Ativos Populares:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Prepopulate with a subset of popular ones
                    val popularTickers = listOf("MXRF11", "HGLG11", "ITSA4", "TAEE11", "BBAS3", "TESOURO SELIC")
                    val popularAssets = allAssets.filter { it.ticker in popularTickers }

                    items(popularAssets, key = { it.ticker }) { asset ->
                        val isSelected = selectedAsset?.ticker == asset.ticker
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                                )
                                .clickable {
                                    selectedAsset = asset
                                    focusManager.clearFocus()
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("search_tag_${asset.ticker}")
                        ) {
                            Text(
                                text = asset.ticker,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            // Results matching text indicator or detail section
            if (filteredAssets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum ativo encontrado para \"$searchQuery\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (selectedAsset == null && searchQuery.isNotEmpty()) {
                // Show matching list to pick
                Text(
                    text = "Resultados Encontrados (${filteredAssets.size}):",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f))
                        .padding(4.dp)
                ) {
                    filteredAssets.take(5).forEach { asset ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedAsset = asset
                                    focusManager.clearFocus()
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .testTag("search_result_row_${asset.ticker}"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = asset.ticker,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = asset.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when (asset.category.uppercase()) {
                                            "FII" -> Color(0xFFE8F5E9)      // light green
                                            "AÇÃO" -> Color(0xFFE3F2FD)     // light blue
                                            else -> Color(0xFFFFF8E1)       // light gold
                                        }
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = asset.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (asset.category.uppercase()) {
                                        "FII" -> Color(0xFF2E7D32)
                                        "AÇÃO" -> Color(0xFF1565C0)
                                        else -> Color(0xFFF57F17)
                                    }
                                )
                            }
                        }
                    }
                    if (filteredAssets.size > 5) {
                        Text(
                            text = "+ ${filteredAssets.size - 5} mais ativos correspondentes. Refine sua busca.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Expanded Preview of the Selected Asset details layout
            AnimatedVisibility(
                visible = selectedAsset != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                selectedAsset?.let { asset ->
                    val parsedPrice = editedPrice.replace(",", ".").toDoubleOrNull() ?: asset.averagePrice
                    val parsedYieldPercent = editedYield.replace(",", ".").toDoubleOrNull() ?: (asset.annualYield * 100.0)
                    val parsedYield = parsedYieldPercent / 100.0

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.03f))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Header with close button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = asset.ticker,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "—",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            when (asset.category.uppercase()) {
                                                "FII" -> Color(0xFFE8F5E9)
                                                "AÇÃO" -> Color(0xFFE3F2FD)
                                                else -> Color(0xFFFFF8E1)
                                            }
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = asset.category,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = when (asset.category.uppercase()) {
                                            "FII" -> Color(0xFF2E7D32)
                                            "AÇÃO" -> Color(0xFF1565C0)
                                            else -> Color(0xFFF57F17)
                                        }
                                    )
                                }
                            }

                            // Close Details action
                            IconButton(
                                onClick = { selectedAsset = null },
                                modifier = Modifier
                                    .size(24.dp)
                                    .testTag("close_asset_details_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Fechar detalhes do ativo",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Company Full Name & Frequency
                        Column {
                            Text(
                                text = asset.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Proventos distribuídos de forma: ${asset.frequency.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pt", "BR")) else it.toString() }}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                        // Stats Grid (Yield & Reference price) - Customizer
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = editedPrice,
                                onValueChange = { editedPrice = it },
                                label = { Text("Preço Unitário (R$)", fontSize = 11.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f).testTag("search_edit_price_field")
                            )

                            OutlinedTextField(
                                value = editedYield,
                                onValueChange = { editedYield = it },
                                label = { Text("Yield Esperado (% a.a.)", fontSize = 11.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f).testTag("search_edit_yield_field")
                            )
                        }

                        // Text explanation / description Portuguese
                        Text(
                            text = asset.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            lineHeight = 16.sp
                        )

                        // Compounding Simulator Section (What if I bought X)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Simulador Inteligente para ${asset.ticker}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                
                                // Unit selector row
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf(10, 100, 1000).forEach { qty ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(
                                                    if (simulationUnits == qty) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.surface
                                                )
                                                .clickable { simulationUnits = qty }
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "$qty",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (simulationUnits == qty) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }

                            val simulatedInvestment = simulationUnits * parsedPrice
                            val periodYieldFactor = when (asset.frequency.uppercase()) {
                                "MENSAL" -> parsedYield / 12.0
                                "TRIMESTRAL" -> parsedYield / 4.0
                                "SEMESTRAL" -> parsedYield / 2.0
                                else -> parsedYield
                            }
                            val simulatedIncomeReturn = simulatedInvestment * periodYieldFactor
                            val simulatedAnnualReturn = simulatedInvestment * parsedYield

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Custo da Operação", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                    Text(
                                        text = String.format(Locale("pt", "BR"), "R$ %,.2f", simulatedInvestment),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Retorno por Período", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                    Text(
                                        text = String.format(Locale("pt", "BR"), "R$ %,.2f", simulatedIncomeReturn),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Retorno Anual", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                    Text(
                                        text = String.format(Locale("pt", "BR"), "R$ %,.2f", simulatedAnnualReturn),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // Add to wallet action trigger button with prefill layout parameters
                        Button(
                            onClick = {
                                val updatedAsset = asset.copy(
                                    averagePrice = parsedPrice,
                                    annualYield = parsedYield
                                )
                                onAddAssetClick(updatedAsset, simulationUnits)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("btn_add_searched_asset_to_wallet")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Adicionar $simulationUnits ${if (asset.category == "Tesouro Direto") "títulos" else "cotas"} de ${asset.ticker} à Carteira",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
