package com.example

import com.example.data.model.Appliance
import com.example.data.model.CalculationSettings
import com.example.domain.EnergyCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testEnergyCalculator_basicLoad() {
        val appliances = listOf(
            Appliance(
                name = "Nevera",
                quantity = 1,
                wattsPerUnit = 150.0,
                hoursPerDay = 10.0, // 1500 Wh/day
                surgeMultiplier = 2.5
            ),
            Appliance(
                name = "Bombillos LED",
                quantity = 5,
                wattsPerUnit = 10.0,
                hoursPerDay = 6.0 // 300 Wh/day
            )
        )

        val settings = CalculationSettings(
            depthOfDischarge = 0.85,
            inverterEfficiency = 0.90,
            peakSunHours = 4.5
        )

        val result = EnergyCalculator.calculate(appliances, settings)

        // Total Running Watts = 150 + 50 = 200 W
        assertEquals(200.0, result.totalRunningWatts, 0.01)

        // Total Daily Wh = 1500 + 300 = 1800 Wh/day
        assertEquals(1800.0, result.dailyEnergyWh, 0.01)

        // LiFePO4 12.8V 100Ah single battery usable = 1280 * 0.85 * 0.90 = 979.2 Wh
        // 1800 / 979.2 = 1.83 -> At least 2 batteries!
        assertTrue(result.batteryBank.batteryCount >= 2)

        // Inverter should be pure sine wave and at least 500W
        assertTrue(result.inverter.recommendedRatedWatts >= 500)
        assertTrue(result.inverter.recommendedSurgeRating >= 750)

        // 200W solar panels should cover 1800 Wh
        // 200W * 4.5 * 0.78 = 702 Wh/panel -> ~3 panels
        assertTrue(result.solarCharging.recommendedPanelsCount >= 3)
    }
}
