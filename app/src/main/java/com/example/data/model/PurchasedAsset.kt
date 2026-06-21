package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "purchased_assets")
data class PurchasedAsset(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ticker: String,
    val name: String,
    val category: String, // "Ação", "FII", "Tesouro Direto"
    val quantity: Int,
    val purchasePrice: Double, // Price paid per share/unit
    val annualYield: Double // Expected annual yield, e.g. 0.088 (8.8%)
)
