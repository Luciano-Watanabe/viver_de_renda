package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.AssetSuggestionEngine
import com.example.data.model.Bill
import com.example.data.model.InvestmentSettings
import com.example.data.model.PurchasedAsset
import com.example.data.repository.ViverRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ViverViewModel(private val repository: ViverRepository) : ViewModel() {

    // Purchased Assets State
    val allPurchasedAssets: StateFlow<List<PurchasedAsset>> = repository.allPurchasedAssets
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Bills List State
    val allBills: StateFlow<List<Bill>> = repository.allBills
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Investment Settings State
    val settings: StateFlow<InvestmentSettings> = repository.settings
        .map { it ?: InvestmentSettings() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = InvestmentSettings()
        )

    // Derived State: Total Fixed Costs of ACTIVE bills
    val totalFixedCosts: StateFlow<Double> = allBills
        .map { bills -> bills.filter { it.isActive }.sumOf { it.value } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    // Live Prices from Yahoo Finance (Map of Ticker -> Current Price)
    private val _livePrices = MutableStateFlow<Map<String, Double>>(emptyMap())
    val livePrices: StateFlow<Map<String, Double>> = _livePrices.asStateFlow()

    // Trigger for when live prices update
    private val _livePricesUpdated = MutableStateFlow(0)
    val livePricesUpdated: StateFlow<Int> = _livePricesUpdated

    init {
        viewModelScope.launch {
            allPurchasedAssets.collect { assets ->
                fetchLivePrices(assets)
            }
        }
    }

    private fun fetchLivePrices(assets: List<PurchasedAsset>) {
        if (assets.isEmpty()) return
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val newPrices = mutableMapOf<String, Double>()
            for (asset in assets) {
                var symbol = asset.ticker.uppercase()
                if (!symbol.endsWith(".SA") && !symbol.contains(".")) {
                    symbol += ".SA"
                }
                try {
                    val url = java.net.URL("https://query1.finance.yahoo.com/v8/finance/chart/$symbol?interval=1d")
                    val connection = url.openConnection() as java.net.HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000
                    
                    if (connection.responseCode == 200) {
                        val response = connection.inputStream.bufferedReader().use { it.readText() }
                        val json = org.json.JSONObject(response)
                        val chart = json.getJSONObject("chart")
                        val result = chart.getJSONArray("result").getJSONObject(0)
                        val meta = result.getJSONObject("meta")
                        val regularMarketPrice = meta.getDouble("regularMarketPrice")
                        newPrices[asset.ticker] = regularMarketPrice
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            // Retain old prices if fetch failed, update with new ones
            _livePrices.value = _livePrices.value + newPrices
        }
    }

    // Derived State: Asset Recommendations
    val recommendations: StateFlow<List<AssetSuggestionEngine.AssetRecommendation>> = combine(
        totalFixedCosts,
        settings,
        livePricesUpdated
    ) { costs, settingsObj, _ ->
        AssetSuggestionEngine.getRecommendations(costs, settingsObj.returnPeriod)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Derived State: Simulation Timeline
    val simulationTimeline: StateFlow<List<AssetSuggestionEngine.SimulationYear>> = combine(
        totalFixedCosts,
        settings
    ) { costs, settingsObj ->
        AssetSuggestionEngine.runSimulation(
            initial = settingsObj.initialInvestment,
            monthly = settingsObj.monthlyContribution,
            targetMonthly = costs,
            annualInterestReal = 0.075, // 7.5% real return (adjusted for inflation)
            years = 25
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Derived State: Months to Freedom Target
    val monthsToFinancialFreedom: StateFlow<Int> = combine(
        totalFixedCosts,
        settings
    ) { costs, settingsObj ->
        AssetSuggestionEngine.monthsToTarget(
            initial = settingsObj.initialInvestment,
            monthly = settingsObj.monthlyContribution,
            targetMonthly = costs,
            annualInterestReal = 0.075
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    init {
        // Pre-populate database with suggested bills on startup if empty
        viewModelScope.launch {
            repository.populateDefaultBills()
        }
        
        // Fetch live prices for assets from Yahoo Finance
        viewModelScope.launch {
            AssetSuggestionEngine.updateLivePrices()
            _livePricesUpdated.value += 1
        }
    }

    // Actions
    fun addPurchasedAsset(ticker: String, name: String, category: String, quantity: Int, purchasePrice: Double, annualYield: Double) {
        viewModelScope.launch {
            repository.insertPurchasedAsset(
                PurchasedAsset(
                    ticker = ticker.uppercase().trim(),
                    name = name.trim(),
                    category = category,
                    quantity = quantity,
                    purchasePrice = purchasePrice,
                    annualYield = annualYield
                )
            )
        }
    }

    fun updatePurchasedAsset(asset: PurchasedAsset) {
        viewModelScope.launch {
            repository.updatePurchasedAsset(asset)
        }
    }

    fun deletePurchasedAsset(asset: PurchasedAsset) {
        viewModelScope.launch {
            repository.deletePurchasedAsset(asset)
        }
    }

    fun addBill(name: String, value: Double) {
        viewModelScope.launch {
            repository.insertBill(Bill(name = name, value = value, isActive = true))
        }
    }

    fun toggleBillActive(bill: Bill) {
        viewModelScope.launch {
            repository.updateBill(bill.copy(isActive = !bill.isActive))
        }
    }

    fun deleteBill(bill: Bill) {
        viewModelScope.launch {
            repository.deleteBill(bill)
        }
    }

    fun updateBillValue(bill: Bill, newValue: Double) {
        viewModelScope.launch {
            repository.updateBill(bill.copy(value = newValue))
        }
    }

    fun updateInvestmentSettings(
        initial: Double,
        monthly: Double,
        period: String
    ) {
        viewModelScope.launch {
            val updated = settings.value.copy(
                initialInvestment = initial,
                monthlyContribution = monthly,
                returnPeriod = period
            )
            repository.saveSettings(updated)
        }
    }

    fun updateTutorialStatus(seen: Boolean) {
        viewModelScope.launch {
            val updated = settings.value.copy(hasSeenTutorial = seen)
            repository.saveSettings(updated)
        }
    }

    fun updateCelebratedProgress(progress: Int) {
        viewModelScope.launch {
            val updated = settings.value.copy(lastCelebratedProgress = progress)
            repository.saveSettings(updated)
        }
    }

    fun updateUserName(name: String) {
        viewModelScope.launch {
            val updated = settings.value.copy(userName = name.trim())
            repository.saveSettings(updated)
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            repository.clearAndResetToDefaults()
        }
    }
}

class ViverViewModelFactory(private val repository: ViverRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ViverViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ViverViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
