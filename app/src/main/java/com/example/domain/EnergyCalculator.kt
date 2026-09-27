package com.example.domain

import com.example.data.model.AcChargingResult
import com.example.data.model.Appliance
import com.example.data.model.BatteryBankResult
import com.example.data.model.CalculationSettings
import com.example.data.model.InverterResult
import com.example.data.model.PowerStationResult
import com.example.data.model.SolarChargingResult
import com.example.data.model.SystemVoltagePreference
import kotlin.math.ceil
import kotlin.math.max

object EnergyCalculator {

    private val STANDARD_INVERTER_WATTS = listOf(
        500, 800, 1000, 1200, 1500, 2000, 2500, 3000, 3500, 4000, 5000, 6000, 8000, 10000
    )

    private val STANDARD_FUSE_AMPS = listOf(
        30, 40, 50, 60, 80, 100, 125, 150, 175, 200, 250, 300, 400, 500
    )

    private val STANDARD_MPPT_AMPS = listOf(
        15, 20, 30, 40, 50, 60, 80, 100
    )

    fun calculate(
        appliances: List<Appliance>,
        settings: CalculationSettings = CalculationSettings()
    ): PowerStationResult {
        val activeAppliances = appliances.filter { it.isEnabled }
        val totalRunningWatts = activeAppliances.sumOf { it.totalRunningWatts }

        // Surge calculation: Running baseline + largest surge delta among all motor/inductive equipment
        val maxSurgeDelta = activeAppliances.maxOfOrNull { it.totalPeakWatts - it.totalRunningWatts } ?: 0.0
        val totalPeakSurgeWatts = totalRunningWatts + maxSurgeDelta

        val dailyEnergyWh = activeAppliances.sumOf { it.dailyWattHours }
        val dailyEnergyKwh = dailyEnergyWh / 1000.0

        // 1. Determine System Voltage
        val systemVoltage = determineSystemVoltage(
            totalRunningWatts = totalRunningWatts,
            dailyEnergyWh = dailyEnergyWh,
            preference = settings.preferredSystemVoltage
        )

        // 2. Battery Bank Calculation
        val batteryBank = calculateBatteryBank(
            dailyEnergyWh = dailyEnergyWh,
            systemVoltage = systemVoltage,
            settings = settings
        )

        // 3. Inverter Sizing
        val inverter = calculateInverter(
            runningWatts = totalRunningWatts,
            surgeWatts = totalPeakSurgeWatts,
            systemVoltage = systemVoltage,
            settings = settings
        )

        // 4. AC Grid House Charger (Vía 1)
        val acCharging = calculateAcCharging(
            batteryBank = batteryBank,
            settings = settings
        )

        // 5. 200W Solar Panels (Vía 2)
        val solarCharging = calculateSolarCharging(
            dailyEnergyWh = dailyEnergyWh,
            batteryBank = batteryBank,
            settings = settings
        )

        return PowerStationResult(
            totalRunningWatts = totalRunningWatts,
            totalPeakSurgeWatts = totalPeakSurgeWatts,
            dailyEnergyWh = dailyEnergyWh,
            dailyEnergyKwh = dailyEnergyKwh,
            activeAppliancesCount = activeAppliances.size,
            batteryBank = batteryBank,
            inverter = inverter,
            acCharging = acCharging,
            solarCharging = solarCharging,
            settings = settings
        )
    }

    private fun determineSystemVoltage(
        totalRunningWatts: Double,
        dailyEnergyWh: Double,
        preference: SystemVoltagePreference
    ): Int {
        if (preference.voltage != null) {
            return preference.voltage
        }

        // Auto recommendation based on electrical efficiency & safe current:
        // Rule of thumb in off-grid engineering:
        // < 1200W continuous & < 3000Wh: 12V (amperage up to ~100A)
        // 1200W - 2500W continuous or 3000Wh - 7000Wh: 24V (amperage 50A - 105A)
        // > 2500W continuous or > 7000Wh: 48V (keeps amperage safe & minimizes cable loss)
        return when {
            totalRunningWatts > 2400 || dailyEnergyWh > 6500 -> 48
            totalRunningWatts > 1100 || dailyEnergyWh > 2600 -> 24
            else -> 12
        }
    }

