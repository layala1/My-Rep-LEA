package com.example.data.model

data class CalculationSettings(
    val depthOfDischarge: Double = 0.85, // 85% recommended for LiFePO4 longevity
    val inverterEfficiency: Double = 0.90, // 90% typical pure sine wave
    val peakSunHours: Double = 4.5, // 4.5 HSP average solar day
    val solarLossFactor: Double = 0.78, // 78% net solar panel + MPPT performance ratio
    val autonomyDays: Double = 1.0, // 1 day of energy backup
    val preferredSystemVoltage: SystemVoltagePreference = SystemVoltagePreference.AUTO,
    val singleBatteryVoltage: Double = 12.8, // 12.8V nominal LiFePO4
    val singleBatteryAh: Double = 100.0, // 100Ah per battery
    val singlePanelWatts: Double = 200.0 // 200W solar panel
)

enum class SystemVoltagePreference(val label: String, val voltage: Int?) {
    AUTO("Automático Recomendado", null),
    V12("12V (12.8V)", 12),
    V24("24V (25.6V)", 24),
    V48("48V (51.2V)", 48)
}

data class BatteryBankResult(
    val batteryCount: Int,
    val singleBatteryVoltage: Double = 12.8,
    val singleBatteryAh: Double = 100.0,
    val singleBatteryWh: Double = 1280.0,
    val totalInstalledWh: Double,
    val totalUsableWh: Double,
    val systemVoltage: Int, // 12, 24, 48
    val seriesCount: Int, // batteries in series
    val parallelCount: Int, // strings in parallel
    val totalAhBank: Double, // total Ah of bank at systemVoltage
    val connectionDescription: String,
    val maxContinuousDischargeAmps: Double
)

data class InverterResult(
    val continuousWatts: Double, // running watts with safety factor
    val surgeWatts: Double, // peak motor starting surge
    val recommendedRatedWatts: Int, // standard market size e.g. 1000W, 1500W, 2000W, 3000W, etc.
    val recommendedSurgeRating: Int, // e.g. 3000W, 4000W, 6000W
    val systemDcVoltage: Int, // 12V, 24V or 48V
    val waveformType: String = "Onda Sinusoidal Pura (Pure Sine Wave)",
    val maxDcCurrentAmps: Double,
    val recommendedCableGauge: String, // AWG recommendation
    val recommendedFuseAmps: Int,
    val technicalRationale: String
)

data class AcChargingResult(
    val recommendedChargerAmps: Double,
    val recommendedChargerWatts: Double,
    val chargerAlgorithm: String = "CC/CV LiFePO4",
    val bulkAbsorptionVoltage: Double, // e.g. 14.4V - 14.6V per 12V unit
    val floatVoltage: Double, // 13.6V - 13.8V per 12V unit
    val hoursToFullChargeFromZero: Double,
    val hoursToFullChargeFromDoD: Double, // from 15% to 100%
    val estimatedKwhPerCharge: Double,
    val wallOutletRequirement: String
)

data class SolarChargingResult(
    val singlePanelWatts: Double = 200.0,
    val panelsForDailyConsumption: Int,
    val panelsForFullRechargeOneDay: Int,
    val recommendedPanelsCount: Int,
    val totalSolarArrayWatts: Double,
    val peakSunHours: Double,
    val dailySolarGenerationWh: Double,
    val mpptMinCurrentAmps: Double,
    val recommendedMpptRatingAmps: Int,
    val recommendedArrayWiring: String,
    val solarCoverageRatioPercent: Double
)

data class PowerStationResult(
    val totalRunningWatts: Double,
    val totalPeakSurgeWatts: Double,
    val dailyEnergyWh: Double,
    val dailyEnergyKwh: Double,
    val activeAppliancesCount: Int,
    val batteryBank: BatteryBankResult,
    val inverter: InverterResult,
    val acCharging: AcChargingResult,
    val solarCharging: SolarChargingResult,
    val settings: CalculationSettings
)
