package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SolarPower
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PowerStationResult

@Composable
fun ResultsScreen(
    result: PowerStationResult,
    onNavigateToDiagram: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedSection by remember { mutableIntStateOf(0) }

    Column(modifier = modifier.fillMaxSize()) {
        // Top navigation tabs
        TabRow(
            selectedTabIndex = selectedSection,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedSection == 0,
                onClick = { selectedSection = 0 },
                text = { Text("Baterías & Inversor", maxLines = 1) },
                icon = { Icon(Icons.Default.BatteryChargingFull, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedSection == 1,
                onClick = { selectedSection = 1 },
                text = { Text("Dos Vías de Carga", maxLines = 1) },
                icon = { Icon(Icons.Default.SolarPower, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (selectedSection == 0) {
                // Section 1: Batteries & Inverter
                item {
                    ConsumptionSummaryCard(result = result)
                }

                item {
                    LiFePo4BatteryBankCard(result = result)
                }

                item {
                    PureSineInverterCard(result = result)
                }

                item {
                    Button(
                        onClick = onNavigateToDiagram,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Cable, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ver Esquema de Conexión del Sistema")
                    }
                }
            } else {
                // Section 2: Two charging pathways
                item {
                    TwoWaysChargingHeaderCard()
                }

                item {
                    Via1AcHouseChargingCard(result = result)
                }

                item {
                    Via2SolarPanelsCard(result = result)
                }

                item {
                    ShareExportCard(result = result, context = context)
                }
            }
        }
    }
}

@Composable
private fun ConsumptionSummaryCard(result: PowerStationResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Resumen de Carga Eléctrica",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryStat(
                    label = "Potencia Continua",
                    value = "${result.totalRunningWatts.toInt()} W",
                    caption = "Total equipos simultáneos"
                )
                SummaryStat(
                    label = "Pico de Arranque",
                    value = "${result.totalPeakSurgeWatts.toInt()} W",
                    caption = "Compresores / motores"
                )
                SummaryStat(
                    label = "Consumo Diario",
                    value = "${String.format("%.2f", result.dailyEnergyKwh)} kWh",
                    caption = "${result.dailyEnergyWh.toInt()} Wh/día"
                )
            }
        }
    }
}

