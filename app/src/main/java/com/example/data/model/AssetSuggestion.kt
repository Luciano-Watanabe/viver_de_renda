package com.example.data.model

data class Asset(
    val ticker: String,
    val name: String,
    val category: String, // "Ação", "FII", "Tesouro Direto"
    val annualYield: Double, // e.g. 0.10 for 10%
    val averagePrice: Double, // R$ average price
    val frequency: String, // "MENSAL", "TRIMESTRAL", "SEMESTRAL", "ANUAL"
    val description: String
)

object AssetSuggestionEngine {

    val availableAssets = listOf(
        // FIIs (Mensal)
        Asset(
            ticker = "MXRF11",
            name = "Maxi Renda FII",
            category = "FII",
            annualYield = 0.108,
            averagePrice = 10.20,
            frequency = "MENSAL",
            description = "Fundo imobiliário do tipo papel (CRI), o mais popular da B3, focado em renda mensal estável."
        ),
        Asset(
            ticker = "CYCR11",
            name = "Cyrela Credit FII",
            category = "FII",
            annualYield = 0.134,
            averagePrice = 8.67,
            frequency = "MENSAL",
            description = "Fundo imobiliário de papel sob gestão da Cyrela focado na aquisição de CRIs corporativos de excelente qualidade."
        ),
        Asset(
            ticker = "HGLG11",
            name = "Patria Logística FII",
            category = "FII",
            annualYield = 0.088,
            averagePrice = 162.50,
            frequency = "MENSAL",
            description = "Fundo de tijolo focado em galpões logísticos de alto padrão, resiliente com inquilinos premium."
        ),
        Asset(
            ticker = "XPML11",
            name = "XP Malls FII",
            category = "FII",
            annualYield = 0.094,
            averagePrice = 112.00,
            frequency = "MENSAL",
            description = "Fundo imobiliário de tijolo com participação em shopping centers de grande circulação nacional."
        ),
        Asset(
            ticker = "KNIP11",
            name = "Kinea Índices de Preços FII",
            category = "FII",
            annualYield = 0.112,
            averagePrice = 96.80,
            frequency = "MENSAL",
            description = "Fundo estruturado pela Kinea, indexado ao IPCA, protegendo a renda e o principal contra a inflação."
        ),

        // Trimestral
        Asset(
            ticker = "ITSA4",
            name = "Itaúsa S.A.",
            category = "Ação",
            annualYield = 0.068,
            averagePrice = 10.50,
            frequency = "TRIMESTRAL",
            description = "Holding de investimento que detém o controle do Banco Itaú. Excelente histórico de remuneração trimestral."
        ),
        Asset(
            ticker = "WEGE3",
            name = "WEG S.A.",
            category = "Ação",
            annualYield = 0.042,
            averagePrice = 48.00,
            frequency = "TRIMESTRAL",
            description = "Multinacional brasileira de bens de capital. Foco em crescimento de dividendos regulares e reinvestimento."
        ),
        Asset(
            ticker = "EGIE3",
            name = "Engie Brasil Energia",
            category = "Ação",
            annualYield = 0.082,
            averagePrice = 43.50,
            frequency = "TRIMESTRAL",
            description = "Uma das maiores geradoras privadas de energia limpa no país, gerando dividendos trimestrais polpudos."
        ),

        // Semestral
        Asset(
            ticker = "TAEE11",
            name = "Taesa Unit",
            category = "Ação",
            annualYield = 0.102,
            averagePrice = 34.80,
            frequency = "SEMESTRAL",
            description = "Concessionária de transmissão de energia elétrica. Paga dividendos consistentes baseados na inflação e contratos longos."
        ),
        Asset(
            ticker = "BBAS3",
            name = "Banco do Brasil S.A.",
            category = "Ação",
            annualYield = 0.098,
            averagePrice = 27.20,
            frequency = "SEMESTRAL",
            description = "Banco estatal de alta governança e excelente margem de lucro, gerando dividendos semi-anuais generosos."
        ),
        Asset(
            ticker = "VALE3",
            name = "Vale S.A.",
            category = "Ação",
            annualYield = 0.085,
            averagePrice = 61.50,
            frequency = "SEMESTRAL",
            description = "Gigante global de mineração de ferro. Excelente pagadora de proventos a cada semestre vinculada ao preço das commodities."
        ),

        // Anual
        Asset(
            ticker = "PETR4",
            name = "Petrobras Pref.",
            category = "Ação",
            annualYield = 0.138,
            averagePrice = 38.50,
            frequency = "ANUAL",
            description = "Maior petroleira do Brasil. Embora distribua ao longo do ano, seu plano estratégico acumula grandes dividendos anuais."
        ),
        Asset(
            ticker = "CPFE3",
            name = "CPFL Energia S.A.",
            category = "Ação",
            annualYield = 0.096,
            averagePrice = 33.10,
            frequency = "ANUAL",
            description = "Holding do setor elétrico atuante em geração, transmissão e distribuição. Foco em estabilidade e dividendos anuais acumulados."
        ),
        Asset(
            ticker = "TRPL4",
            name = "ISA CTEEP Pref.",
            category = "Ação",
            annualYield = 0.089,
            averagePrice = 25.40,
            frequency = "ANUAL",
            description = "Líder em transmissão de energia. Negócio previsível que rende proventos robustos anualmente após o fechamento do balanço."
        ),

        // Tesouro Direto
        Asset(
            ticker = "TESOURO SELIC",
            name = "Tesouro Selic (LFT)",
            category = "Tesouro Direto",
            annualYield = 0.105,
            averagePrice = 14500.00, // Preço de uma fração inteira/título
            frequency = "MENSAL",
            description = "Título público mais seguro, ideal para reserva de liquidez ou fluxo mensal equivalente via saques controlados."
        ),
        Asset(
            ticker = "TESOURO IPCA+ COM JUROS SEMESTRAIS",
            name = "Tesouro IPCA+ Semestral",
            category = "Tesouro Direto",
            annualYield = 0.062, // Taxa real (exclui inflação para consistência)
            averagePrice = 4200.00,
            frequency = "SEMESTRAL",
            description = "Título de renda fixa público que paga cupons a cada 6 meses, corrigidos pelo IPCA (inflação)."
        ),
        Asset(
            ticker = "TESOURO RENDA+",
            name = "Tesouro Renda+ Aposentadoria Extra",
            category = "Tesouro Direto",
            annualYield = 0.064,
            averagePrice = 1200.00,
            frequency = "MENSAL",
            description = "Título inovador projetado especificamente para acumular capital e receber 240 salários mensais na fase de usufruto."
        )
    )

