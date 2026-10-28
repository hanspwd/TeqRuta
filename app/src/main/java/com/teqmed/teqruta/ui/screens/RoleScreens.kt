package com.teqmed.teqruta.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teqmed.teqruta.model.Viaje
import com.teqmed.teqruta.ui.components.MetricSummaryCard
import com.teqmed.teqruta.ui.components.StatusChip
import com.teqmed.teqruta.ui.components.clearFocusOnTap
import com.teqmed.teqruta.ui.theme.ErrorRed
import com.teqmed.teqruta.ui.theme.InfoBlue
import com.teqmed.teqruta.ui.theme.SuccessGreen
import com.teqmed.teqruta.ui.theme.WarningYellow
import com.teqmed.teqruta.utils.FormatUtils
import com.teqmed.teqruta.viewmodel.ViajeViewModel

@Composable
fun TecnicoDashboard(viewModel: ViajeViewModel) {
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf("Viaje Actual", "Historial")
    val haptic = LocalHapticFeedback.current

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            AnimatedContent(
                targetState = selectedItem,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tecnico_tab"
            ) { tab ->
                if (tab == 0) {
                    ActiveTripScreen(viewModel)
                } else {
                    HistoryScreen(viewModel)
                }
            }
        }
        NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
            NavigationBarItem(
                icon = { Icon(Icons.Filled.DirectionsCar, contentDescription = "Viaje Actual") },
                label = { Text(items[0], fontWeight = FontWeight.Bold) },
                selected = selectedItem == 0,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    selectedItem = 0
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
            NavigationBarItem(
                icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Historial") },
                label = { Text(items[1], fontWeight = FontWeight.Bold) },
                selected = selectedItem == 1,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    selectedItem = 1
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

@Composable
fun JefeDashboard(viewModel: ViajeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val viajes by viewModel.viajesDb.collectAsState()
    val haptic = LocalHapticFeedback.current

    val pendientes = viajes.filter { it.estadoAprobacion == "PENDIENTE" }
    val aprobados = viajes.filter { it.estadoAprobacion == "APROBADO" }

    var selectedViajeForDialog by remember { mutableStateOf<Viaje?>(null) }
    var observationText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clearFocusOnTap()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Panel de Jefatura de Terreno",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Validación y control de kilometraje técnico",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Metrics Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricSummaryCard(
                title = "Por Aprobar",
                value = pendientes.size.toString(),
                subtitle = "Viajes pendientes",
                icon = Icons.AutoMirrored.Outlined.FactCheck,
                accentColor = WarningYellow,
                modifier = Modifier.weight(1f)
            )
            MetricSummaryCard(
                title = "Aprobados",
                value = aprobados.size.toString(),
                subtitle = "Conformes",
                icon = Icons.Outlined.CheckCircle,
                accentColor = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Viajes Pendientes de Validación",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (pendientes.isEmpty()) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("No hay viajes pendientes de aprobación.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            pendientes.forEach { viaje ->
                val dist = (viaje.odometroFin - viaje.odometroInicio).coerceAtLeast(0)
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                viaje.rutaSeleccionada,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            StatusChip("Pendiente", Icons.Outlined.HourglassEmpty, WarningYellow)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Técnico: ${viaje.tecnicoEmail}", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "Recorrido: ${FormatUtils.formatNumber(dist)} km (${FormatUtils.formatNumber(viaje.odometroInicio)} → ${FormatUtils.formatNumber(viaje.odometroFin)} km)",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "Monto Peajes: ${FormatUtils.formatCurrency(viaje.totalMontoPeajes)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { selectedViajeForDialog = viaje },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Observar")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.cambiarEstadoAprobacion(viaje.uuid, "APROBADO")
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Aprobar")
                            }
                        }
                    }
                }
            }
        }
    }

    // Observation Dialog
    if (selectedViajeForDialog != null) {
        AlertDialog(
            onDismissRequest = { selectedViajeForDialog = null },
            title = { Text("Registrar Observación") },
            text = {
                Column {
                    Text("Indique la discrepancia encontrada para el viaje a ${selectedViajeForDialog?.rutaSeleccionada}:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = observationText,
                        onValueChange = { observationText = it },
                        label = { Text("Motivo / Observación") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedViajeForDialog?.let {
                            viewModel.cambiarEstadoAprobacion(it.uuid, "OBSERVADO")
                        }
                        selectedViajeForDialog = null
                        observationText = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Guardar Observación")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedViajeForDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun GerenciaDashboard() {
    val haptic = LocalHapticFeedback.current
    var showExportSnackbar by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clearFocusOnTap()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Gerencia & Control Financiero",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Análisis mensual de costos de traslados médicos",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Metrics Grid
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricSummaryCard(
                title = "Gasto Total Mes",
                value = FormatUtils.formatCurrency(428500.0),
                subtitle = "Peajes + Estimación Combustible",
                icon = Icons.Outlined.AttachMoney,
                accentColor = InfoBlue
            )
            MetricSummaryCard(
                title = "Costo Promedio / Viaje",
                value = FormatUtils.formatCurrency(14280.0),
                subtitle = "30 viajes realizados",
                icon = Icons.Outlined.Analytics,
                accentColor = SuccessGreen
            )
            MetricSummaryCard(
                title = "Rendimiento Flota",
                value = "12,4 Km/L",
                subtitle = "4 vehículos en operación",
                icon = Icons.Outlined.DirectionsCar,
                accentColor = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        ElevatedCard(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Reportes & Exportación",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Generar archivo CSV granular para auditoría contable y reembolso de peajes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showExportSnackbar = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Exportar Reporte Mensual (CSV)", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (showExportSnackbar) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                color = SuccessGreen.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reporte exportado exitosamente a Descargas/TeqRuta_Mayo_2025.csv", style = MaterialTheme.typography.bodySmall, color = SuccessGreen)
                }
            }
        }
    }
}

@Composable
fun AdminDashboard() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clearFocusOnTap()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Administración del Sistema",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Parámetros globales, vehículos y clínicas",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Flota Vehicular Registrada",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        listOf(
            Triple("HK-8932", "Toyota Hilux 4x4", "11,5 km/L"),
            Triple("JP-4021", "Nissan Navara", "12,0 km/L"),
            Triple("CB-1290", "Chevrolet Colorado", "10,8 km/L")
        ).forEach { (patente, modelo, consumo) ->
            ElevatedCard(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(patente, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(modelo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    StatusChip(consumo, Icons.Outlined.LocalGasStation, InfoBlue)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Destinos Médicos Frecuentes",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        listOf(
            "Clínica RedSalud Rancagua" to "Distancia: 87 km",
            "Hospital Clínico Chillán" to "Distancia: 400 km",
            "Centro Médico Viña del Mar" to "Distancia: 120 km"
        ).forEach { (clinica, dist) ->
            ElevatedCard(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalHospital, contentDescription = null, tint = ErrorRed)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(clinica, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(dist, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
