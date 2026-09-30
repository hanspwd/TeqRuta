package com.teqmed.teqruta.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.teqmed.teqruta.viewmodel.TripState
import com.teqmed.teqruta.viewmodel.ViajeViewModel
import java.util.UUID
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState

@Composable
fun ActiveTripScreen(viewModel: ViajeViewModel) {
    val tripState by viewModel.tripState.collectAsState()
    val rutaSeleccionada by viewModel.rutaSeleccionada.collectAsState()

    when (tripState) {
        TripState.SETUP -> InitialTripScreen(viewModel, rutaSeleccionada)
        TripState.IN_PROGRESS -> InProgressScreen(viewModel, rutaSeleccionada)
        TripState.FINISHED -> FinishedScreen(viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InitialTripScreen(viewModel: ViajeViewModel, rutaActual: String) {
    val odoInicio by viewModel.odometroInicio.collectAsState()
    val fotoInicio by viewModel.fotoInicio.collectAsState()
    
    val rutas = listOf("Santiago - Rancagua", "Chillán - Los Ángeles", "Valparaíso - Viña del Mar")
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        Text("Iniciar Nuevo Viaje", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(16.dp))
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = rutaActual,
                onValueChange = {},
                readOnly = true,
                label = { Text("Seleccionar Destino") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier.menuAnchor().fillMaxWidth()
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
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Odometría Inicial", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = odoInicio,
                    onValueChange = viewModel::onOdometroInicioChange,
                    label = { Text("Valor Odómetro (Km)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = { viewModel.onFotoInicioTomada("foto_inicio_${UUID.randomUUID()}.jpg") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = if (fotoInicio != null) com.teqmed.teqruta.ui.theme.SuccessGreen else MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(if (fotoInicio != null) Icons.Filled.Check else Icons.Filled.CameraAlt, contentDescription = "Cámara")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (fotoInicio != null) "Foto Capturada" else "Tomar Foto Odómetro")
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { viewModel.iniciarViaje() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = odoInicio.isNotEmpty() && fotoInicio != null,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = "Iniciar")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Iniciar Viaje", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun InProgressScreen(viewModel: ViajeViewModel, ruta: String) {
    val odoFin by viewModel.odometroFin.collectAsState()
    val fotoFin by viewModel.fotoFin.collectAsState()
    val peajes by viewModel.peajes.collectAsState()
    
    var montoPeajeInput by remember { mutableStateOf("") }
    var eventoInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp).fillMaxSize().verticalScroll(rememberScrollState())) {
        Card(
            modifier = Modifier.fillMaxWidth(), 
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.DirectionsCar, contentDescription = "Carro", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Viaje en Curso", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Ruta actual: \$ruta", color = MaterialTheme.colorScheme.onSurface)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Registro de Peajes", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = montoPeajeInput,
                        onValueChange = { montoPeajeInput = it },
                        label = { Text("Monto ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledIconButton(
                        onClick = { 
                            val monto = montoPeajeInput.toDoubleOrNull()
                            if (monto != null) {
                                viewModel.agregarPeaje(monto, "foto_peaje_\${UUID.randomUUID()}.jpg")
                                montoPeajeInput = ""
                            }
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = "Capturar", tint = Color.White)
                    }
                }
                if (peajes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    peajes.forEach { peaje ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                            Icon(Icons.Filled.Receipt, contentDescription = "Peaje", tint = com.teqmed.teqruta.ui.theme.SuccessGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Peaje registrado: $\${peaje.monto}", color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Registro de Eventos (Stops, Bencina, etc)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = eventoInput,
                        onValueChange = { eventoInput = it },
                        label = { Text("Describir evento") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledIconButton(
                        onClick = { 
                            if (eventoInput.isNotBlank()) {
                                viewModel.agregarEvento(eventoInput)
                                eventoInput = ""
                            }
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Añadir", tint = Color.White)
                    }
                }
                val eventos by viewModel.eventos.collectAsState()
                if (eventos.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    eventos.forEach { evt ->
                        val parts = evt.split("|", limit = 2)
                        val text = if(parts.size == 2) parts[1] else evt
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                            Icon(Icons.Filled.Info, contentDescription = "Evento", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text, color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Finalizar Viaje", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = odoFin,
                    onValueChange = viewModel::onOdometroFinChange,
                    label = { Text("Odómetro Final (Km)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = { viewModel.onFotoFinTomada("foto_fin_\${UUID.randomUUID()}.jpg") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = if (fotoFin != null) com.teqmed.teqruta.ui.theme.SuccessGreen else MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(if (fotoFin != null) Icons.Filled.Check else Icons.Filled.CameraAlt, contentDescription = "Cámara")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (fotoFin != null) "Foto Capturada" else "Tomar Foto Odómetro")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.terminarViaje() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = odoFin.isNotEmpty() && fotoFin != null,
            colors = ButtonDefaults.buttonColors(containerColor = com.teqmed.teqruta.ui.theme.ErrorRed)
        ) {
            Icon(Icons.Filled.Stop, contentDescription = "Terminar", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Terminar Viaje", style = MaterialTheme.typography.titleMedium, color = Color.White)
        }
        
        Spacer(modifier = Modifier.height(16.dp)) // Extra padding at bottom
    }
}

@Composable
fun FinishedScreen(viewModel: ViajeViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Filled.CheckCircle, contentDescription = "Listo", tint = com.teqmed.teqruta.ui.theme.SuccessGreen, modifier = Modifier.size(120.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text("¡Viaje finalizado con éxito!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(8.dp))
        Text("El viaje ha sido guardado localmente y quedará en espera de sincronización automática al detectar red.", 
             color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
             textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = { viewModel.reset() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Iniciar Nuevo Viaje")
        }
    }
}
