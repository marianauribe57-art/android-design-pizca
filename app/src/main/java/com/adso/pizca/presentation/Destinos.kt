package com.adso.pizca.presentation

import kotlinx.serialization.Serializable

/**
 * RUTAS TYPE-SAFE DE PIZCA
 * Cada pantalla tiene su "dirección".
 */

// Pestañas de la barra inferior: no necesitan datos
@Serializable
object InicioRuta

@Serializable
object BusquedaRuta

@Serializable
object PerfilRuta

// Chef: puede llegar con una receta (desde Detalle) o sin ninguna (desde la barra)
@Serializable
data class ChefRuta(val recetaId: String? = null)

// Detalle: siempre necesita saber qué receta mostrar
@Serializable
data class DetalleRuta(val recetaId: String)