package com.example.data.model

import java.util.UUID

data class Appliance(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: ApplianceCategory = ApplianceCategory.GENERAL,
    val quantity: Int = 1,
    val wattsPerUnit: Double,
    val hoursPerDay: Double,
    val surgeMultiplier: Double = 1.0,
    val isEnabled: Boolean = true
) {
    val totalRunningWatts: Double
        get() = if (isEnabled) quantity * wattsPerUnit else 0.0

    val totalPeakWatts: Double
        get() = if (isEnabled) totalRunningWatts * surgeMultiplier else 0.0

    val dailyWattHours: Double
        get() = if (isEnabled) totalRunningWatts * hoursPerDay else 0.0
}

enum class ApplianceCategory(val label: String, val iconName: String) {
    REFRIGERATION("Refrigeración", "Kitchen"),
    LIGHTING("Iluminación", "Lightbulb"),
    ELECTRONICS("Electrónica", "Devices"),
    CLIMATE("Climatización", "Air"),
    KITCHEN("Cocina", "SoupKitchen"),
    TOOLS("Motores y Bombas", "Build"),
    GENERAL("General", "Power")
}
