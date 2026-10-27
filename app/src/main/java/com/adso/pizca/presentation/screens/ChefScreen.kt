package com.adso.pizca.presentation.screens

import androidx.compose.runtime.Composable
import com.adso.pizca.data.repository.RecetaRepository
import com.adso.pizca.presentation.components.PantallaEnConstruccion

// Responsable: Ismael
@Composable
fun ChefScreen(recetaId: String?) {
    val receta = recetaId?.let { RecetaRepository.buscarPorId(it) }
    val titulo = if (receta != null) "Chef Teo · ${receta.nombre}" else "Chef Teo"
    PantallaEnConstruccion(titulo = titulo, responsable = "Ismael")
}