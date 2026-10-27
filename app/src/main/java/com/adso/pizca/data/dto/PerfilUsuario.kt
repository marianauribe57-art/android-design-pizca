package com.adso.pizca.data.dto

enum class Objetivo(val titulo: String) {
    BAJAR("Bajar de peso"),
    MANTENER("Mantenerme"),
    GANAR("Ganar músculo")
}

// Los datos que escribe el usuario
data class PerfilUsuario(
    val pesoKg: Double,
    val estaturaCm: Double,
    val edad: Int,
    val esMujer: Boolean,
    val objetivo: Objetivo
)

// El resultado del cálculo
data class MetaDiaria(
    val calorias: Int,
    val proteinas: Int
)