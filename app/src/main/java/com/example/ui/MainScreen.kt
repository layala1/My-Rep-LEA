package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddApplianceDialog
import com.example.ui.components.SaveStationDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ResultsScreen
import com.example.ui.screens.SavedStationsScreen
import com.example.ui.screens.SystemDiagramScreen
import com.example.ui.viewmodel.PowerStationViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: PowerStationViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }

    val appliances by viewModel.appliances.collectAsStateWithLifecycle()
    val calculationResult by viewModel.calculationResult.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val savedStations by viewModel.savedStations.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (selectedTab) {
                            0 -> "Estación LiFePO4"
                            1 -> "Dimensionamiento"
                            2 -> "Esquema Eléctrico"
                            3 -> "Proyectos Guardados"
                            else -> "Estación LiFePO4"
                        },
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showSaveDialog = true },
                        modifier = Modifier.testTag("top_bar_save_action")
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = "Guardar configuración")
                    }
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.testTag("top_bar_settings_action")
                    ) {
                        Icon(imageVector = Icons.Default.Tune, contentDescription = "Ajustes de cálculo")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.FormatListBulleted, contentDescription = "Equipos", modifier = Modifier.size(22.dp)) },
                    label = { Text("Equipos") },
                    modifier = Modifier.testTag("nav_item_appliances")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.BatteryChargingFull, contentDescription = "Resultados", modifier = Modifier.size(22.dp)) },
                    label = { Text("Cálculo") },
                    modifier = Modifier.testTag("nav_item_results")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.AccountTree, contentDescription = "Esquema", modifier = Modifier.size(22.dp)) },
                    label = { Text("Esquema") },
                    modifier = Modifier.testTag("nav_item_diagram")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Bookmark, contentDescription = "Guardados", modifier = Modifier.size(22.dp)) },
                    label = { Text("Guardados") },
                    modifier = Modifier.testTag("nav_item_saved")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> HomeScreen(
                    appliances = appliances,
                    result = calculationResult,
                    onAddApplianceClick = { showAddDialog = true },
                    onToggleAppliance = { viewModel.toggleAppliance(it) },
                    onQuantityChange = { id, delta -> viewModel.updateQuantity(id, delta) },
                    onDeleteAppliance = { viewModel.removeAppliance(it) },
                    onLoadScenario = { scenario ->
                        viewModel.loadPresetScenario(scenario)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Plantilla cargada")
                        }
                    },
                    onClearAll = {
                        viewModel.clearAllAppliances()
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Lista vaciada")
                        }
                    },
                    onNavigateToResults = { selectedTab = 1 }
                )
                1 -> ResultsScreen(
                    result = calculationResult,
                    onNavigateToDiagram = { selectedTab = 2 }
                )
                2 -> SystemDiagramScreen(
                    result = calculationResult
                )
                3 -> SavedStationsScreen(
                    savedStations = savedStations,
                    onLoadStation = { station ->
                        viewModel.loadStation(station)
                        selectedTab = 0
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Configuración '${station.name}' cargada")
                        }
                    },
                    onDeleteStation = { viewModel.deleteStation(it) },
                    onOpenSaveDialog = { showSaveDialog = true },
                    onOpenSettingsDialog = { showSettingsDialog = true }
                )
            }
        }
    }

    if (showAddDialog) {
        AddApplianceDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { newApp ->
                viewModel.addAppliance(newApp)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("${newApp.name} agregado")
                }
            }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            currentSettings = settings,
            onDismiss = { showSettingsDialog = false },
            onSave = { updatedSettings ->
                viewModel.updateSettings(updatedSettings)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Ajustes actualizados")
                }
            }
        )
    }

    if (showSaveDialog) {
        SaveStationDialog(
            onDismiss = { showSaveDialog = false },
            onSave = { name ->
                viewModel.saveCurrentStation(name) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Proyecto '$name' guardado con éxito")
                    }
                }
            }
        )
    }
}
