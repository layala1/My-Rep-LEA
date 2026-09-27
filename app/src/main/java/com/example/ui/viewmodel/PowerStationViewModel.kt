package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.StationEntity
import com.example.data.local.StationRepository
import com.example.data.model.Appliance
import com.example.data.model.CalculationSettings
import com.example.data.model.PowerStationResult
import com.example.domain.AppliancePresets
import com.example.domain.EnergyCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PowerStationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StationRepository

    private val _appliances = MutableStateFlow<List<Appliance>>(AppliancePresets.getDefaultInitialList())
    val appliances: StateFlow<List<Appliance>> = _appliances.asStateFlow()

    private val _settings = MutableStateFlow(CalculationSettings())
    val settings: StateFlow<CalculationSettings> = _settings.asStateFlow()

    val calculationResult: StateFlow<PowerStationResult> = combine(_appliances, _settings) { apps, sets ->
        EnergyCalculator.calculate(apps, sets)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EnergyCalculator.calculate(_appliances.value, _settings.value)
    )

    val savedStations: StateFlow<List<StationEntity>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = StationRepository(database.stationDao())
        savedStations = repository.allSavedStations.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun addAppliance(appliance: Appliance) {
        _appliances.value = _appliances.value + appliance
    }

    fun updateAppliance(updated: Appliance) {
        _appliances.value = _appliances.value.map { if (it.id == updated.id) updated else it }
    }

    fun removeAppliance(id: String) {
        _appliances.value = _appliances.value.filterNot { it.id == id }
    }

    fun toggleAppliance(id: String) {
        _appliances.value = _appliances.value.map {
            if (it.id == id) it.copy(isEnabled = !it.isEnabled) else it
        }
    }

    fun updateQuantity(id: String, delta: Int) {
        _appliances.value = _appliances.value.mapNotNull {
            if (it.id == id) {
                val newQty = it.quantity + delta
                if (newQty > 0) it.copy(quantity = newQty) else null
            } else it
        }
    }

    fun updateHours(id: String, hours: Double) {
        _appliances.value = _appliances.value.map {
            if (it.id == id) it.copy(hoursPerDay = hours.coerceIn(0.1, 24.0)) else it
        }
    }

    fun updateWatts(id: String, watts: Double) {
        _appliances.value = _appliances.value.map {
            if (it.id == id) it.copy(wattsPerUnit = watts.coerceAtLeast(1.0)) else it
        }
    }

    fun updateSettings(newSettings: CalculationSettings) {
        _settings.value = newSettings
    }

    fun loadPresetScenario(scenario: String) {
        when (scenario) {
            "cabin" -> _appliances.value = AppliancePresets.getScenarioCabinOffgrid()
            "emergency" -> _appliances.value = AppliancePresets.getScenarioEmergencyKit()
            "standard" -> _appliances.value = AppliancePresets.getDefaultInitialList()
        }
    }

    fun clearAllAppliances() {
        _appliances.value = emptyList()
    }

    fun saveCurrentStation(name: String, onSaved: () -> Unit = {}) {
        val currentResult = calculationResult.value
        val currentApps = _appliances.value
        val currentSettings = _settings.value

        viewModelScope.launch {
            repository.saveStation(
                name = name.ifBlank { "Mi Estación Solar" },
                appliances = currentApps,
                settings = currentSettings,
                totalWatts = currentResult.totalRunningWatts,
                dailyWh = currentResult.dailyEnergyWh,
                batteryCount = currentResult.batteryBank.batteryCount,
                systemVoltage = currentResult.batteryBank.systemVoltage,
                inverterWatts = currentResult.inverter.recommendedRatedWatts,
                solarPanelsCount = currentResult.solarCharging.recommendedPanelsCount
            )
            onSaved()
        }
    }

    fun loadStation(entity: StationEntity) {
        val loadedAppliances = StationRepository.deserializeAppliances(entity.appliancesJson)
        val loadedSettings = StationRepository.deserializeSettings(entity.settingsJson)
        if (loadedAppliances.isNotEmpty()) {
            _appliances.value = loadedAppliances
        }
        _settings.value = loadedSettings
    }

    fun deleteStation(id: Long) {
        viewModelScope.launch {
            repository.deleteStation(id)
        }
    }
}
