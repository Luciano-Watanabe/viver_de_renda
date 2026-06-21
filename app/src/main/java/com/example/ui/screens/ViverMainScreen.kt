package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.*
import com.example.ui.viewmodel.ViverViewModel
import com.example.data.model.Asset
import com.example.data.model.PurchasedAsset
import com.example.data.model.AssetSuggestionEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViverMainScreen(
    viewModel: ViverViewModel,
    modifier: Modifier = Modifier
) {
    val bills by viewModel.allBills.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val totalCosts by viewModel.totalFixedCosts.collectAsStateWithLifecycle()
    val recommendations by viewModel.recommendations.collectAsStateWithLifecycle()
    val timeline by viewModel.simulationTimeline.collectAsStateWithLifecycle()
    val monthsToFreedom by viewModel.monthsToFinancialFreedom.collectAsStateWithLifecycle()
    val purchasedAssets by viewModel.allPurchasedAssets.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showAddAssetDialog by remember { mutableStateOf(false) }

    var prefilledTicker by remember { mutableStateOf("") }
    var prefilledName by remember { mutableStateOf("") }
    var prefilledCategory by remember { mutableStateOf("") }
    var prefilledQuantity by remember { mutableStateOf("") }
    var prefilledPurchasePrice by remember { mutableStateOf("") }
    var prefilledAnnualYield by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Wizard Tutorial State
    var showWizard by remember { mutableStateOf(false) }
    var wizardStep by remember { mutableStateOf(0) }
    var showNamePrompt by remember { mutableStateOf(false) }

    LaunchedEffect(settings.hasSeenTutorial, settings.userName) {
        if (settings.userName.isBlank()) {
            showNamePrompt = true
        } else if (!settings.hasSeenTutorial && settings.initialInvestment > 0) {
            showWizard = true
        }
    }

    // Compute approximate target capital required to generate the total fixed expenses
    // Assuming a conservative average portfolio yield of 8.5% annual (which is ~0.7% monthly)
    val averageYield = 0.085
    val targetCapitalNeeded = if (totalCosts > 0) {
        (totalCosts * 12.0) / averageYield
    } else {
        0.0
    }

    val currentWealth = settings.initialInvestment
    val progressRatio = if (targetCapitalNeeded > 0) {
        (currentWealth / targetCapitalNeeded).toFloat().coerceIn(0f, 1f)
    } else {
        1f
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Text(
                                text = "Viver de Renda",
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp
                            )
                        }
                        if (settings.userName.isNotBlank() && settings.userName != "Investidor") {
                            Text(
                                text = "Olá, ${settings.userName}!",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 2.dp, start = 44.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                ExtendedFloatingActionButton(
                    text = { Text("Nova Conta", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Adicionar conta") },
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("fab_add_bill")
                )
            } else if (selectedTab == 3) {
                ExtendedFloatingActionButton(
                    text = { Text("Adquirir Ativo", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Adicionar ativo comprado") },
                    onClick = {
                        prefilledTicker = ""
                        prefilledName = ""
                        prefilledCategory = "Ação"
                        prefilledQuantity = ""
                        prefilledPurchasePrice = ""
                        prefilledAnnualYield = ""
                        showAddAssetDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("fab_add_asset")
                )
            }
        },
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            
            // --- TOP METRIC DASHBOARD ---
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                ),
                shape = RoundedCornerShape(0.dp), // stays cohesive at the top
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("top_dashboard")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val periodFactor = when (settings.returnPeriod.uppercase()) {
                            "TRIMESTRAL" -> 3.0
                            "SEMESTRAL" -> 6.0
                            "ANUAL" -> 12.0
                            else -> 1.0
                        }
                        val periodCosts = totalCosts * periodFactor
                        val periodLabel = when (settings.returnPeriod.uppercase()) {
                            "TRIMESTRAL" -> "Despesa por Trimestre"
                            "SEMESTRAL" -> "Despesa por Semestre"
                            "ANUAL" -> "Despesa por Ano"
                            else -> "Despesa por Mês"
                        }
                        Column {
                            Text(
                                text = periodLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = String.format(Locale("pt", "BR"), "R$ %,.2f", periodCosts),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (periodFactor > 1.0) {
                                Text(
                                    text = String.format(Locale("pt", "BR"), "Ref: R$ %,.2f/mês", totalCosts),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Patrimônio Recomendado",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = String.format(Locale("pt", "BR"), "R$ %,.0f", targetCapitalNeeded),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    // Progress Metric to goal
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Cobertura pelo Aporte Inicial:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = String.format(Locale("pt", "BR"), "%.1f%% do alvo", progressRatio * 100),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        LinearProgressIndicator(
                            progress = progressRatio,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        )

                        // Target period indication
                        Text(
                            text = when {
                                monthsToFreedom == 0 -> "🎉 Seu aporte inicial já atinge o patrimônio alvo!"
                                monthsToFreedom > 12 * 60 -> "⚠️ Ajuste seus aportes para atingir a meta em menos tempo."
                                else -> {
                                    val anos = monthsToFreedom / 12
                                    val meses = monthsToFreedom % 12
                                    val anosStr = if (anos > 0) "$anos ${if (anos == 1) "ano" else "anos"}" else ""
                                    val mesesStr = if (meses > 0) " e $meses ${if (meses == 1) "mês" else "meses"}" else ""
                                    "⏳ Falta aproximadamente $anosStr$mesesStr de aportes mensais para Viver de Renda."
                                }
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            // Gamification Effect Check
            LaunchedEffect(progressRatio) {
                if (progressRatio > 0) {
                    val progressInt = (progressRatio * 100).toInt()
                    val multipleOf10 = (progressInt / 10) * 10
                    if (multipleOf10 >= 10 && multipleOf10 > settings.lastCelebratedProgress) {
                        viewModel.updateCelebratedProgress(multipleOf10)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = "🎉 Incrível! Seus rendimentos já cobrem $multipleOf10% das suas contas!",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                }
            }

            // --- NAVIGATION TABS ---
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("1. Contas Fixas", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.List, contentDescription = "Tab Contas") },
                    modifier = Modifier.height(64.dp).testTag("tab_bills")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("2. Simulador", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Tab Simulador") },
                    modifier = Modifier.height(64.dp).testTag("tab_simulator")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("3. Sugestões", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Star, contentDescription = "Tab Sugestoes") },
                    modifier = Modifier.height(64.dp).testTag("tab_suggestions")
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("4. Carteira", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Tab Carteira") },
                    modifier = Modifier.height(64.dp).testTag("tab_portfolio")
                )
            }

            // --- TAB CONTENT WITH ANIMATION ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Crossfade(
                    targetState = selectedTab,
                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 300),
                    label = "TabTransition"
                ) { currentTab ->
                    when (currentTab) {
                    0 -> {
                        // --- TAB 1: FIXED BILLS LIST ---
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentPadding = PaddingValues(bottom = 80.dp) // padding for FAB
                        ) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Suas Despesas do Dia a Dia",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Tente mapear tudo o que você precisa por mês para viver.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }

                                    TextButton(
                                        onClick = { viewModel.resetToDefaults() },
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = MaterialTheme.colorScheme.primary
                                        ),
                                        modifier = Modifier.testTag("reset_defaults_button")
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Resetar", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Padrão B3", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }

                            if (bills.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 40.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ShoppingCart,
                                                contentDescription = "Sem contas",
                                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                                modifier = Modifier.size(64.dp)
                                            )
                                            Text(
                                                text = "Sua jornada rumo à liberdade financeira começa aqui!",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                            )
                                            Text(
                                                text = "Cadastre suas despesas para descobrirmos quanto você precisa investir.",
                                                style = MaterialTheme.typography.bodyMedium,
                                                textAlign = TextAlign.Center,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                modifier = Modifier.padding(horizontal = 32.dp)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(
                                                onClick = { showAddDialog = true },
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                            ) {
                                                Text("Adicionar Primeira Despesa", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            } else {
                                items(bills, key = { it.id }) { bill ->
                                    BillItem(
                                        bill = bill,
                                        onToggleActive = { viewModel.toggleBillActive(bill) },
                                        onDelete = { viewModel.deleteBill(bill) },
                                        onUpdateValue = { newVal -> viewModel.updateBillValue(bill, newVal) }
                                    )
                                }
                            }
                        }
                    }

                    1 -> {
                        // --- TAB 2: SIMULATOR & PARAMETERS ---
                        var initialInput by remember { mutableStateOf("") }
                        var monthlyInput by remember { mutableStateOf("") }

                        // Initialize input fields once when database settings are fully loaded
                        LaunchedEffect(settings) {
                            if (initialInput.isEmpty() && settings.initialInvestment > 0.0) {
                                initialInput = settings.initialInvestment.toString()
                            }
                            if (monthlyInput.isEmpty() && settings.monthlyContribution > 0.0) {
                                monthlyInput = settings.monthlyContribution.toString()
                            }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Text(
                                            text = "Configuração do seu Plano",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )

                                        // Initial Investment Input
                                        OutlinedTextField(
                                            value = initialInput,
                                            onValueChange = { initialInput = it },
                                            label = { Text("Aporte Inicial Disponível (R$)") },
                                            placeholder = { Text("Ex: 10000") },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Decimal,
                                                imeAction = ImeAction.Next
                                            ),
                                            modifier = Modifier.fillMaxWidth().testTag("config_initial_input"),
                                            leadingIcon = { Text("R$ ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 8.dp)) }
                                        )

                                        // Monthly investment input
                                        OutlinedTextField(
                                            value = monthlyInput,
                                            onValueChange = { monthlyInput = it },
                                            label = { Text("Valor do Aporte Mensal (R$)") },
                                            placeholder = { Text("Ex: 500") },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Decimal,
                                                imeAction = ImeAction.Done
                                            ),
                                            keyboardActions = KeyboardActions(
                                                onDone = {
                                                    val initialVal = initialInput.toDoubleOrNull() ?: 0.0
                                                    val monthlyVal = monthlyInput.toDoubleOrNull() ?: 0.0
                                                    viewModel.updateInvestmentSettings(
                                                        initial = initialVal,
                                                        monthly = monthlyVal,
                                                        period = settings.returnPeriod
                                                    )
                                                    focusManager.clearFocus()
                                                }
                                            ),
                                            modifier = Modifier.fillMaxWidth().testTag("config_monthly_input"),
                                            leadingIcon = { Text("R$ ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 8.dp)) }
                                        )

                                        // Apply/Save button
                                        Button(
                                            onClick = {
                                                val initialVal = initialInput.toDoubleOrNull() ?: 0.0
                                                val monthlyVal = monthlyInput.toDoubleOrNull() ?: 0.0
                                                viewModel.updateInvestmentSettings(
                                                    initial = initialVal,
                                                    monthly = monthlyVal,
                                                    period = settings.returnPeriod
                                                )
                                                focusManager.clearFocus()
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("apply_settings_button"),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.secondary,
                                                contentColor = MaterialTheme.colorScheme.onSecondary
                                            )
                                        ) {
                                            Text("Recalcular e Atualizar Simulação", fontWeight = FontWeight.Bold)
                                        }

                                        // Return Period frequency Toggle
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(
                                                text = "Prazo que deseja o Retorno (Frequência de Proventos)",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )

                                            val periods = listOf(
                                                "MENSAL" to "Mensal",
                                                "TRIMESTRAL" to "Trimestral",
                                                "SEMESTRAL" to "Semestral",
                                                "ANUAL" to "Anual"
                                            )

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                periods.forEach { (key, label) ->
                                                    val isSelected = settings.returnPeriod == key
                                                    Button(
                                                        onClick = {
                                                            viewModel.updateInvestmentSettings(
                                                                initial = settings.initialInvestment,
                                                                monthly = settings.monthlyContribution,
                                                                period = key
                                                            )
                                                            focusManager.clearFocus()
                                                        },
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = if (isSelected) {
                                                                MaterialTheme.colorScheme.primary
                                                            } else {
                                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                                            },
                                                            contentColor = if (isSelected) {
                                                                MaterialTheme.colorScheme.onPrimary
                                                            } else {
                                                                MaterialTheme.colorScheme.primary
                                                            }
                                                        ),
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .height(38.dp)
                                                            .testTag("period_btn_$key"),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Dynamic Simulation Chart drawing
                            item {
                                SimulationChart(
                                    timeline = timeline,
                                    targetCapitalNeeded = targetCapitalNeeded
                                )
                            }

                            // 12-Month Wealth Evolution Chart
                            item {
                                MonthlyWealthEvolutionChart(
                                    purchasedAssets = purchasedAssets,
                                    settings = settings
                                )
                            }

                            // Standard of living calculator module based on desired standards
                            item {
                                StandardOfLivingCalculator(
                                    purchasedAssets = purchasedAssets,
                                    settings = settings,
                                    totalFixedCosts = totalCosts
                                )
                            }
                        }
                    }

                    2 -> {
                        // --- TAB 3: SUGGESTED CARTEIRA ---
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            // Search and Explore Assets Box module
                            item {
                                AssetSearchModule(
                                    onAddAssetClick = { asset, units ->
                                        prefilledTicker = asset.ticker
                                        prefilledName = asset.name
                                        prefilledCategory = asset.category
                                        prefilledQuantity = units.toString()
                                        prefilledPurchasePrice = asset.averagePrice.toString()
                                        prefilledAnnualYield = (asset.annualYield * 100.0).toString()
                                        showAddAssetDialog = true
                                    }
                                )
                            }

                            item {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.06f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = "Sugestão",
                                                tint = MaterialTheme.colorScheme.secondary
                                            )
                                            Text(
                                                text = "Recomendação Portfólio ${settings.returnPeriod.lowercase().replaceFirstChar { it.titlecase() }}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        
                                        val periodFactor = when (settings.returnPeriod.uppercase()) {
                                            "TRIMESTRAL" -> 3.0
                                            "SEMESTRAL" -> 6.0
                                            "ANUAL" -> 12.0
                                            else -> 1.0
                                        }
                                        val periodCosts = totalCosts * periodFactor
                                        val periodName = when(settings.returnPeriod.uppercase()) {
                                            "TRIMESTRAL" -> "trimestral (3 meses)"
                                            "SEMESTRAL" -> "semestral (6 meses)"
                                            "ANUAL" -> "anual (12 meses)"
                                            else -> "mensal (1 mês)"
                                        }
                                        Text(
                                            text = String.format(
                                                Locale("pt", "BR"),
                                                "Sua meta total de despesas para o período %s é de R$ %,.2f (pois as contas são mensais, R$ %,.2f/mês, e se acumulam). Selecionamos ativos de alta qualidade com pagamentos de proventos correspondentes a %s para suprir esta meta de maneira automatizada.",
                                                periodName,
                                                periodCosts,
                                                totalCosts,
                                                settings.returnPeriod.lowercase()
                                            ),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }

                            if (recommendations.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 40.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = "Descubra a mágica",
                                                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                                                modifier = Modifier.size(64.dp)
                                            )
                                            Text(
                                                text = "Descubra o poder dos juros compostos!",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                            )
                                            Text(
                                                text = "Adicione contas fixas na aba '1. Contas Fixas' para ver a mágica acontecer.",
                                                textAlign = TextAlign.Center,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                modifier = Modifier.padding(horizontal = 32.dp)
                                            )
                                        }
                                    }
                                }
                            } else {
                                items(recommendations, key = { it.asset.ticker }) { rec ->
                                    AssetCard(
                                        recommendation = rec,
                                        period = settings.returnPeriod,
                                        onAddClick = { asset, units ->
                                            prefilledTicker = asset.ticker
                                            prefilledName = asset.name
                                            prefilledCategory = asset.category
                                            prefilledQuantity = units.toString()
                                            prefilledPurchasePrice = asset.averagePrice.toString()
                                            prefilledAnnualYield = (asset.annualYield * 100.0).toString()
                                            showAddAssetDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    }

                    3 -> {
                        // --- TAB 4: MY PORTFOLIO & RETURNS ---
                        val totalInvested = purchasedAssets.sumOf { it.quantity * it.purchasePrice }
                        val totalMonthlyReturn = purchasedAssets.sumOf { (it.quantity * it.purchasePrice * it.annualYield) / 12.0 }
                        val pctCoverage = if (totalCosts > 0) (totalMonthlyReturn / totalCosts) * 100.0 else 100.0

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 80.dp) // space for FAB
                        ) {
                            // Portfolio summary card
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "Resumo Real da Carteira",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(
                                                    text = "Total Investido Real",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                )
                                                Text(
                                                    text = String.format(Locale("pt", "BR"), "R$ %,.2f", totalInvested),
                                                    style = MaterialTheme.typography.titleLarge,
                                                    fontWeight = FontWeight.Black
                               				)
                                            }

                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = "Proventos Estimados/Mês",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                )
                                                Text(
                                                    text = String.format(Locale("pt", "BR"), "R$ %,.2f", totalMonthlyReturn),
                                                    style = MaterialTheme.typography.titleLarge,
                                                    fontWeight = FontWeight.Black,
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                            }
                                        }

                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 4.dp),
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                        )

                                        // Cost coverage summary
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Cobertura do Custo de Vida:",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                            Text(
                                                text = String.format(Locale("pt", "BR"), "%.1f%%", pctCoverage),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (pctCoverage >= 100.0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        
                                        // Dynamic Pie Chart for Portfolio Balance
                                        if (purchasedAssets.isNotEmpty()) {
                                            PortfolioPieChart(purchasedAssets = purchasedAssets)
                                        }

                                        LinearProgressIndicator(
                                            progress = (pctCoverage / 100.0).toFloat().coerceIn(0f, 1f),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            color = if (pctCoverage >= 100.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                        )

                                        Text(
                                            text = if (pctCoverage >= 100.0) {
                                                "🎉 Parabéns! Seus proventos reais atuais cobrem integralmente suas despesas ativas. Você alcançou a Liberdade Financeira real!"
                                            } else {
                                                val remainingStr = String.format(Locale("pt", "BR"), "R$ %,.2f", if (totalCosts > totalMonthlyReturn) totalCosts - totalMonthlyReturn else 0.0)
                                                "Falta $remainingStr em dividendos mensais para cobrir totalmente suas despesas mapeadas (R$ ${String.format(Locale("pt", "BR"), "%,.2f", totalCosts)}/mês)."
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }

                            // 12-Month passive income bar chart projection (Recharts style)
                            item {
                                MonthlyIncomeBarChart(
                                    purchasedAssets = purchasedAssets,
                                    settings = settings,
                                    totalFixedCosts = totalCosts
                                )
                            }

                            // Heading for Asset list
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Seus Investimentos Adquiridos",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${purchasedAssets.size} ativos",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (purchasedAssets.isEmpty()) {
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "Não há ativos na carteira 🤔",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Text(
                                                text = "Toque no botão 'Adquirir Ativo' ou no '+' abaixo para adicionar suas ações, FIIs ou títulos comprados. Assim, você poderá comparar o dividend yield real pago com o valor de suas contas mensais!",
                                                textAlign = TextAlign.Center,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Button(
                                                onClick = {
                                                    prefilledTicker = ""
                                                    prefilledName = ""
                                                    prefilledCategory = "Ação"
                                                    prefilledQuantity = ""
                                                    prefilledPurchasePrice = ""
                                                    prefilledAnnualYield = ""
                                                    showAddAssetDialog = true
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary
                                                )
                                            ) {
                                                Text("Adicionar Meu Primeiro Ativo")
                                            }
                                        }
                                    }
                                }
                            } else {
                                items(purchasedAssets, key = { it.id }) { asset ->
                                    PurchasedAssetItem(
                                        asset = asset,
                                        onDelete = { viewModel.deletePurchasedAsset(asset) }
                                    )
                                }
                            }
                    }
                }
            }
        }
    }
    }

    if (showAddDialog) {
        AddBillDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, value -> viewModel.addBill(name, value) }
        )
    }

    if (showAddAssetDialog) {
        AddPurchasedAssetDialog(
            initialTicker = prefilledTicker,
            initialName = prefilledName,
            initialCategory = if (prefilledCategory.isNotBlank()) prefilledCategory else "Ação",
            initialQuantity = prefilledQuantity,
            initialPurchasePrice = prefilledPurchasePrice,
            initialAnnualYield = prefilledAnnualYield,
            onDismiss = { 
                showAddAssetDialog = false
                prefilledTicker = ""
                prefilledName = ""
                prefilledCategory = ""
                prefilledQuantity = ""
                prefilledPurchasePrice = ""
                prefilledAnnualYield = ""
            },
            onConfirm = { ticker, name, category, quantity, purchasePrice, annualYield ->
                viewModel.addPurchasedAsset(ticker, name, category, quantity, purchasePrice, annualYield)
            }
        )
    }
}

