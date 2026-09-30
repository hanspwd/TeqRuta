package com.teqmed.teqruta.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.compose.*
import com.teqmed.teqruta.viewmodel.ViajeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(viewModel: ViajeViewModel) {
    val navController = rememberNavController()
    
    // Track current route for App Bar logic
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "login"

    Scaffold(
        topBar = {
            if (currentRoute != "login") {
                CenterAlignedTopAppBar(
                    title = { 
                        Text("TeqRuta", fontWeight = FontWeight.Bold, color = Color.White)
                    },
                    navigationIcon = {
                        val isRootScreen = currentRoute?.endsWith("_dashboard") == true
                        if (currentRoute != "login" && !isRootScreen) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                            }
                        } else if (currentRoute != "login" && isRootScreen) {
                            IconButton(onClick = { }, enabled = false) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = Color.Transparent)
                            }
                        }
                    },
                    actions = {
                        if (currentRoute != "login") {
                            IconButton(onClick = { 
                                SessionManager.currentRole = ""
                                navController.navigate("login") { 
                                    popUpTo(0) { inclusive = true } 
                                } 
                            }) {
                                Icon(Icons.Filled.ExitToApp, contentDescription = "Cerrar Sesión", tint = Color.White)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            NavHost(navController = navController, startDestination = "login") {
                composable("login") { LoginScreen(navController) }
                composable("tecnico_dashboard") { TecnicoDashboard(viewModel) }
                composable("jefe_dashboard") { JefeDashboard() }
                composable("gerencia_dashboard") { GerenciaDashboard() }
                composable("admin_dashboard") { AdminDashboard() }
            }
        }
    }
}
