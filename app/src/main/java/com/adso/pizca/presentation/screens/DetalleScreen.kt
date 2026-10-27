package com.adso.pizca.presentation.screens

import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.adso.pizca.data.repository.RecetaRepository
import com.adso.pizca.presentation.components.PantallaEnConstruccion

// Responsable: Valeria
@Composable
fun DetalleScreen(recetaId: String, onVolver: () -> Unit, onCocinar: (String) -> Unit) {
    val receta = RecetaRepository.buscarPorId(recetaId)
    PantallaEnConstruccion(titulo = "Detalle: ${receta?.nombre ?: "?"}", responsable = "Valeria") {
        Button(onClick = { onCocinar(recetaId) }) { Text("Cocinar con Chef Teo") }
        OutlinedButton(onClick = onVolver) { Text("Volver") }
    }
}