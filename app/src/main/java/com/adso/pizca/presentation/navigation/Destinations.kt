package com.adso.pizca.presentation.navigation

import kotlinx.serialization.Serializable

/**
 * RUTAS TYPE-SAFE DE PIZCA
 * Todas las "direcciones" de la app agrupadas en una sola familia: Destination.
 */
sealed interface Destination {

    // Pestañas de la barra inferior: no necesitan datos
    @Serializable
    data object Inicio : Destination

    @Serializable
    data object Busqueda : Destination

    @Serializable
    data object Perfil : Destination

    // Chef: puede llegar con una receta (desde Detalle) o sin ninguna (desde la barra)
    @Serializable
    data class Chef(val recetaId: String? = null) : Destination

    // Detalle: siempre necesita saber qué receta mostrar
    @Serializable
    data class Detalle(val recetaId: String) : Destination
}