    // Helper data structure to return recommendations
    data class AssetRecommendation(
        val asset: Asset,
        val targetWeight: Double, // Suggested allocation e.g. 0.40 for 40%
        val capitalToInvest: Double, // Amount of capital to invest in this asset
        val estimatedYieldValue: Double, // Value generated per frequency period
        val unitsToBuy: Int // Number of shares / bonds
    )

    fun getRecommendations(
        monthlyRequirement: Double, // e.g. 3000
        period: String // "MENSAL", "TRIMESTRAL", "SEMESTRAL", "ANUAL"
    ): List<AssetRecommendation> {
        // We filter assets based on frequency first, but always allow Tesouro Direto (mixed options) and FIIs (great for mensual yield)
        // Let's make sure FIIs are suggested when "MENSAL" is selected, Stocks for respective, and Tesouro Direto as ballast.
        val targetPeriod = period.uppercase()
        val filteredAssets = availableAssets.filter {
            it.frequency == targetPeriod || 
            (targetPeriod == "MENSAL" && it.category == "FII") || 
            (targetPeriod == "SEMESTRAL" && it.ticker == "TESOURO IPCA+ COM JUROS SEMESTRAIS") ||
            (it.ticker == "TESOURO RENDA+" && targetPeriod == "MENSAL")
        }

        if (filteredAssets.isEmpty()) {
            return emptyList()
        }

        // We distribute weights equally among selected suggestions, up to 4 assets to prevent cluttering
        val selectedAssets = filteredAssets.take(4)
        val weight = 1.0 / selectedAssets.size

        // Calculate money needed depending on period
        // For monthly bills of $3000:
        // Mensal needs $3000.
        // Trimestral wants $9000 per trimester.
        // Semestral wants $18000 per semester.
        // Anual wants $36000 per year.
        val factor = when (targetPeriod) {
            "TRIMESTRAL" -> 3.0
            "SEMESTRAL" -> 6.0
            "ANUAL" -> 12.0
            else -> 1.0
        }
        val targetIncomePerPeriod = monthlyRequirement * factor

        return selectedAssets.map { asset ->
            // Yield per period:
            // Tesouro real yield is ~6%+inflation. High level yield average:
            val yieldPerPeriod = when (asset.frequency) {
                "MENSAL" -> asset.annualYield / 12.0
                "TRIMESTRAL" -> asset.annualYield / 4.0
                "SEMESTRAL" -> asset.annualYield / 2.0
                else -> asset.annualYield
            }

            // Capital needed to generate 'targetIncomePerPeriod * weight' for this asset
            // Formula: Capital * yieldPerPeriod = targetIncomePerPeriod * weight
            // Capital = (targetIncomePerPeriod * weight) / yieldPerPeriod
            val neededIncomeForThisAsset = targetIncomePerPeriod * weight
            val capitalForThisAsset = if (yieldPerPeriod > 0) {
                neededIncomeForThisAsset / yieldPerPeriod
            } else {
                neededIncomeForThisAsset / 0.05
            }

            // Units to buy
            val units = (capitalForThisAsset / asset.averagePrice).toInt().coerceAtLeast(1)
            // Recalculate real capital based on whole units (or keep mathematical)
            val finalCapital = units * asset.averagePrice
            val realEstimatedYield = finalCapital * yieldPerPeriod

            AssetRecommendation(
                asset = asset,
                targetWeight = weight,
                capitalToInvest = finalCapital,
                estimatedYieldValue = realEstimatedYield,
                unitsToBuy = units
            )
        }
    }

