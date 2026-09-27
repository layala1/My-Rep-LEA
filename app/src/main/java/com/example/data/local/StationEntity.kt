package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_stations")
data class StationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val totalWatts: Double,
    val dailyWh: Double,
    val batteryCount: Int,
    val systemVoltage: Int,
    val inverterWatts: Int,
    val solarPanelsCount: Int,
    val appliancesJson: String,
    val settingsJson: String
)
