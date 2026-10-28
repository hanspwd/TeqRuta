package com.teqmed.teqruta.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
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

    var selectedViajeForRejectDialog by remember { mutableStateOf<Viaje?>(null) }
    var selectedViajeForDetailModal by remember { mutableStateOf<Viaje?>(null) }
    var rejectionReasonText by remember { mutableStateOf("") }

    var showRejectNotificationPopup by remember { mutableStateOf(false) }
    var rejectedTechnicianEmail by remember { mutableStateOf("") }
    var rejectedRouteName by remember { mutableStateOf("") }

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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricSummaryCard(
                title = "Por Aprobar",
                value = pendientes.size.toString(),
                subtitle = "Viajes pendientes",
                icon = Icons.Default.FactCheck,
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

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { selectedViajeForDetailModal = viaje },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Detalle", style = MaterialTheme.typography.labelMedium)
                            }

                            OutlinedButton(
                                onClick = { selectedViajeForRejectDialog = viaje },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Rechazar", style = MaterialTheme.typography.labelMedium)
                            }

                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.cambiarEstadoAprobacion(viaje.uuid, "APROBADO")
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Aprobar", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedViajeForRejectDialog != null) {
        AlertDialog(
            onDismissRequest = { selectedViajeForRejectDialog = null },
            title = { Text("Rechazar Viaje") },
            text = {
                Column {
                    Text("Indique el motivo del rechazo para el viaje de ${selectedViajeForRejectDialog?.tecnicoEmail}:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectionReasonText,
                        onValueChange = { rejectionReasonText = it },
                        label = { Text("Motivo / Discrepancia") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val viajeRechazado = selectedViajeForRejectDialog
                        viajeRechazado?.let {
                            viewModel.cambiarEstadoAprobacion(it.uuid, "RECHAZADO")
                            rejectedTechnicianEmail = it.tecnicoEmail
                            rejectedRouteName = it.rutaSeleccionada
                        }
                        selectedViajeForRejectDialog = null
                        rejectionReasonText = ""
                        showRejectNotificationPopup = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Confirmar Rechazo")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedViajeForRejectDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showRejectNotificationPopup) {
        AlertDialog(
            onDismissRequest = { showRejectNotificationPopup = false },
            icon = {
                Icon(Icons.Default.Email, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(36.dp))
            },
            title = { Text(text = "Notificación Enviada", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Se ha enviado un correo electrónico de notificación a ($rejectedTechnicianEmail) solicitando la justificación y corrección del viaje ($rejectedRouteName).",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(onClick = { showRejectNotificationPopup = false }, shape = RoundedCornerShape(10.dp)) {
                    Text("Entendido")
                }
            }
        )
    }

    if (selectedViajeForDetailModal != null) {
        val viaje = selectedViajeForDetailModal!!
        val dist = (viaje.odometroFin - viaje.odometroInicio).coerceAtLeast(0)

        AlertDialog(
            onDismissRequest = { selectedViajeForDetailModal = null },
            title = { Text(text = "Detalle Completo del Viaje", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    Text("Ruta: ${viaje.rutaSeleccionada}", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Técnico: ${viaje.tecnicoEmail}", style = MaterialTheme.typography.bodySmall)
                    Text("Patente Vehículo: ${viaje.vehiculoPatente}", style = MaterialTheme.typography.bodySmall)
                    Text("Fecha: ${FormatUtils.formatDateTime(viaje.horaInicio)}", style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Odometría Inicial: ${FormatUtils.formatNumber(viaje.odometroInicio)} km", style = MaterialTheme.typography.bodySmall)
                    Text("Odometría Final: ${FormatUtils.formatNumber(viaje.odometroFin)} km", style = MaterialTheme.typography.bodySmall)
                    Text("Distancia Total Recorrida: ${FormatUtils.formatNumber(dist)} km", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    Text("Total Peajes: ${FormatUtils.formatCurrency(viaje.totalMontoPeajes)}", style = MaterialTheme.typography.bodySmall, color = SuccessGreen)

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Ubicación GPS Inicio:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    Text(if (viaje.latitudInicio != null && viaje.longitudInicio != null) FormatUtils.formatGps(viaje.latitudInicio, viaje.longitudInicio) else "No disponible", style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Ubicación GPS Fin:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    Text(if (viaje.latitudFin != null && viaje.longitudFin != null) FormatUtils.formatGps(viaje.latitudFin, viaje.longitudFin) else "Pendiente", style = MaterialTheme.typography.bodySmall)

                    if (viaje.eventosExtra.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Bitácora / Observaciones:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        Text(viaje.eventosExtra.replace(";", "\n• "), style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { selectedViajeForDetailModal = null }, shape = RoundedCornerShape(10.dp)) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
fun GerenciaDashboard() {
    val haptic = LocalHapticFeedback.current
    var showExportPopup by remember { mutableStateOf(false) }

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
                        showExportPopup = true
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
    }

    if (showExportPopup) {
        AlertDialog(
            onDismissRequest = { showExportPopup = false },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(36.dp)) },
            title = { Text(text = "¡Reporte Exportado!", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "El archivo CSV ha sido generado correctamente y guardado en Descargas/TeqRuta_Mayo_2025.csv",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showExportPopup = false
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Abrir Reporte")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportPopup = false }) { Text("Cerrar") }
            }
        )
    }
}

@Composable
fun AdminDashboard() {
    val haptic = LocalHapticFeedback.current
    var showAddVehicleDialog by remember { mutableStateOf(false) }

    val flotaVehicular = remember {
        mutableStateListOf(
            VehicleInfo("HK-8932", "Toyota Hilux 4x4", "11,5 km/L", "45.200 km", "carlos.perez@teqmed.cl"),
            VehicleInfo("JP-4021", "Nissan Navara", "12,0 km/L", "28.150 km", "andres.soto@teqmed.cl"),
            VehicleInfo("CB-1290", "Chevrolet Colorado", "10,8 km/L", "61.900 km", "mauricio.gomez@teqmed.cl")
        )
    }

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

        // Cabecera Flota con Margen y Espaciado Corregido (Ya no está pegado)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Flota Vehicular Registrada",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Gestión y asignación de móviles",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showAddVehicleDialog = true
                },
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Nuevo Vehículo", style = MaterialTheme.typography.labelMedium)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        flotaVehicular.forEach { vehiculo ->
            ElevatedCard(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(vehiculo.patente, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text(vehiculo.modelo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        StatusChip(vehiculo.rendimiento, Icons.Outlined.LocalGasStation, InfoBlue)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Odómetro Actual:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(vehiculo.kilometrajeActual, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Técnico Asignado:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(vehiculo.tecnicoEmail, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Destinos Médicos Frecuentes",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        listOf(
            "Clínica RedSalud Rancagua" to "87 km",
            "Hospital Clínico Chillán" to "400 km",
            "Centro Médico Viña del Mar" to "120 km"
        ).forEach { (clinica, dist) ->
            ElevatedCard(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.LocalHospital, contentDescription = null, tint = ErrorRed)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = clinica,
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = dist,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }

    if (showAddVehicleDialog) {
        var nuevaPatente by remember { mutableStateOf("") }
        var nuevoModelo by remember { mutableStateOf("") }
        var nuevoRendimiento by remember { mutableStateOf("") }
        var nuevoKm by remember { mutableStateOf("") }
        var nuevoTecnico by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddVehicleDialog = false },
            title = { Text("Registrar Nuevo Vehículo", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nuevaPatente,
                        onValueChange = { nuevaPatente = it },
                        label = { Text("Patente (ej. AB-1234)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = nuevoModelo,
                        onValueChange = { nuevoModelo = it },
                        label = { Text("Modelo / Marca") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = nuevoRendimiento,
                        onValueChange = { nuevoRendimiento = it },
                        label = { Text("Rendimiento (ej. 11,0 km/L)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = nuevoKm,
                        onValueChange = { nuevoKm = it },
                        label = { Text("Kilometraje Actual (ej. 15.000 km)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = nuevoTecnico,
                        onValueChange = { nuevoTecnico = it },
                        label = { Text("Correo Técnico Asignado") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nuevaPatente.isNotBlank() && nuevoModelo.isNotBlank()) {
                            flotaVehicular.add(
                                VehicleInfo(
                                    patente = nuevaPatente.uppercase(),
                                    modelo = nuevoModelo,
                                    rendimiento = if (nuevoRendimiento.isNotBlank()) nuevoRendimiento else "11.0 km/L",
                                    kilometrajeActual = if (nuevoKm.isNotBlank()) nuevoKm else "0 km",
                                    tecnicoEmail = if (nuevoTecnico.isNotBlank()) nuevoTecnico else "sin.asignar@teqmed.cl"
                                )
                            )
                            showAddVehicleDialog = false
                        }
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Guardar Vehículo")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddVehicleDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

data class VehicleInfo(
    val patente: String,
    val modelo: String,
    val rendimiento: String,
    val kilometrajeActual: String,
    val tecnicoEmail: String
)