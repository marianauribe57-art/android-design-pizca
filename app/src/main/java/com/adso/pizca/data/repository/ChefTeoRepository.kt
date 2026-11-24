package com.adso.pizca.data.repository

import com.adso.pizca.data.dto.Mensaje
import com.adso.pizca.data.dto.Receta
import kotlinx.coroutines.flow.Flow

/** Lo que Chef Teo sabe hacer con IA. No dice qué IA usa. */
interface ChefTeoRepository {
    /** La misma receta, con nombre, pasos e ingredientes en español */
    suspend fun traducir(receta: Receta): Result<Receta>

    /** La respuesta de Teo, sabiendo la receta, el paso y la conversación */
    suspend fun responder(
        receta: Receta,
        pasoActual: Int,
        historial: List<Mensaje>,
        pregunta: String
    ): Result<String>

    /**
     * NUEVO: la misma respuesta, pero "en vivo": va entregando el texto
     * a medida que Gemini lo escribe (cada vez el texto completo hasta ese momento).
     */
    fun responderEnVivo(
        receta: Receta,
        pasoActual: Int,
        historial: List<Mensaje>,
        pregunta: String
    ): Flow<String>
}
