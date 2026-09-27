package com.example.domain

import com.example.data.model.Appliance
import com.example.data.model.ApplianceCategory

object AppliancePresets {

    val CATALOG: List<Appliance> = listOf(
        Appliance(
            name = "Refrigerador / Nevera Inverter",
            category = ApplianceCategory.REFRIGERATION,
            quantity = 1,
            wattsPerUnit = 140.0,
            hoursPerDay = 10.0, // Effective compressor duty cycle hours
            surgeMultiplier = 2.5
        ),
        Appliance(
            name = "Iluminación LED (6 focos)",
            category = ApplianceCategory.LIGHTING,
            quantity = 1,
            wattsPerUnit = 60.0,
            hoursPerDay = 5.0,
            surgeMultiplier = 1.0
        ),
        Appliance(
            name = "Smart TV 55\" / Pantalla",
            category = ApplianceCategory.ELECTRONICS,
            quantity = 1,
            wattsPerUnit = 110.0,
            hoursPerDay = 4.0,
            surgeMultiplier = 1.2
        ),
        Appliance(
            name = "Laptop / Computadora Portátil",
            category = ApplianceCategory.ELECTRONICS,
            quantity = 1,
            wattsPerUnit = 65.0,
            hoursPerDay = 6.0,
            surgeMultiplier = 1.1
        ),
        Appliance(
            name = "Router WiFi / Módem Internet",
            category = ApplianceCategory.ELECTRONICS,
            quantity = 1,
            wattsPerUnit = 15.0,
            hoursPerDay = 24.0,
            surgeMultiplier = 1.0
        ),
        Appliance(
            name = "Ventilador de Pedestal",
            category = ApplianceCategory.CLIMATE,
            quantity = 2,
            wattsPerUnit = 50.0,
            hoursPerDay = 8.0,
            surgeMultiplier = 1.6
        ),
        Appliance(
            name = "Microondas",
            category = ApplianceCategory.KITCHEN,
            quantity = 1,
            wattsPerUnit = 1000.0,
            hoursPerDay = 0.35, // ~20 min
            surgeMultiplier = 1.5
        ),
        Appliance(
            name = "Licuadora",
            category = ApplianceCategory.KITCHEN,
            quantity = 1,
            wattsPerUnit = 450.0,
            hoursPerDay = 0.15, // ~10 min
            surgeMultiplier = 2.0
        ),
        Appliance(
            name = "Cafetera Eléctrica",
            category = ApplianceCategory.KITCHEN,
            quantity = 1,
            wattsPerUnit = 850.0,
            hoursPerDay = 0.25, // 15 min
            surgeMultiplier = 1.0
        ),
        Appliance(
            name = "Cargadores de Móvil (2x)",
            category = ApplianceCategory.ELECTRONICS,
            quantity = 2,
            wattsPerUnit = 20.0,
            hoursPerDay = 3.0,
            surgeMultiplier = 1.0
        ),
        Appliance(
            name = "Antena Starlink / Satelital",
            category = ApplianceCategory.ELECTRONICS,
            quantity = 1,
            wattsPerUnit = 55.0,
            hoursPerDay = 24.0,
            surgeMultiplier = 1.2
        ),
        Appliance(
            name = "Bomba de Agua 1/2 HP",
            category = ApplianceCategory.TOOLS,
            quantity = 1,
            wattsPerUnit = 375.0,
            hoursPerDay = 1.0,
            surgeMultiplier = 3.0 // High inrush motor current
        ),
        Appliance(
            name = "Aire Acondicionado Inverter 9000 BTU",
            category = ApplianceCategory.CLIMATE,
            quantity = 1,
            wattsPerUnit = 650.0,
            hoursPerDay = 6.0,
            surgeMultiplier = 2.0
        )
    )