    // Accumulation simulation
    // Computes progression of investment
    data class SimulationYear(
        val year: Int,
        val totalAccumulated: Double,
        val monthlyPassiveIncome: Double,
        val targetMet: Boolean
    )

    fun runSimulation(
        initial: Double,
        monthly: Double,
        targetMonthly: Double,
        annualInterestReal: Double = 0.075, // 7.5% real gain (after inflation) is realistic
        years: Int = 30
    ): List<SimulationYear> {
        val monthlyRate = Math.pow(1.0 + annualInterestReal, 1.0 / 12.0) - 1.0
        var currentSum = initial
        val timeline = mutableListOf<SimulationYear>()

        for (year in 1..years) {
            for (month in 1..12) {
                currentSum = currentSum * (1.0 + monthlyRate) + monthly
            }
            // Passive income possible at 8% annual yield factor:
            val estMonthlyPassive = (currentSum * 0.08) / 12.0
            timeline.add(
                SimulationYear(
                    year = year,
                    totalAccumulated = currentSum,
                    monthlyPassiveIncome = estMonthlyPassive,
                    targetMet = estMonthlyPassive >= targetMonthly
                )
            )
        }
        return timeline
    }

    // Calculates exactly how many months to hit target
    fun monthsToTarget(
        initial: Double,
        monthly: Double,
        targetMonthly: Double,
        annualInterestReal: Double = 0.075
    ): Int {
        val averageTargetCapital = (targetMonthly * 12) / 0.085 // Conservatively assume 8.5% annual yield
        if (initial >= averageTargetCapital) return 0
        if (monthly <= 0) return 999 // Representing impossibility without contributions

        val monthlyRate = Math.pow(1.0 + annualInterestReal, 1.0 / 12.0) - 1.0
        var currentSum = initial
        var months = 0
        val maxMonths = 12 * 70 // cap at 70 years to avoid infinite loops

        while (currentSum < averageTargetCapital && months < maxMonths) {
            currentSum = currentSum * (1.10 + monthlyRate) // Wait, let's keep standard formula: currentSum * (1.0 + monthlyRate) + monthly
            // Let's do standard compounding:
            currentSum = currentSum * (1.0 + monthlyRate) + monthly
            months++
        }
        return months
    }
}
