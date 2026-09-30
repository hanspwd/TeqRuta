package com.teqmed.teqruta.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.teqmed.teqruta.viewmodel.ViajeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardMenuCard(title: String, description: String, icon: ImageVector, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TecnicoDashboard(viewModel: ViajeViewModel) {
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf("Viaje Actual", "Historial")
    
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            if (selectedItem == 0) {
                ActiveTripScreen(viewModel)
            } else {
                HistoryScreen(viewModel)
            }
        }
        NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
            NavigationBarItem(
                icon = { Icon(Icons.Filled.DirectionsCar, contentDescription = "Viaje Actual") },
                label = { Text(items[0]) },
                selected = selectedItem == 0,
                onClick = { selectedItem = 0 },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            )
            NavigationBarItem(
                icon = { Icon(Icons.Filled.List, contentDescription = "Historial") },
                label = { Text(items[1]) },
                selected = selectedItem == 1,
                onClick = { selectedItem = 1 },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            )
        }
    }
}

@Composable
fun JefeDashboard() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Panel de Jefatura", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Gestión de validaciones de terreno", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(24.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item { DashboardMenuCard("Validaciones", "Viajes pendientes de aprobar", Icons.Filled.FactCheck, {}) }
            item { DashboardMenuCard("Alertas", "Desviaciones de kilometraje (>10%)", Icons.Filled.Warning, {}) }
            item { DashboardMenuCard("Observados", "Viajes con observaciones", Icons.Filled.Visibility, {}) }
            item { DashboardMenuCard("Historial", "Registro de aprobaciones", Icons.Filled.History, {}) }
        }
    }
}

@Composable
fun GerenciaDashboard() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Gerencia y Finanzas", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Análisis de costos y rentabilidad", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(24.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item { DashboardMenuCard("Costos Mensuales", "Gasto total en movilización", Icons.Filled.AttachMoney, {}) }
            item { DashboardMenuCard("Rentabilidad", "Análisis por contrato médico", Icons.Filled.Analytics, {}) }
            item { DashboardMenuCard("Exportar Datos", "Generar reportes CSV granulares", Icons.Filled.Download, {}) }
            item { DashboardMenuCard("Métricas", "Rendimiento general de flota", Icons.Filled.BarChart, {}) }
        }
    }
}

@Composable
fun AdminDashboard() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Administración", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Configuración y parámetros del sistema", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(24.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item { DashboardMenuCard("Flota Vehicular", "Mantenedor de patentes y km/L", Icons.Filled.DirectionsCar, {}) }
            item { DashboardMenuCard("Clínicas", "Gestión de destinos y distancias", Icons.Filled.LocalHospital, {}) }
            item { DashboardMenuCard("Usuarios", "Gestión de roles y accesos", Icons.Filled.People, {}) }
            item { DashboardMenuCard("Combustible", "Actualización tarifa diaria", Icons.Filled.LocalGasStation, {}) }
        }
    }
}
