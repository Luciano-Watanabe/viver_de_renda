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

    // Derived State: Asset Recommendations
    val recommendations: StateFlow<List<AssetSuggestionEngine.AssetRecommendation>> = combine(
        totalFixedCosts,
        settings
    ) { costs, settingsObj ->
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