@Composable
private fun LiFePo4BatteryBankCard(result: PowerStationResult) {
    val bank = result.batteryBank
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("battery_bank_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF10B981),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.BatteryChargingFull,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Banco LiFePO4 Requerido",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Baterías de Litio Fosfato 12.8V / 100Ah",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text(
                        text = "${bank.batteryCount} Batería(s)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF10B981)
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Specs grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SpecRow(
                    label = "Configuración del Banco",
                    value = "${bank.systemVoltage}V DC (${bank.seriesCount}S${bank.parallelCount}P)"
                )
                SpecRow(
                    label = "Capacidad Total Instalada",
                    value = "${String.format("%.2f", bank.totalInstalledWh / 1000.0)} kWh (${bank.totalInstalledWh.toInt()} Wh)"
                )
                SpecRow(
                    label = "Capacidad Útil (${(result.settings.depthOfDischarge * 100).toInt()}% DoD)",
                    value = "${String.format("%.2f", bank.totalUsableWh / 1000.0)} kWh utilizables"
                )
                SpecRow(
                    label = "Capacidad en Amperios-hora",
                    value = "${bank.totalAhBank.toInt()} Ah a ${bank.systemVoltage}V"
                )
                SpecRow(
                    label = "Descarga Máx. Continua",
                    value = "${bank.maxContinuousDischargeAmps.toInt()} A continuo"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Connection instructions box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Conexión física: ${bank.connectionDescription}. Las baterías LiFePO4 de 12.8V incluyen BMS interno que protege contra sobretensión, subtensión y sobrecorriente.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun PureSineInverterCard(result: PowerStationResult) {
    val inv = result.inverter
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pure_sine_inverter_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF06B6D4),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Waves,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Inversor Onda Sinusoidal Pura",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Pure Sine Wave Inverter",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF06B6D4).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${inv.recommendedRatedWatts} W",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF06B6D4)
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SpecRow(
                    label = "Potencia Nominal Continua",
                    value = "${inv.recommendedRatedWatts} W (Onda Pura)"
                )
                SpecRow(
                    label = "Potencia Pico / Arranque (Surge)",
                    value = "${inv.recommendedSurgeRating} W (3 a 5 seg)"
                )
                SpecRow(
                    label = "Voltaje Entrada DC",
                    value = "${inv.systemDcVoltage}V DC (Directo a Baterías)"
                )
                SpecRow(
                    label = "Salida AC",
                    value = "110V - 120V / 220V AC @ 60Hz/50Hz"
                )
                SpecRow(
                    label = "Corriente Máx. en Cables DC",
                    value = "${String.format("%.1f", inv.maxDcCurrentAmps)} A"
                )
                SpecRow(
                    label = "Calibre Cable DC Recomendado",
                    value = inv.recommendedCableGauge
                )
                SpecRow(
                    label = "Fusible DC de Protección",
                    value = "${inv.recommendedFuseAmps} A (Tipo ANL o Clase T)"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Explanation box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFF06B6D4).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color(0xFF06B6D4),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "¿Por qué Onda Sinusoidal Pura?",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "La onda pura reproduce fielmente la corriente de red. Es OBLIGATORIA para compresores de neveras, motores, microondas, fuentes de laptops y televisores. Los inversores de 'onda modificada' queman motores, recalientan transformadores y generan zumbidos molestos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun TwoWaysChargingHeaderCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ElectricalServices,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Sistema de Carga Doble Vía",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Vía 1: Red Eléctrica / Casa  •  Vía 2: Paneles Solares de 200W",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun Via1AcHouseChargingCard(result: PowerStationResult) {
    val ac = result.acCharging
    val bank = result.batteryBank

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ac_charging_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF3B82F6),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ElectricalServices,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Vía 1: Carga con Corriente de Casa",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Cargador Inteligente AC a LiFePO4",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF3B82F6).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${ac.recommendedChargerAmps.toInt()} A",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF3B82F6)
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SpecRow(
                    label = "Cargador Recomendado",
                    value = "Cargador LiFePO4 ${bank.systemVoltage}V / ${ac.recommendedChargerAmps.toInt()}A"
                )
                SpecRow(
                    label = "Potencia Consumo de Pared AC",
                    value = "~${ac.recommendedChargerWatts.toInt()} W"
                )
                SpecRow(
                    label = "Tiempo Recarga (desde 15% hasta 100%)",
                    value = "${String.format("%.1f", ac.hoursToFullChargeFromDoD)} horas"
                )
                SpecRow(
                    label = "Tiempo Recarga (desde 0% hasta 100%)",
                    value = "${String.format("%.1f", ac.hoursToFullChargeFromZero)} horas"
                )
                SpecRow(
                    label = "Curva de Carga LiFePO4",
                    value = "CC/CV (Bulk: ${String.format("%.1f", ac.bulkAbsorptionVoltage)}V | Flotación: ${String.format("%.1f", ac.floatVoltage)}V)"
                )
                SpecRow(
                    label = "Requisito de Enchufe Doméstico",
                    value = ac.wallOutletRequirement
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = "Nota de Carga: Las baterías LiFePO4 NO deben cargarse con cargadores de plomo-ácido convencionales porque carecen del perfil CC/CV y pueden sobrecargar las celdas o disparar el BMS.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun Via2SolarPanelsCard(result: PowerStationResult) {
    val solar = result.solarCharging
    val bank = result.batteryBank

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("solar_charging_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.SolarPower,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Vía 2: Paneles Solares de 200W",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Módulos monocristalinos de 200W",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF59E0B).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${solar.recommendedPanelsCount} Paneles",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFF59E0B)
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SpecRow(
                    label = "Paneles de 200W para cubrir Consumo Diario",
                    value = "${solar.panelsForDailyConsumption} paneles (${solar.panelsForDailyConsumption * 200}W)"
                )
                SpecRow(
                    label = "Paneles para recargar TODO el banco en 1 día de sol",
                    value = "${solar.panelsForFullRechargeOneDay} paneles (${solar.panelsForFullRechargeOneDay * 200}W)"
                )
                SpecRow(
                    label = "Arreglo Solar Recomendado",
                    value = "${solar.recommendedPanelsCount} paneles (${solar.totalSolarArrayWatts.toInt()} Wp)"
                )
                SpecRow(
                    label = "Generación Solar Diaria Estimada",
                    value = "${String.format("%.2f", solar.dailySolarGenerationWh / 1000.0)} kWh/día (con ${solar.peakSunHours} HSP)"
                )
                SpecRow(
                    label = "Controlador de Carga MPPT",
                    value = "Mínimo ${solar.recommendedMpptRatingAmps} A (${bank.systemVoltage}V)"
                )
                SpecRow(
                    label = "Esquema de Conexión de Paneles",
                    value = solar.recommendedArrayWiring
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFFF59E0B).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = "Controlador MPPT: Se recomienda un regulador solar tipo MPPT (no PWM) para aprovechar hasta un 30% más de energía de los paneles de 200W y permitir conectar paneles en serie para reducir caídas de voltaje.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun ShareExportCard(result: PowerStationResult, context: Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Exportar Lista y Especificaciones",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Comparte el reporte técnico y lista de materiales completa para armar tu estación de energía.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { shareReport(context, result) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Compartir Reporte Técnico")
            }
        }
    }
}

