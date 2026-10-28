package com.teqmed.teqruta.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.teqmed.teqruta.ui.components.CapturedImagePreview
import com.teqmed.teqruta.ui.components.PulsatingStatusBadge
import com.teqmed.teqruta.ui.components.ValidableOutlinedTextField
import com.teqmed.teqruta.ui.components.clearFocusOnTap
import com.teqmed.teqruta.ui.theme.ErrorRed
import com.teqmed.teqruta.ui.theme.SuccessGreen
import com.teqmed.teqruta.utils.FormatUtils
import com.teqmed.teqruta.viewmodel.TripState
import com.teqmed.teqruta.viewmodel.ViajeViewModel

@Composable
fun ActiveTripScreen(viewModel: ViajeViewModel) {
    val tripState by viewModel.tripState.collectAsState()

    AnimatedContent(
        targetState = tripState,
        transitionSpec = {
            fadeIn() + slideInHorizontally() togetherWith fadeOut() + slideOutHorizontally()
        },
        label = "trip_state_transition"
    ) { state ->
        when (state) {
            TripState.SETUP -> InitialTripScreen(viewModel)
            TripState.IN_PROGRESS -> InProgressScreen(viewModel)
            TripState.FINISHED -> FinishedScreen(viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InitialTripScreen(viewModel: ViajeViewModel) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val rutaActual by viewModel.rutaSeleccionada.collectAsState()
    val odoInicio by viewModel.odometroInicio.collectAsState()
    val odoInicioError by viewModel.odometroInicioError.collectAsState()
    val fotoInicioBitmap by viewModel.fotoInicioBitmap.collectAsState()

    val rutas = listOf(
        "Santiago - Rancagua (Clínica RedSalud)",
        "Chillán - Los Ángeles (Hospital Clínico)",
        "Valparaíso - Viña del Mar (Centro Médico)",
        "Concepción - Talcahuano (Clínica Biobío)"
    )
    var expanded by remember { mutableStateOf(false) }
    var isTouched by remember { mutableStateOf(false) }

    // Native Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.onFotoInicioTomada(bitmap)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clearFocusOnTap()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Section Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Icon(
                Icons.Outlined.DirectionsCar,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Iniciar Nuevo Viaje",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Verificación inicial del vehículo e inicio de ruta",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Route Selector Dropdown
        ElevatedCard(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "1. Destino Asignado",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = rutaActual,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Ruta Médica") },
                        leadingIcon = { Icon(Icons.Outlined.LocationOn, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        rutas.forEach { ruta ->
                            DropdownMenuItem(
                                text = { Text(ruta) },
                                onClick = {
                                    viewModel.onRutaChange(ruta)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Initial Odometer Card
        ElevatedCard(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "2. Odometría & Evidencia Fotográfica",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                ValidableOutlinedTextField(
                    value = odoInicio,
                    onValueChange = {
                        isTouched = true
                        viewModel.onOdometroInicioChange(it)
                    },
                    label = "Odómetro Inicial (Km)",
                    leadingIcon = Icons.Outlined.Speed,
                    errorMessage = odoInicioError,
                    isTouched = isTouched,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Native Camera Launcher UI Component
                CapturedImagePreview(
                    bitmap = fotoInicioBitmap,
                    onTakeClick = { cameraLauncher.launch(null) },
                    label = if (fotoInicioBitmap != null) "Foto Odómetro Capturada" else "Tomar Foto Odómetro (Cámara Nativa)"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Start Trip Button
        val canStart = odoInicio.isNotEmpty() && odoInicioError == null && fotoInicioBitmap != null

        Button(
            onClick = {
                isTouched = true
                if (canStart) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.iniciarViaje(context)
                }
            },
            enabled = canStart,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Iniciar Viaje (Capturar GPS)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun InProgressScreen(viewModel: ViajeViewModel) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val ruta by viewModel.rutaSeleccionada.collectAsState()
    val odoInicio by viewModel.odometroInicio.collectAsState()
    val locationInicio by viewModel.locationInicio.collectAsState()

    val peajes by viewModel.peajes.collectAsState()
    val montoPeajeInput by viewModel.montoPeajeInput.collectAsState()
    val montoPeajeError by viewModel.montoPeajeError.collectAsState()
    val fotoPeajeActual by viewModel.fotoPeajeActual.collectAsState()

    val eventos by viewModel.eventos.collectAsState()
    val eventoInput by viewModel.eventoInput.collectAsState()

    val odoFin by viewModel.odometroFin.collectAsState()
    val odoFinError by viewModel.odometroFinError.collectAsState()
    val fotoFinBitmap by viewModel.fotoFinBitmap.collectAsState()

    var isPeajeTouched by remember { mutableStateOf(false) }
    var isFinTouched by remember { mutableStateOf(false) }

    // Camera Launchers for Toll receipt and Final Odometer
    val peajeCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.onFotoPeajeTomada(bitmap)
        }
    }

    val finCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.onFotoFinTomada(bitmap)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clearFocusOnTap()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Active Status Pulsating Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Viaje en Progreso",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            PulsatingStatusBadge(
                text = "EN CURSO",
                badgeColor = SuccessGreen
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Trip Info Summary Card
        ElevatedCard(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(ruta, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
                Spacer(modifier = Modifier.height(4.dp))
                val odoInt = odoInicio.toIntOrNull() ?: 0
                Text("Odómetro Inicio: ${FormatUtils.formatNumber(odoInt)} km", style = MaterialTheme.typography.bodyMedium)
                if (locationInicio != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        FormatUtils.formatGps(locationInicio?.latitude, locationInicio?.longitude),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Peajes Section
        ElevatedCard(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Registro de Peajes / Boletas",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                ValidableOutlinedTextField(
                    value = montoPeajeInput,
                    onValueChange = {
                        isPeajeTouched = true
                        viewModel.onMontoPeajeChange(it)
                    },
                    label = "Monto Peaje ($ CLP)",
                    leadingIcon = Icons.Filled.AttachMoney,
                    errorMessage = montoPeajeError,
                    isTouched = isPeajeTouched,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(12.dp))

                CapturedImagePreview(
                    bitmap = fotoPeajeActual,
                    onTakeClick = { peajeCameraLauncher.launch(null) },
                    label = if (fotoPeajeActual != null) "Comprobante Capturado" else "Tomar Foto Boleta (Cámara)"
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        isPeajeTouched = true
                        viewModel.agregarPeaje()
                        isPeajeTouched = false
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Agregar Peaje a la Lista")
                }

                // Peajes items list with smooth animation
                if (peajes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Peajes Registrados (${peajes.size}):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    peajes.forEach { peaje ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + expandVertically()
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Receipt, contentDescription = null, tint = SuccessGreen)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(FormatUtils.formatCurrency(peaje.monto), fontWeight = FontWeight.Bold)
                                            Text("Evidencia: ${if (peaje.bitmap != null) "Foto adjunta" else "Sin foto"}", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                    IconButton(onClick = { viewModel.eliminarPeaje(peaje.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = ErrorRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Events Section
        ElevatedCard(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Registro de Eventos / Bitácora",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = eventoInput,
                        onValueChange = viewModel::onEventoInputChange,
                        label = { Text("Detalle evento (ej. Detención, Tráfico)") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { viewModel.agregarEvento() },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Añadir", tint = Color.White)
                    }
                }

                if (eventos.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    eventos.forEach { evt ->
                        val text = evt.substringAfter("|", evt)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Finalize Trip Section
        ElevatedCard(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Finalización del Viaje",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                ValidableOutlinedTextField(
                    value = odoFin,
                    onValueChange = {
                        isFinTouched = true
                        viewModel.onOdometroFinChange(it)
                    },
                    label = "Odómetro Final (Km)",
                    leadingIcon = Icons.Outlined.Speed,
                    errorMessage = odoFinError,
                    isTouched = isFinTouched,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(16.dp))

                CapturedImagePreview(
                    bitmap = fotoFinBitmap,
                    onTakeClick = { finCameraLauncher.launch(null) },
                    label = if (fotoFinBitmap != null) "Foto Final Capturada" else "Foto Odómetro Final (Cámara)"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Finish Trip Action Button
        val canFinish = odoFin.isNotEmpty() && odoFinError == null && fotoFinBitmap != null

        Button(
            onClick = {
                isFinTouched = true
                if (canFinish) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.terminarViaje(context)
                }
            },
            enabled = canFinish,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(Icons.Filled.Stop, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Terminar Viaje & Guardar (Room DB)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun FinishedScreen(viewModel: ViajeViewModel) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = "Éxito",
            tint = SuccessGreen,
            modifier = Modifier.size(110.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "¡Viaje Finalizado & Guardado!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "El registro ha sido almacenado localmente en Room DB y se encuentra listo para sincronización con la central.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.reset()
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Iniciar Otro Viaje", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}
