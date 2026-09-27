package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CalculationSettings
import com.example.data.model.SystemVoltagePreference

@Composable
fun SettingsDialog(
    currentSettings: CalculationSettings,
    onDismiss: () -> Unit,
    onSave: (CalculationSettings) -> Unit
) {
    var dod by remember { mutableDoubleStateOf(currentSettings.depthOfDischarge) }
    var hsp by remember { mutableDoubleStateOf(currentSettings.peakSunHours) }
    var autonomy by remember { mutableDoubleStateOf(currentSettings.autonomyDays) }
    var voltagePref by remember { mutableStateOf(currentSettings.preferredSystemVoltage) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("settings_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Parámetros de Cálculo",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Horas Sol Pico (HSP)
                Text(
                    text = "Horas Sol Pico (HSP): ${String.format("%.1f", hsp)} h/día",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Promedio solar de tu región (Típico: 4.0 - 5.5 h en zonas soleadas).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = hsp.toFloat(),
                    onValueChange = { hsp = it.toDouble() },
                    valueRange = 3.0f..6.5f,
                    steps = 6,
                    modifier = Modifier.testTag("hsp_slider")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Profundidad de descarga (DoD)
                Text(
                    text = "Profundidad de Descarga LiFePO4: ${(dod * 100).toInt()}%",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "85% recomendado para máxima vida útil (más de 4000 a 6000 ciclos).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = dod.toFloat(),
                    onValueChange = { dod = it.toDouble() },
                    valueRange = 0.70f..0.95f,
                    steps = 4,
                    modifier = Modifier.testTag("dod_slider")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Días de autonomía
                Text(
                    text = "Días de Autonomía: ${String.format("%.1f", autonomy)} día(s)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Días de reserva de energía en batería sin sol ni recarga.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = autonomy.toFloat(),
                    onValueChange = { autonomy = it.toDouble() },
                    valueRange = 1.0f..3.0f,
                    steps = 3,
                    modifier = Modifier.testTag("autonomy_slider")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Voltaje del Sistema preferido
                Text(
                    text = "Voltaje del Banco de Baterías:",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SystemVoltagePreference.entries.forEach { pref ->
                        FilterChip(
                            selected = voltagePref == pref,
                            onClick = { voltagePref = pref },
                            label = {
                                Text(
                                    text = when (pref) {
                                        SystemVoltagePreference.AUTO -> "Auto"
                                        SystemVoltagePreference.V12 -> "12V"
                                        SystemVoltagePreference.V24 -> "24V"
                                        SystemVoltagePreference.V48 -> "48V"
                                    }
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val def = CalculationSettings()
                            dod = def.depthOfDischarge
                            hsp = def.peakSunHours
                            autonomy = def.autonomyDays
                            voltagePref = def.preferredSystemVoltage
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset")
                    }

                    Button(
                        onClick = {
                            onSave(
                                currentSettings.copy(
                                    depthOfDischarge = dod,
                                    peakSunHours = hsp,
                                    autonomyDays = autonomy,
                                    preferredSystemVoltage = voltagePref
                                )
                            )
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_settings_btn")
                    ) {
                        Text("Aplicar")
                    }
                }
            }
        }
    }
}
