package com.adso.pizca.data.repository

import com.adso.pizca.data.api.CalificacionesApi
import com.adso.pizca.data.dto.CalificacionEnviar
import com.adso.pizca.data.dto.ResumenCalificaciones
import com.adso.pizca.data.network.ApiClient

class CalificacionRepositoryImpl(
    private val api: CalificacionesApi = ApiClient.calificacionesApi
) : CalificacionRepository {

    override suspend fun obtenerResumen(recetaId: String): Result<ResumenCalificaciones> = runCatching {
        val respuesta = api.obtenerResumen(recetaId)
        // El script puede responder bien por red pero con ok = false (por ejemplo, datos inválidos)
        if (!respuesta.ok) error(respuesta.error ?: "No se pudieron cargar las calificaciones")
        respuesta
    }

    override suspend fun enviar(recetaId: String, estrellas: Int, comentario: String): Result<Unit> = runCatching {
        val respuesta = api.enviar(CalificacionEnviar(recetaId, estrellas, comentario.trim()))
        if (!respuesta.ok) error(respuesta.error ?: "No se pudo guardar la calificación")
    }
}
