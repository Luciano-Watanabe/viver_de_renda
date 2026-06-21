package com.example.data.local

import androidx.room.*
import com.example.data.model.Bill
import com.example.data.model.InvestmentSettings
import com.example.data.model.PurchasedAsset
import kotlinx.coroutines.flow.Flow

@Dao
interface ViverDao {
    // Purchased Assets Queries
    @Query("SELECT * FROM purchased_assets ORDER BY ticker ASC")
    fun getAllPurchasedAssets(): Flow<List<PurchasedAsset>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchasedAsset(asset: PurchasedAsset)

    @Update
    suspend fun updatePurchasedAsset(asset: PurchasedAsset)

    @Delete
    suspend fun deletePurchasedAsset(asset: PurchasedAsset)

    // Bills Queries
    @Query("SELECT * FROM bills ORDER BY value DESC")
    fun getAllBills(): Flow<List<Bill>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: Bill)

    @Update
    suspend fun updateBill(bill: Bill)

    @Delete
    suspend fun deleteBill(bill: Bill)

    @Query("DELETE FROM bills")
    suspend fun clearAllBills()

    // Investment Settings Queries
    @Query("SELECT * FROM investment_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<InvestmentSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: InvestmentSettings)
}
