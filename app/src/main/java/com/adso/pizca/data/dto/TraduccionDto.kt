package com.adso.pizca.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class TraduccionDto(
    val pasos: List<String> = emptyList(),
    val ingredientes: List<IngredienteTraducidoDto> = emptyList()
)

@Serializable
data class IngredienteTraducidoDto(
    val nombre: String = "",
    val medida: String = ""
)