private fun shareReport(context: Context, result: PowerStationResult) {
    val text = buildString {
        appendLine("⚡ REPORTE DE ESTACIÓN DE ENERGÍA SOLAR LiFePO4")
        appendLine("==============================================")
        appendLine("1. DEMANDA ELÉCTRICA:")
        appendLine("• Potencia continua de cargas: ${result.totalRunningWatts.toInt()} W")
        appendLine("• Pico de arranque estimado: ${result.totalPeakSurgeWatts.toInt()} W")
        appendLine("• Consumo diario: ${String.format("%.2f", result.dailyEnergyKwh)} kWh/día (${result.dailyEnergyWh.toInt()} Wh/día)")
        appendLine()
        appendLine("2. BANCO DE BATERÍAS (LiFePO4 12.8V 100Ah):")
        appendLine("• Baterías necesarias: ${result.batteryBank.batteryCount} unidades de 12.8V 100Ah (1280 Wh c/u)")
        appendLine("• Voltaje del sistema: ${result.batteryBank.systemVoltage}V DC (${result.batteryBank.seriesCount}S${result.batteryBank.parallelCount}P)")
        appendLine("• Capacidad instalada: ${String.format("%.2f", result.batteryBank.totalInstalledWh / 1000.0)} kWh")
        appendLine("• Capacidad útil: ${String.format("%.2f", result.batteryBank.totalUsableWh / 1000.0)} kWh (DoD ${(result.settings.depthOfDischarge * 100).toInt()}%)")
        appendLine("• Conexión: ${result.batteryBank.connectionDescription}")
        appendLine()
        appendLine("3. INVERSOR DE ONDA SINUSOIDAL PURA:")
        appendLine("• Tipo: Onda Sinusoidal Pura (Pure Sine Wave)")
        appendLine("• Potencia nominal recomendada: ${result.inverter.recommendedRatedWatts} W")
        appendLine("• Potencia pico (Surge): ${result.inverter.recommendedSurgeRating} W")
        appendLine("• Entrada DC: ${result.inverter.systemDcVoltage}V DC")
        appendLine("• Calibre de cable DC: ${result.inverter.recommendedCableGauge}")
        appendLine("• Fusible DC: ${result.inverter.recommendedFuseAmps} A")
        appendLine()
        appendLine("4. VÍA 1 - CARGA CON CORRIENTE DE CASA:")
        appendLine("• Cargador LiFePO4 recomendado: ${result.batteryBank.systemVoltage}V / ${result.acCharging.recommendedChargerAmps.toInt()} A")
        appendLine("• Consumo en toma AC: ~${result.acCharging.recommendedChargerWatts.toInt()} W")
        appendLine("• Tiempo de recarga: ~${String.format("%.1f", result.acCharging.hoursToFullChargeFromDoD)} horas")
        appendLine()
        appendLine("5. VÍA 2 - CARGA SOLAR CON PANELES DE 200W:")
        appendLine("• Paneles solares de 200W requeridos: ${result.solarCharging.recommendedPanelsCount} paneles")
        appendLine("• Potencia solar total: ${result.solarCharging.totalSolarArrayWatts.toInt()} Wp")
        appendLine("• Generación diaria estimada: ${String.format("%.2f", result.solarCharging.dailySolarGenerationWh / 1000.0)} kWh/día")
        appendLine("• Controlador de carga: MPPT de mínimo ${result.solarCharging.recommendedMpptRatingAmps} A")
        appendLine("• Cableado del arreglo: ${result.solarCharging.recommendedArrayWiring}")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Especificaciones Estación de Energía LiFePO4")
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir reporte"))
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SummaryStat(label: String, value: String, caption: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = caption,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