    fun getDefaultInitialList(): List<Appliance> {
        return listOf(
            Appliance(
                name = "Refrigerador / Nevera Inverter",
                category = ApplianceCategory.REFRIGERATION,
                quantity = 1,
                wattsPerUnit = 140.0,
                hoursPerDay = 10.0,
                surgeMultiplier = 2.5
            ),
            Appliance(
                name = "Iluminación LED (4 focos)",
                category = ApplianceCategory.LIGHTING,
                quantity = 1,
                wattsPerUnit = 40.0,
                hoursPerDay = 5.0,
                surgeMultiplier = 1.0
            ),
            Appliance(
                name = "Smart TV 55\"",
                category = ApplianceCategory.ELECTRONICS,
                quantity = 1,
                wattsPerUnit = 100.0,
                hoursPerDay = 4.0,
                surgeMultiplier = 1.1
            ),
            Appliance(
                name = "Router WiFi Fibra",
                category = ApplianceCategory.ELECTRONICS,
                quantity = 1,
                wattsPerUnit = 15.0,
                hoursPerDay = 24.0,
                surgeMultiplier = 1.0
            ),
            Appliance(
                name = "Ventilador",
                category = ApplianceCategory.CLIMATE,
                quantity = 1,
                wattsPerUnit = 50.0,
                hoursPerDay = 7.0,
                surgeMultiplier = 1.5
            )
        )
    }

    fun getScenarioCabinOffgrid(): List<Appliance> {
        return listOf(
            Appliance(
                name = "Refrigerador Inverter",
                category = ApplianceCategory.REFRIGERATION,
                quantity = 1,
                wattsPerUnit = 140.0,
                hoursPerDay = 10.0,
                surgeMultiplier = 2.5
            ),
            Appliance(
                name = "Luces LED cabaña",
                category = ApplianceCategory.LIGHTING,
                quantity = 1,
                wattsPerUnit = 50.0,
                hoursPerDay = 4.0,
                surgeMultiplier = 1.0
            ),
            Appliance(
                name = "Starlink Satelital",
                category = ApplianceCategory.ELECTRONICS,
                quantity = 1,
                wattsPerUnit = 55.0,
                hoursPerDay = 16.0,
                surgeMultiplier = 1.2
            ),
            Appliance(
                name = "Bomba de agua 0.5 HP",
                category = ApplianceCategory.TOOLS,
                quantity = 1,
                wattsPerUnit = 375.0,
                hoursPerDay = 0.8,
                surgeMultiplier = 3.0
            ),
            Appliance(
                name = "Licuadora",
                category = ApplianceCategory.KITCHEN,
                quantity = 1,
                wattsPerUnit = 450.0,
                hoursPerDay = 0.2,
                surgeMultiplier = 2.0
            ),
            Appliance(
                name = "Cargadores de móvil",
                category = ApplianceCategory.ELECTRONICS,
                quantity = 2,
                wattsPerUnit = 20.0,
                hoursPerDay = 3.0,
                surgeMultiplier = 1.0
            )
        )
    }

    fun getScenarioEmergencyKit(): List<Appliance> {
        return listOf(
            Appliance(
                name = "Nevera / Refrigerador",
                category = ApplianceCategory.REFRIGERATION,
                quantity = 1,
                wattsPerUnit = 140.0,
                hoursPerDay = 8.0,
                surgeMultiplier = 2.5
            ),
            Appliance(
                name = "Router WiFi y Celulares",
                category = ApplianceCategory.ELECTRONICS,
                quantity = 1,
                wattsPerUnit = 35.0,
                hoursPerDay = 18.0,
                surgeMultiplier = 1.0
            ),
            Appliance(
                name = "Luces LED emergencia",
                category = ApplianceCategory.LIGHTING,
                quantity = 1,
                wattsPerUnit = 30.0,
                hoursPerDay = 5.0,
                surgeMultiplier = 1.0
            ),
            Appliance(
                name = "Ventilador",
                category = ApplianceCategory.CLIMATE,
                quantity = 1,
                wattsPerUnit = 45.0,
                hoursPerDay = 6.0,
                surgeMultiplier = 1.5
            )
        )
    }
}
