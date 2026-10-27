package com.adso.pizca.presentation.screens

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.adso.pizca.presentation.components.PantallaEnConstruccion

// Responsable: Valeria
@Composable
fun InicioScreen(onRecetaClick: (String) -> Unit) {
    PantallaEnConstruccion(titulo = "Inicio", responsable = "Valeria") {
        Button(onClick = { onRecetaClick("1") }) { Text("Ver receta de ejemplo") }
    }
}
