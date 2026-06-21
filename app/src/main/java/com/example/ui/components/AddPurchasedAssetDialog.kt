package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssetSuggestionEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPurchasedAssetDialog(
    initialTicker: String = "",
    initialName: String = "",
    initialCategory: String = "Ação",
    initialQuantity: String = "",
    initialPurchasePrice: String = "",
    initialAnnualYield: String = "",
    onDismiss: () -> Unit,
    onConfirm: (
        ticker: String,
        name: String,
        category: String,
        quantity: Int,
        purchasePrice: Double,
        annualYield: Double
    ) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val suggestedAssets = AssetSuggestionEngine.availableAssets
    
    var selectedSuggestedAssetIndex by remember { 
        mutableStateOf(suggestedAssets.indexOfFirst { it.ticker == initialTicker.uppercase().trim() })
    }
    var dropdownExpanded by remember { mutableStateOf(false) }

    var ticker by remember { mutableStateOf(initialTicker) }
    var name by remember { mutableStateOf(initialName) }
    var category by remember { mutableStateOf(initialCategory) } // "Ação", "FII", "Tesouro Direto"
    var quantityStr by remember { mutableStateOf(initialQuantity) }
    var purchasePriceStr by remember { mutableStateOf(initialPurchasePrice) }
    var annualYieldStr by remember { mutableStateOf(initialAnnualYield) }

    var showError by remember { mutableStateOf(false) }

    val categories = listOf("Ação", "FII", "Tesouro Direto")

    AlertDialog(
        onDismissRequest = {
            focusManager.clearFocus()
            onDismiss()
        },
        title = { Text(text = "Adicionar Ativo à Carteira", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Dropdown to pick from suggestions
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = if (selectedSuggestedAssetIndex >= 0) {
                            "${suggestedAssets[selectedSuggestedAssetIndex].ticker} - ${suggestedAssets[selectedSuggestedAssetIndex].name}"
                        } else {
                            "Digitar Ativo Customizado..."
                        },
                        onValueChange = {},
                        label = { Text("Ativo Recomendado / Sugerido") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { dropdownExpanded = !dropdownExpanded }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Mostrar ativos")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { dropdownExpanded = !dropdownExpanded }
                            .testTag("suggested_asset_select")
                    )

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Digitar Ativo Customizado ✏️", fontWeight = FontWeight.Bold) },
                            onClick = {
                                selectedSuggestedAssetIndex = -1
                                ticker = ""
                                name = ""
                                annualYieldStr = ""
                                purchasePriceStr = ""
                                dropdownExpanded = false
                            }
                        )
                        Divider()
                        suggestedAssets.forEachIndexed { index, asset ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${asset.ticker} - ${asset.name}", fontWeight = FontWeight.Medium)
                                        Text(
                                            text = asset.category,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                onClick = {
                                    selectedSuggestedAssetIndex = index
                                    ticker = asset.ticker
                                    name = asset.name
                                    category = asset.category
                                    purchasePriceStr = asset.averagePrice.toString()
                                    annualYieldStr = (asset.annualYield * 100.0).toString()
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Ticker Input
                OutlinedTextField(
                    value = ticker,
                    onValueChange = { 
                        ticker = it
                        // If user manual edits, unselect preset if it doesn't match
                        if (selectedSuggestedAssetIndex >= 0 && suggestedAssets[selectedSuggestedAssetIndex].ticker != it.uppercase().trim()) {
                            selectedSuggestedAssetIndex = -1
                        }
                    },
                    label = { Text("Ticker (Código)") },
                    placeholder = { Text("Ex: ITSA4, MXRF11, etc.") },
                    modifier = Modifier.fillMaxWidth().testTag("asset_ticker_input"),
                    singleLine = true
                )

                // Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome do Ativo") },
                    placeholder = { Text("Ex: Itaúsa S.A.") },
                    modifier = Modifier.fillMaxWidth().testTag("asset_name_input"),
                    singleLine = true
                )

                // Category selection if custom
                if (selectedSuggestedAssetIndex == -1) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Categoria",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            categories.forEach { cat ->
                                val isSelected = category == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { category = cat },
                                    label = { Text(cat, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                } else {
                    // Show fixed category
                    Text(
                        text = "Categoria: $category",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // Quantity Input
                OutlinedTextField(
                    value = quantityStr,
                    onValueChange = { quantityStr = it },
                    label = { Text("Quantidade Adquirida") },
                    placeholder = { Text("Ex: 100") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("asset_qty_input"),
                    singleLine = true
                )

                // Purchase Price Input
                OutlinedTextField(
                    value = purchasePriceStr,
                    onValueChange = { purchasePriceStr = it },
                    label = { Text("Preço Pago (por Cota/Título)") },
                    placeholder = { Text("Ex: 10.50") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("asset_price_input"),
                    singleLine = true,
                    leadingIcon = { Text("R$ ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 8.dp)) }
                )

                // Expected Annual Yield (DY%)
                OutlinedTextField(
                    value = annualYieldStr,
                    onValueChange = { annualYieldStr = it },
                    label = { Text("Dividend Yield Anual Esperado (%)") },
                    placeholder = { Text("Ex: 8.5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("asset_yield_input"),
                    singleLine = true,
                    trailingIcon = { Text("% ", fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp)) }
                )

                if (showError) {
                    Text(
                        text = "Por favor, preencha todos os campos corretamente (quantidade e preço devem ser maiores que zero).",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qtyParsed = quantityStr.toIntOrNull()
                    val priceParsed = purchasePriceStr.replace(",", ".").toDoubleOrNull()
                    val dYParsed = annualYieldStr.replace(",", ".").toDoubleOrNull()

                    val nameToSubmit = if (name.isNotBlank()) name.trim() else ticker.uppercase().trim()

                    if (ticker.isNotBlank() && qtyParsed != null && qtyParsed > 0 && priceParsed != null && priceParsed > 0) {
                        focusManager.clearFocus()
                        // Annual Yield is submitted as double coefficient: e.g. 8.5% is 0.085
                        val yieldCoeff = if (dYParsed != null) dYParsed / 100.0 else 0.08
                        onConfirm(
                            ticker.uppercase().trim(),
                            nameToSubmit,
                            category,
                            qtyParsed,
                            priceParsed,
                            yieldCoeff
                        )
                        onDismiss()
                    } else {
                        showError = true
                    }
                },
                modifier = Modifier.testTag("asset_confirm_button")
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    focusManager.clearFocus()
                    onDismiss()
                }
            ) {
                Text("Cancelar")
            }
        }
    )
}
