package com.example.data.repository

import com.example.data.local.ViverDao
import com.example.data.model.Bill
import com.example.data.model.InvestmentSettings
import com.example.data.model.PurchasedAsset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ViverRepository(private val dao: ViverDao) {

    val allBills: Flow<List<Bill>> = dao.getAllBills()
    val settings: Flow<InvestmentSettings?> = dao.getSettings()
    val allPurchasedAssets: Flow<List<PurchasedAsset>> = dao.getAllPurchasedAssets()

    suspend fun insertPurchasedAsset(asset: PurchasedAsset) {
        dao.insertPurchasedAsset(asset)
    }

    suspend fun updatePurchasedAsset(asset: PurchasedAsset) {
        dao.updatePurchasedAsset(asset)
    }

    suspend fun deletePurchasedAsset(asset: PurchasedAsset) {
        dao.deletePurchasedAsset(asset)
    }

    suspend fun insertBill(bill: Bill) {
        dao.insertBill(bill)
    }

    suspend fun updateBill(bill: Bill) {
        dao.updateBill(bill)
    }

    suspend fun deleteBill(bill: Bill) {
        dao.deleteBill(bill)
    }

    suspend fun saveSettings(settings: InvestmentSettings) {
        dao.saveSettings(settings)
    }

    suspend fun populateDefaultBills() {
        val currentBills = dao.getAllBills().first()
        if (currentBills.isEmpty()) {
            val defaults = listOf(
                Bill(name = "Aluguel ou Financiamento", value = 1600.0),
                Bill(name = "Condomínio e IPTU", value = 350.0),
                Bill(name = "Supermercado & Alimentação", value = 900.0),
                Bill(name = "Energia Elétrica (Luz)", value = 180.0),
                Bill(name = "Água e Gás", value = 80.0),
                Bill(name = "SVA, Internet & Streamings", value = 140.0),
                Bill(name = "Plano de Saúde & Medicamentos", value = 400.0),
                Bill(name = "Transporte, Petroleo & Uber", value = 300.0),
                Bill(name = "Lazer e Assinaturas", value = 250.0)
            )
            for (bill in defaults) {
                dao.insertBill(bill)
            }
        }
    }

    suspend fun clearAndResetToDefaults() {
        dao.clearAllBills()
        val defaults = listOf(
            Bill(name = "Aluguel ou Financiamento", value = 1600.0),
            Bill(name = "Condomínio e IPTU", value = 350.0),
            Bill(name = "Supermercado & Alimentação", value = 900.0),
            Bill(name = "Energia Elétrica (Luz)", value = 180.0),
            Bill(name = "Água e Gás", value = 80.0),
            Bill(name = "SVA, Internet & Streamings", value = 140.0),
            Bill(name = "Plano de Saúde & Medicamentos", value = 400.0),
            Bill(name = "Transporte, Petroleo & Uber", value = 300.0),
            Bill(name = "Lazer e Assinaturas", value = 250.0)
        )
        for (bill in defaults) {
            dao.insertBill(bill)
        }
    }
}