    private fun calculateBatteryBank(
        dailyEnergyWh: Double,
        systemVoltage: Int,
        settings: CalculationSettings
    ): BatteryBankResult {
        val singleBattWh = settings.singleBatteryVoltage * settings.singleBatteryAh // 1280 Wh
        val usablePerBattWh = singleBattWh * settings.depthOfDischarge * settings.inverterEfficiency

        // Energy needed to cover autonomy days with safe discharge & inverter efficiency
        val energyTargetWh = max(100.0, dailyEnergyWh) * settings.autonomyDays
        val rawBatteriesNeeded = ceil(energyTargetWh / usablePerBattWh).toInt()

        // Series requirements:
        // 12V -> 1 battery in series (12.8V)
        // 24V -> 2 batteries in series (25.6V)
        // 48V -> 4 batteries in series (51.2V)
        val seriesCount = when (systemVoltage) {
            48 -> 4
            24 -> 2
            else -> 1
        }

        // Total batteries must be a multiple of seriesCount to build balanced strings
        val parallelStrings = max(1, ceil(rawBatteriesNeeded.toDouble() / seriesCount).toInt())
        val totalBatteryCount = parallelStrings * seriesCount

        val totalInstalledWh = totalBatteryCount * singleBattWh
        val totalUsableWh = totalInstalledWh * settings.depthOfDischarge * settings.inverterEfficiency
        val totalAhBank = parallelStrings * settings.singleBatteryAh

        // Max continuous discharge (1C rating typical for 100Ah LiFePO4 = 100A per parallel string)
        val maxContinuousDischargeAmps = parallelStrings * 100.0

        val connectionDescription = when {
            seriesCount == 1 && parallelStrings == 1 ->
                "1 batería individual (12.8V 100Ah)"
            seriesCount == 1 ->
                "$parallelStrings baterías conectadas en PARALELO (12.8V, ${totalAhBank.toInt()}Ah)"
            parallelStrings == 1 ->
                "$seriesCount baterías conectadas en SERIE (${systemVoltage}V nominal, 100Ah)"
            else ->
                "$totalBatteryCount baterías: $parallelStrings ramas en paralelo, cada rama con $seriesCount baterías en serie (${seriesCount}S${parallelStrings}P - ${systemVoltage}V nominal, ${totalAhBank.toInt()}Ah)"
        }

        return BatteryBankResult(
            batteryCount = totalBatteryCount,
            singleBatteryVoltage = settings.singleBatteryVoltage,
            singleBatteryAh = settings.singleBatteryAh,
            singleBatteryWh = singleBattWh,
            totalInstalledWh = totalInstalledWh,
            totalUsableWh = totalUsableWh,
            systemVoltage = systemVoltage,
            seriesCount = seriesCount,
            parallelCount = parallelStrings,
            totalAhBank = totalAhBank,
            connectionDescription = connectionDescription,
            maxContinuousDischargeAmps = maxContinuousDischargeAmps
        )
    }

    private fun calculateInverter(
        runningWatts: Double,
        surgeWatts: Double,
        systemVoltage: Int,
        settings: CalculationSettings
    ): InverterResult {
        // Continuous rating with 25% safety margin to prevent thermal throttling & prolong inverter life
        val targetContinuousWatts = max(300.0, runningWatts * 1.25)
        val targetSurgeWatts = max(targetContinuousWatts * 1.5, surgeWatts * 1.15)

        val recommendedRatedWatts = STANDARD_INVERTER_WATTS.firstOrNull { it >= targetContinuousWatts }
            ?: ceil(targetContinuousWatts / 1000.0).toInt() * 1000

        // Pure sine wave inverters typically provide 2x rated surge for 3-5 seconds
        val recommendedSurgeRating = max(recommendedRatedWatts * 2, ceil(targetSurgeWatts / 500.0).toInt() * 500)

        // Maximum DC current at full continuous power + inverter efficiency factor
        val maxDcCurrentAmps = (recommendedRatedWatts.toDouble() / (systemVoltage * settings.inverterEfficiency))

        val (cableGauge, fuseAmps) = determineCableAndFuse(maxDcCurrentAmps)

        val technicalRationale = buildString {
            append("Inversor de Onda Sinusoidal Pura de ${recommendedRatedWatts}W nominales y ${recommendedSurgeRating}W de pico a ${systemVoltage}V DC. ")
            append("La onda senoidal pura es indispensable para no dañar motores, compresores, fuentes de poder de laptops, routers y pantallas, evitando sobrecalentamiento y zumbidos. ")
            if (systemVoltage > 12) {
                append("La elección de ${systemVoltage}V DC reduce la corriente máxima a ${String.format("%.1f", maxDcCurrentAmps)}A, evitando cables excesivamente gruesos y minimizando caídas de tensión.")
            }
        }

        return InverterResult(
            continuousWatts = targetContinuousWatts,
            surgeWatts = targetSurgeWatts,
            recommendedRatedWatts = recommendedRatedWatts,
            recommendedSurgeRating = recommendedSurgeRating,
            systemDcVoltage = systemVoltage,
            maxDcCurrentAmps = maxDcCurrentAmps,
            recommendedCableGauge = cableGauge,
            recommendedFuseAmps = fuseAmps,
            technicalRationale = technicalRationale
        )
    }

