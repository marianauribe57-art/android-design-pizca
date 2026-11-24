package com.adso.pizca.data.repository

import com.adso.pizca.data.dto.ResumenCalificaciones

/** Calificaciones compartidas entre usuarios (Apps Script + Google Sheet). */
interface CalificacionRepository {
    suspend fun obtenerResumen(recetaId: String): Result<ResumenCalificaciones>
    suspend fun enviar(recetaId: String, estrellas: Int, comentario: String): Result<Unit>
}
