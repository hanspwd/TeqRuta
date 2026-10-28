package com.teqmed.teqruta.ui.screens

import android.Manifest
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.onFotoInicioTomada(bitmap)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clearFocusOnTap()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Icon(
                Icons.Outlined.DirectionsCar,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Iniciar Nuevo Viaje",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Seleccione ruta y registre odometría inicial",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // --- SECCIÓN 1: DESTINO ASIGNADO ---
        ElevatedCard(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "1. Destino Asignado",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = rutaActual,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Seleccione Ruta Médica") },
                        leadingIcon = { Icon(Icons.Outlined.LocationOn, contentDescription = null, modifier = Modifier.size(20.dp)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        rutas.forEach { ruta ->
                            DropdownMenuItem(
                                text = { Text(ruta, style = MaterialTheme.typography.bodyMedium) },
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

        // --- SECCIÓN 2: ODOMETRÍA & EVIDENCIA FOTOGRÁFICA (Con vista previa compacta y botón de retoma) ---
        ElevatedCard(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "2. Odometría & Evidencia Inicial",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

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

                Spacer(modifier = Modifier.height(10.dp))

                // Vista previa compacta controlada con opción de recargar/retomar foto si quedó mal
                Box(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                    CapturedImagePreview(
                        bitmap = fotoInicioBitmap,
                        onTakeClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        label = if (fotoInicioBitmap != null) "Foto Odómetro Capturada (Tocar para repetir)" else "Tomar Foto Odómetro"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

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
                .height(52.dp)
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Iniciar Viaje", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InProgressScreen(viewModel: ViajeViewModel) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val peajes by viewModel.peajes.collectAsState()
    val montoPeajeInput by viewModel.montoPeajeInput.collectAsState()
    val montoPeajeError by viewModel.montoPeajeError.collectAsState()
    val fotoPeajeActual by viewModel.fotoPeajeActual.collectAsState()

    val eventos by viewModel.eventos.collectAsState()
    val eventoInput by viewModel.eventoInput.collectAsState()
    val fotoBitacoraActual by viewModel.fotoPeajeActual.collectAsState()

    val odoFin by viewModel.odometroFin.collectAsState()
    val odoFinError by viewModel.odometroFinError.collectAsState()
    val fotoFinBitmap by viewModel.fotoFinBitmap.collectAsState()

    var isPeajeTouched by remember { mutableStateOf(false) }
    var isFinTouched by remember { mutableStateOf(false) }
    var subStep by remember { mutableStateOf(0) }

    var showAddPeaje by remember { mutableStateOf(false) }
    var showAddEvento by remember { mutableStateOf(false) }

    val peajeCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.onFotoPeajeTomada(bitmap)
        }
    }

    val peajePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            peajeCameraLauncher.launch(null)
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

    val finPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            finCameraLauncher.launch(null)
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Viaje en Progreso",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (subStep == 0) "Fase 1: Registro en ruta" else "Fase 2: Finalización y GPS Final",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            PulsatingStatusBadge(
                text = "EN CURSO",
                badgeColor = SuccessGreen
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            SegmentedButton(
                selected = subStep == 0,
                onClick = { subStep = 0 },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Peajes y Bitácora")
                }
            }
            SegmentedButton(
                selected = subStep == 1,
                onClick = { subStep = 1 },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Finalizar Viaje")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (subStep == 0) {
            // --- PEAJES ---
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Peajes",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!showAddPeaje) {
                            TextButton(onClick = { showAddPeaje = true }) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Agregar peaje", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = showAddPeaje,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            ValidableOutlinedTextField(
                                value = montoPeajeInput,
                                onValueChange = {
                                    isPeajeTouched = true
                                    viewModel.onMontoPeajeChange(it)
                                },
                                label = "Monto ($ CLP)",
                                leadingIcon = Icons.Filled.AttachMoney,
                                errorMessage = montoPeajeError,
                                isTouched = isPeajeTouched,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Vista previa compacta con opción de recargar / volver a tomar la foto si quedó mal
                            Box(modifier = Modifier.fillMaxWidth().height(110.dp)) {
                                CapturedImagePreview(
                                    bitmap = fotoPeajeActual,
                                    onTakeClick = { peajePermissionLauncher.launch(Manifest.permission.CAMERA) },
                                    label = if (fotoPeajeActual != null) "Comprobante Capturado (Tocar para repetir)" else "Tomar Foto Boleta"
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        showAddPeaje = false
                                        viewModel.onMontoPeajeChange("")
                                    },
                                    modifier = Modifier.weight(1f).height(40.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Cancelar", style = MaterialTheme.typography.labelMedium)
                                }

                                val canSavePeaje = montoPeajeInput.isNotEmpty() && montoPeajeError == null && fotoPeajeActual != null
                                Button(
                                    onClick = {
                                        isPeajeTouched = true
                                        if (canSavePeaje) {
                                            viewModel.agregarPeaje()
                                            isPeajeTouched = false
                                            showAddPeaje = false
                                        }
                                    },
                                    enabled = canSavePeaje,
                                    modifier = Modifier.weight(1f).height(40.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Guardar", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }

                    if (peajes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                peajes.forEach { peaje ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(SuccessGreen, shape = CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${FormatUtils.formatCurrency(peaje.monto)} • Con foto",
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- BITÁCORA ---
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Bitácora de Eventos",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!showAddEvento) {
                            TextButton(onClick = { showAddEvento = true }) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Agregar evento", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = showAddEvento,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            OutlinedTextField(
                                value = eventoInput,
                                onValueChange = viewModel::onEventoInputChange,
                                label = { Text("Detalle evento") },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Vista previa compacta con opción de recargar foto
                            Box(modifier = Modifier.fillMaxWidth().height(110.dp)) {
                                CapturedImagePreview(
                                    bitmap = fotoBitacoraActual,
                                    onTakeClick = { peajePermissionLauncher.launch(Manifest.permission.CAMERA) },
                                    label = if (fotoBitacoraActual != null) "Foto Evidencia Capturada (Tocar para repetir)" else "Tomar Foto Evidencia"
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        showAddEvento = false
                                        viewModel.onEventoInputChange("")
                                    },
                                    modifier = Modifier.weight(1f).height(40.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Cancelar", style = MaterialTheme.typography.labelMedium)
                                }

                                val canSaveEvento = eventoInput.isNotEmpty() && fotoBitacoraActual != null
                                Button(
                                    onClick = {
                                        if (canSaveEvento) {
                                            viewModel.agregarEvento()
                                            showAddEvento = false
                                        }
                                    },
                                    enabled = canSaveEvento,
                                    modifier = Modifier.weight(1f).height(40.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Guardar", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }

                    if (eventos.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                eventos.forEach { evt ->
                                    val text = evt.substringAfter("|", evt)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { subStep = 1 },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Ir a Finalizar Viaje")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }

        } else {
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Finalización y GPS Final",
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

                    Box(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                        CapturedImagePreview(
                            bitmap = fotoFinBitmap,
                            onTakeClick = { finPermissionLauncher.launch(Manifest.permission.CAMERA) },
                            label = if (fotoFinBitmap != null) "Foto Final Capturada (Tocar para repetir)" else "Foto Odómetro Final"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

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
                Text("Finalizar y Guardar Viaje", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun FinishedScreen(viewModel: ViajeViewModel) {
    val haptic = LocalHapticFeedback.current
    val ultimaSincronizacion by remember { mutableStateOf("Hace un momento") }

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
            text = "¡Viaje Finalizado!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Registro correctamente guardado y sincronizado.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CloudSync,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Sincronización automática activa",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Última vez: $ultimaSincronizacion",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

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