    private fun determineCableAndFuse(maxDcAmps: Double): Pair<String, Int> {
        val cable = when {
            maxDcAmps <= 35 -> "8 AWG (10 mm²)"
            maxDcAmps <= 60 -> "6 AWG (16 mm²)"
            maxDcAmps <= 95 -> "4 AWG (25 mm²)"
            maxDcAmps <= 140 -> "2 AWG (35 mm²)"
            maxDcAmps <= 180 -> "1/0 AWG (50 mm²)"
            maxDcAmps <= 240 -> "2/0 AWG (70 mm²)"
            maxDcAmps <= 320 -> "4/0 AWG (120 mm²)"
            else -> "Doble cable 4/0 AWG en paralelo (2x 120 mm²)"
        }

        val minFuse = maxDcAmps * 1.25
        val fuse = STANDARD_FUSE_AMPS.firstOrNull { it >= minFuse }
            ?: (ceil(minFuse / 50.0).toInt() * 50)

        return Pair(cable, fuse)
    }

    private fun calculateAcCharging(
        batteryBank: BatteryBankResult,
        settings: CalculationSettings
    ): AcChargingResult {
        // LiFePO4 recommended charging current is 0.2C to 0.3C (0.2 * total Ah bank)
        // Safe, cool and fast enough (charges in ~3-4 hours)
        val nominalC20Amps = batteryBank.totalAhBank * 0.20
        val recommendedChargerAmps = when {
            nominalC20Amps <= 20.0 -> 20.0
            nominalC20Amps <= 30.0 -> 30.0
            nominalC20Amps <= 40.0 -> 40.0
            nominalC20Amps <= 50.0 -> 50.0
            nominalC20Amps <= 60.0 -> 60.0
            nominalC20Amps <= 80.0 -> 80.0
            else -> ceil(nominalC20Amps / 10.0) * 10.0
        }

        val bulkVoltagePerUnit = 14.4
        val floatVoltagePerUnit = 13.6
        val bulkAbsorptionVoltage = bulkVoltagePerUnit * batteryBank.seriesCount
        val floatVoltage = floatVoltagePerUnit * batteryBank.seriesCount

        // Charger wattage delivered into battery
        val chargingWattsDc = bulkAbsorptionVoltage * recommendedChargerAmps
        // Assuming ~88% AC to DC charger efficiency
        val chargerAcWatts = chargingWattsDc / 0.88

        // Recharge time:
        // From empty (0% to 100%): Total Ah / Amps * 1.05 (chemical absorption tail)
        val hoursFromZero = (batteryBank.totalAhBank / recommendedChargerAmps) * 1.05
        // From typical DoD (85% to recover):
        val hoursFromDoD = (batteryBank.totalAhBank * settings.depthOfDischarge / recommendedChargerAmps) * 1.05

        val estimatedKwhPerCharge = (batteryBank.totalInstalledWh * settings.depthOfDischarge / 1000.0) / 0.88

        val wallOutletReq = if (chargerAcWatts > 1500) {
            "Toma de 20A / 120V dedicada o 230V AC (${String.format("%.0f", chargerAcWatts)}W consumo)"
        } else {
            "Tomacorriente residencial estándar 110V-120V / 15A (${String.format("%.0f", chargerAcWatts)}W consumo)"
        }

        return AcChargingResult(
            recommendedChargerAmps = recommendedChargerAmps,
            recommendedChargerWatts = chargerAcWatts,
            bulkAbsorptionVoltage = bulkAbsorptionVoltage,
            floatVoltage = floatVoltage,
            hoursToFullChargeFromZero = hoursFromZero,
            hoursToFullChargeFromDoD = hoursFromDoD,
            estimatedKwhPerCharge = estimatedKwhPerCharge,
            wallOutletRequirement = wallOutletReq
        )
    }

