package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "investment_settings")
data class InvestmentSettings(
    @PrimaryKey val id: Int = 1,
    val initialInvestment: Double = 10000.0,
    val monthlyContribution: Double = 500.0,
    val returnPeriod: String = "MENSAL" // "MENSAL", "TRIMESTRAL", "SEMESTRAL", "ANUAL"
)
