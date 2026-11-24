package com.adso.pizca.data.dto

import kotlinx.serialization.Serializable

// Calificaciones de la comunidad: así se ve el JSON que va y viene del Apps Script.
// Los nombres de los campos son los mismos del script (recetaId, estrellas, promedio...).

/** Lo que la app ENVÍA al calificar (doPost) */
@Serializable
data class CalificacionEnviar(
    val recetaId: String,
    val estrellas: Int,
    val comentario: String = ""
)

/** Lo que responde doPost */
@Serializable
data class RespuestaEnvio(
    val ok: Boolean,
    val error: String? = null
)

/** Lo que responde doGet: el resumen de calificaciones de una receta */
@Serializable
data class ResumenCalificaciones(
    val ok: Boolean,
    val recetaId: String = "",
    val promedio: Double = 0.0,
    val total: Int = 0,
    val comentarios: List<ComentarioDto> = emptyList(),
    val error: String? = null
)

/** Un comentario dentro del resumen */
@Serializable
data class ComentarioDto(
    val estrellas: Int,
    val comentario: String,
    val fecha: String
)