@Composable
fun PortfolioPieChart(purchasedAssets: List<PurchasedAsset>) {
    val grouped = purchasedAssets.groupBy { it.category }
    val categoryTotals = grouped.mapValues { entry -> entry.value.sumOf { it.quantity * it.purchasePrice } }
    val totalValue = categoryTotals.values.sum()
    
    val categoryColors = mapOf(
        "Ação" to Color(0xFF4CAF50),       // Green
        "FII" to Color(0xFF2196F3),        // Blue
        "Tesouro Direto" to Color(0xFF9C27B0) // Purple
    )
    
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Distribuição do Patrimônio",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = -90f
                val strokeWidth = 32.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                
                categoryTotals.forEach { (category, value) ->
                    val sweepAngle = (value / totalValue).toFloat() * 360f
                    val color = categoryColors[category] ?: Color.Gray
                    
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                    )
                    startAngle += sweepAngle
                }
            }
            
            Text(
                text = "100%",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            categoryTotals.forEach { (category, value) ->
                val percentage = (value / totalValue) * 100
                val color = categoryColors[category] ?: Color.Gray
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(6.dp)).background(color))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$category (${String.format(Locale("pt", "BR"), "%.0f", percentage)}%)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun WizardTutorial(step: Int, onNext: () -> Unit, onSkip: () -> Unit) {
    val messages = listOf(
        "👋 Bem-vindo ao Viver de Renda!\n\nComece adicionando o seu Custo de Vida na aba Contas Fixas.",
        "⚙️ Na aba Simulador, defina o que você já tem guardado e o quanto consegue investir por mês.",
        "💡 A aba Sugestões mostrará automaticamente uma lista de investimentos para você atingir a meta.",
        "💰 Finalmente, cadastre os ativos que comprar na aba Carteira e veja o seu Patrimônio Real crescer!"
    )

    Dialog(
        onDismissRequest = { onSkip() },
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Passo ${step + 1} de 4",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = messages[step],
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onSkip) {
                        Text("Pular", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(onClick = onNext) {
                        Text(if (step == 3) "Começar" else "Avançar")
                    }
                }
            }
        }
    }
}
}
