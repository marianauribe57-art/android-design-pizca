package com.adso.pizca.data.repository

import com.adso.pizca.data.dto.Mensaje
import com.adso.pizca.data.dto.Receta

/** Lo que Chef Teo sabe hacer con IA. No dice qué IA usa. */
interface ChefTeoRepository {
    /** La misma receta, con pasos e ingredientes en español */
    suspend fun traducir(receta: Receta): Result<Receta>

    /** La respuesta de Teo, sabiendo la receta, el paso y la conversación */
    suspend fun responder(
        receta: Receta,
        pasoActual: Int,
        historial: List<Mensaje>,
        pregunta: String
    ): Result<String>
}