    private fun calculateSolarCharging(
        dailyEnergyWh: Double,
        batteryBank: BatteryBankResult,
        settings: CalculationSettings
    ): SolarChargingResult {
        // Daily output of a single 200W panel accounting for HSP and net derating factor (dust, temp, MPPT efficiency)
        val dailyWhPer200WPanel = settings.singlePanelWatts * settings.peakSunHours * settings.solarLossFactor

        // Panels needed to balance daily load consumption
        val panelsForDaily = max(1, ceil(max(100.0, dailyEnergyWh) / dailyWhPer200WPanel).toInt())

        // Panels needed to recharge depleted battery bank (usable capacity) within 1 sunny day
        val energyToRechargeBankWh = batteryBank.totalInstalledWh * settings.depthOfDischarge
        val panelsForFullBank = max(1, ceil(energyToRechargeBankWh / dailyWhPer200WPanel).toInt())

        // Recommended panels: We prioritize replenishing the daily consumption plus a 15% safety buffer,
        // or fully replenishing the bank if autonomy is 1 day.
        val recommendedPanelsCount = max(panelsForDaily, minPanelsForBankVoltage(batteryBank.systemVoltage))
        val totalSolarWatts = recommendedPanelsCount * settings.singlePanelWatts
        val dailySolarGenWh = recommendedPanelsCount * dailyWhPer200WPanel

        // MPPT controller sizing:
        // Current into battery = Total solar watts / battery bank nominal voltage * 1.25 safety factor
        val mpptMinAmps = (totalSolarWatts / batteryBank.systemVoltage) * 1.25
        val recommendedMpptRating = STANDARD_MPPT_AMPS.firstOrNull { it >= mpptMinAmps }
            ?: (ceil(mpptMinAmps / 10.0).toInt() * 10)

        // Recommended panel wiring (e.g. series-parallel configurations)
        val recommendedWiring = determinePanelWiring(recommendedPanelsCount, batteryBank.systemVoltage)

        val coverageRatio = if (dailyEnergyWh > 0) {
            (dailySolarGenWh / dailyEnergyWh) * 100.0
        } else 100.0

        return SolarChargingResult(
            singlePanelWatts = settings.singlePanelWatts,
            panelsForDailyConsumption = panelsForDaily,
            panelsForFullRechargeOneDay = panelsForFullBank,
            recommendedPanelsCount = recommendedPanelsCount,
            totalSolarArrayWatts = totalSolarWatts,
            peakSunHours = settings.peakSunHours,
            dailySolarGenerationWh = dailySolarGenWh,
            mpptMinCurrentAmps = mpptMinAmps,
            recommendedMpptRatingAmps = recommendedMpptRating,
            recommendedArrayWiring = recommendedWiring,
            solarCoverageRatioPercent = coverageRatio
        )
    }

    private fun minPanelsForBankVoltage(systemVoltage: Int): Int {
        // A single 200W panel has Vmp around 18V - 20V, Voc around 24V.
        // For a 24V battery bank, we need at least 2 panels in series (or a high-voltage 200W panel)
        // For a 48V battery bank, we need at least 2 or 3 panels in series for MPPT step-down.
        return when (systemVoltage) {
            48 -> 4
            24 -> 2
            else -> 1
        }
    }

    private fun determinePanelWiring(panelCount: Int, systemVoltage: Int): String {
        return when (systemVoltage) {
            12 -> {
                when {
                    panelCount == 1 -> "1 panel de 200W conectado directo al controlador MPPT."
                    panelCount % 2 == 0 -> "${panelCount / 2} ramas en paralelo de 2 paneles en serie (2S${panelCount / 2}P). Eleva el voltaje a ~40V Vmp para máxima eficiencia MPPT."
                    else -> "$panelCount paneles en paralelo (o combinación recomendada por controlador MPPT)."
                }
            }
            24 -> {
                when {
                    panelCount == 2 -> "2 paneles en SERIE (2S1P). Voltaje de arreglo: ~40V Vmp, ~48V Voc. Ideal para banco de 24V."
                    panelCount % 2 == 0 -> "${panelCount / 2} ramas en paralelo de 2 paneles en serie (2S${panelCount / 2}P). Cada rama entrega ~40V Vmp."
                    panelCount % 3 == 0 -> "${panelCount / 3} ramas en paralelo de 3 paneles en serie (3S${panelCount / 3}P). Arreglo de ~60V Vmp."
                    else -> "$panelCount paneles: combinar en ramas de 2 o 3 en serie según el límite de voltaje Voc de tu MPPT."
                }
            }
            48 -> {
                when {
                    panelCount % 3 == 0 -> "${panelCount / 3} ramas en paralelo de 3 paneles en serie (3S${panelCount / 3}P). Arreglo de ~60V Vmp hacia banco de 48V."
                    panelCount % 4 == 0 -> "${panelCount / 4} ramas en paralelo de 4 paneles en serie (4S${panelCount / 4}P). Arreglo de ~80V Vmp."
                    panelCount % 2 == 0 -> "${panelCount / 2} ramas en paralelo de 2 paneles en serie (mínimo Voc para MPPT de 48V)."
                    else -> "$panelCount paneles: agrupar en strings de al menos 3 en serie para superar el voltaje de carga de 48V (58.4V)."
                }
            }
            else -> "$panelCount paneles de 200W conectados según especificaciones del controlador MPPT."
        }
    }
}
