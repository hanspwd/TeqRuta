package com.teqmed.teqruta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.teqmed.teqruta.ui.theme.TeqRutaTheme
import com.teqmed.teqruta.viewmodel.ViajeViewModel

class MainActivity : ComponentActivity() {
    
    private val viajeViewModel: ViajeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TeqRutaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Padding is applied internally by the screen if needed, 
                    // or wrapped around the screen
                    androidx.compose.foundation.layout.Box(modifier = Modifier.padding(innerPadding)) {
                        com.teqmed.teqruta.ui.screens.AppNavigation(viewModel = viajeViewModel)
                    }
                }
            }
        }
    }
}