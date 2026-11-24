package com.adso.pizca.data.api

import com.adso.pizca.data.dto.CalificacionEnviar
import com.adso.pizca.data.dto.RespuestaEnvio
import com.adso.pizca.data.dto.ResumenCalificaciones
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/** Endpoints del Apps Script de calificaciones. Los dos apuntan a "exec". */
interface CalificacionesApi {

    // GET .../exec?recetaId=52772 → promedio, total y comentarios (doGet)
    @GET("exec")
    suspend fun obtenerResumen(@Query("recetaId") recetaId: String): ResumenCalificaciones

    // POST .../exec con el JSON de la calificación en el cuerpo (doPost)
    @POST("exec")
    suspend fun enviar(@Body calificacion: CalificacionEnviar): RespuestaEnvio
}
