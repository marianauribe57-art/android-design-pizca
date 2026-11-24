package com.adso.pizca.data.repository

import com.adso.pizca.data.dto.Receta

/**
 * Lo que la app puede pedir para verse en español.
 * Las traducciones se guardan en el celular: la segunda vez son instantáneas.
 */
interface TraduccionRepository {
    /** Receta completa (nombre, pasos e ingredientes) en español */
    suspend fun receta(receta: Receta): Result<Receta>

    /** Solo los nombres de una lista de recetas (Inicio y Búsqueda). Si falla, devuelve la lista igual */
    suspend fun nombres(recetas: List<Receta>): List<Receta>

    /** Lo que el usuario escribió, en inglés, para buscar en TheMealDB. Si falla, devuelve el texto igual */
    suspend fun aIngles(texto: String): String
}
