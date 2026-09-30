package com.teqmed.teqruta.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.teqmed.teqruta.viewmodel.ViajeViewModel

@Composable
fun ViajeScreen(viewModel: ViajeViewModel) {
    val odometroInicio by viewModel.odometroInicio.collectAsState()
    val odometroFin by viewModel.odometroFin.collectAsState()
    val pagoPeaje by viewModel.pagoPeaje.collectAsState()
    val montoPeaje by viewModel.montoPeaje.collectAsState()

    Column(
        modifier = Modifier.padding(16.dp).fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Registro de Viaje", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = odometroInicio,
            onValueChange = viewModel::onOdometroInicioChange,
            label = { Text("Odómetro Inicio (km)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = odometroFin,
            onValueChange = viewModel::onOdometroFinChange,
            label = { Text("Odómetro Fin (km)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Checkbox(
                checked = pagoPeaje,
                onCheckedChange = viewModel::onPagoPeajeChange
            )
            Text("Pago de Peaje")
        }
        
        if (pagoPeaje) {
            OutlinedTextField(
                value = montoPeaje,
                onValueChange = viewModel::onMontoPeajeChange,
                label = { Text("Monto de Peaje") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = { /* Invocar cámara */ }, modifier = Modifier.fillMaxWidth()) {
                Text("Capturar Comprobante")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { viewModel.finalizarViaje(clinicaId = 1) },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Finalizar Viaje")
        }
    }
}
