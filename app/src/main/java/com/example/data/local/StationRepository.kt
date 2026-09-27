package com.example.data.local

import com.example.data.model.Appliance
import com.example.data.model.ApplianceCategory
import com.example.data.model.CalculationSettings
import com.example.data.model.SystemVoltagePreference
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class StationRepository(private val stationDao: StationDao) {

    val allSavedStations: Flow<List<StationEntity>> = stationDao.getAllStations()

    suspend fun saveStation(
        name: String,
        appliances: List<Appliance>,
        settings: CalculationSettings,
        totalWatts: Double,
        dailyWh: Double,
        batteryCount: Int,
        systemVoltage: Int,
        inverterWatts: Int,
        solarPanelsCount: Int
    ): Long {
        val appliancesJson = serializeAppliances(appliances)
        val settingsJson = serializeSettings(settings)

        val entity = StationEntity(
            name = name,
            totalWatts = totalWatts,
            dailyWh = dailyWh,
            batteryCount = batteryCount,
            systemVoltage = systemVoltage,
            inverterWatts = inverterWatts,
            solarPanelsCount = solarPanelsCount,
            appliancesJson = appliancesJson,
            settingsJson = settingsJson
        )
        return stationDao.insertStation(entity)
    }

    suspend fun deleteStation(id: Long) {
        stationDao.deleteStationById(id)
    }

    companion object {
        fun serializeAppliances(appliances: List<Appliance>): String {
            val array = JSONArray()
            appliances.forEach { app ->
                val obj = JSONObject().apply {
                    put("id", app.id)
                    put("name", app.name)
                    put("category", app.category.name)
                    put("quantity", app.quantity)
                    put("wattsPerUnit", app.wattsPerUnit)
                    put("hoursPerDay", app.hoursPerDay)
                    put("surgeMultiplier", app.surgeMultiplier)
                    put("isEnabled", app.isEnabled)
                }
                array.put(obj)
            }
            return array.toString()
        }

        fun deserializeAppliances(json: String): List<Appliance> {
            val list = mutableListOf<Appliance>()
            try {
                val array = JSONArray(json)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val cat = try {
                        ApplianceCategory.valueOf(obj.getString("category"))
                    } catch (e: Exception) {
                        ApplianceCategory.GENERAL
                    }
                    list.add(
                        Appliance(
                            id = obj.optString("id"),
                            name = obj.getString("name"),
                            category = cat,
                            quantity = obj.getInt("quantity"),
                            wattsPerUnit = obj.getDouble("wattsPerUnit"),
                            hoursPerDay = obj.getDouble("hoursPerDay"),
                            surgeMultiplier = obj.optDouble("surgeMultiplier", 1.0),
                            isEnabled = obj.optBoolean("isEnabled", true)
                        )
                    )
                }
            } catch (e: Exception) {
                // Return empty list on parse error
            }
            return list
        }

        fun serializeSettings(settings: CalculationSettings): String {
            val obj = JSONObject().apply {
                put("depthOfDischarge", settings.depthOfDischarge)
                put("inverterEfficiency", settings.inverterEfficiency)
                put("peakSunHours", settings.peakSunHours)
                put("solarLossFactor", settings.solarLossFactor)
                put("autonomyDays", settings.autonomyDays)
                put("preferredSystemVoltage", settings.preferredSystemVoltage.name)
            }
            return obj.toString()
        }

        fun deserializeSettings(json: String): CalculationSettings {
            return try {
                val obj = JSONObject(json)
                val pref = try {
                    SystemVoltagePreference.valueOf(obj.getString("preferredSystemVoltage"))
                } catch (e: Exception) {
                    SystemVoltagePreference.AUTO
                }
                CalculationSettings(
                    depthOfDischarge = obj.optDouble("depthOfDischarge", 0.85),
                    inverterEfficiency = obj.optDouble("inverterEfficiency", 0.90),
                    peakSunHours = obj.optDouble("peakSunHours", 4.5),
                    solarLossFactor = obj.optDouble("solarLossFactor", 0.78),
                    autonomyDays = obj.optDouble("autonomyDays", 1.0),
                    preferredSystemVoltage = pref
                )
            } catch (e: Exception) {
                CalculationSettings()
            }
        }
    }
}
