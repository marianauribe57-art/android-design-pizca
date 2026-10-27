package com.adso.pizca.presentation.screens

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.adso.pizca.presentation.components.PantallaEnConstruccion

// Responsable: Ismael
@Composable
fun BusquedaScreen(onRecetaClick: (String) -> Unit) {
    PantallaEnConstruccion(titulo = "Búsqueda", responsable = "Ismael") {
        Button(onClick = { onRecetaClick("2") }) { Text("Ver receta de ejemplo") }
    }
}