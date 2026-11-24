package com.adso.pizca.data.repository

import com.adso.pizca.PizcaApp
import com.adso.pizca.data.dto.IngredienteTraducidoDto
import com.adso.pizca.data.dto.InglesDto
import com.adso.pizca.data.dto.NombresDto
import com.adso.pizca.data.dto.Receta
import com.adso.pizca.data.dto.TraduccionDto
import com.adso.pizca.data.local.TraduccionDao
import com.adso.pizca.data.local.TraduccionEntity
import com.adso.pizca.data.network.GeminiClient
import com.google.firebase.ai.GenerativeModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

/**
 * La "cocina" de TraduccionRepository.
 * Siempre mira primero en Room (tabla "traducciones"); solo si no está, le pregunta a Gemini
 * y guarda la respuesta para la próxima vez.
 */
class TraduccionRepositoryImpl(
    private val chefTeo: ChefTeoRepository = ChefTeoRepositoryImpl(),
    private val ligero: GenerativeModel = GeminiClient.ligero,
    private val dao: TraduccionDao = PizcaApp.database.traduccionDao()
) : TraduccionRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun receta(receta: Receta): Result<Receta> = runCatching {
        val clave = "receta:${receta.id}"

        // 1. ¿Ya la habíamos traducido? → instantáneo
        val guardada = dao.obtener(clave)?.let { json.decodeFromString<TraduccionDto>(it) }
        if (guardada != null && guardada.pasos.size == receta.pasos.size &&
            guardada.ingredientes.size == receta.ingredientes.size
        ) {
            return@runCatching receta.copy(
                nombre = guardada.nombre.ifBlank { receta.nombre },
                pasos = guardada.pasos,
                ingredientes = receta.ingredientes.zip(guardada.ingredientes) { original, t ->
                    original.copy(nombre = t.nombre, medida = t.medida)
                }
            )
        }

        // 2. No estaba: la traduce Gemini (lo que ya hacía Chef Teo) y la guardamos
        val traducida = chefTeo.traducir(receta).getOrThrow()
        val dto = TraduccionDto(
            nombre = traducida.nombre,
            pasos = traducida.pasos,
            ingredientes = traducida.ingredientes.map { IngredienteTraducidoDto(it.nombre, it.medida) }
        )
        dao.guardar(TraduccionEntity(clave, json.encodeToString(dto)))
        dao.guardar(TraduccionEntity("nombre:${receta.id}", traducida.nombre))  // así Inicio también lo sabe
        traducida
    }

    override suspend fun nombres(recetas: List<Receta>): List<Receta> {
        if (recetas.isEmpty()) return recetas
        return try {
            // 1. Los que ya estaban guardados
            val conocidos = mutableMapOf<String, String>()
            for (r in recetas) dao.obtener("nombre:${r.id}")?.let { conocidos[r.id] = it }

            // 2. Los que faltan, todos en UNA sola pregunta a Gemini
            val faltan = recetas.filter { it.id !in conocidos }
            if (faltan.isNotEmpty()) conocidos.putAll(traducirNombres(faltan))

            recetas.map { r -> r.copy(nombre = conocidos[r.id] ?: r.nombre) }
        } catch (e: CancellationException) {
            throw e                     // el usuario se fue de la pantalla: no es un error
        } catch (e: Exception) {
            android.util.Log.e("Traduccion", "Falló traducir nombres", e)
            recetas                     // si falla, se ven en inglés (la app no se cae)
        }
    }

    private suspend fun traducirNombres(recetas: List<Receta>): Map<String, String> {
        val lista = recetas.mapIndexed { i, r -> "${i + 1}. ${r.nombre}" }.joinToString("\n")
        val prompt = "Traduce al español de Colombia estos nombres de platos. " +
                "Si un nombre es conocido así en Colombia (ej: Lasagna, Sushi, Pizza), déjalo igual. " +
                "Responde SOLO con JSON con esta forma: {\"nombres\": [\"...\"]}, " +
                "con la misma cantidad y en el mismo orden.\n\n$lista"

        val texto = ligero.generateContent(prompt).text ?: error("Gemini no respondió")
        val nombres = json.decodeFromString<NombresDto>(texto).nombres
        if (nombres.size != recetas.size) error("La traducción de nombres no coincide")

        val resultado = mutableMapOf<String, String>()
        recetas.zip(nombres).forEach { (receta, nombre) ->
            if (nombre.isNotBlank()) {
                resultado[receta.id] = nombre
                dao.guardar(TraduccionEntity("nombre:${receta.id}", nombre))
            }
        }
        return resultado
    }

    override suspend fun aIngles(texto: String): String {
        val limpio = texto.trim()
        if (limpio.isEmpty()) return limpio
        val clave = "ingles:${limpio.lowercase()}"
        dao.obtener(clave)?.let { return it }

        return try {
            val prompt = "Traduce al inglés este término de cocina para buscarlo en un recetario en inglés. " +
                    "Si ya está en inglés, déjalo igual. " +
                    "Responde SOLO con JSON con esta forma: {\"ingles\": \"...\"}\n\n$limpio"
            val respuesta = ligero.generateContent(prompt).text ?: error("Gemini no respondió")
            val ingles = json.decodeFromString<InglesDto>(respuesta).ingles.trim()
            if (ingles.isBlank()) limpio
            else ingles.also { dao.guardar(TraduccionEntity(clave, it)) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            android.util.Log.e("Traduccion", "Falló traducir búsqueda", e)
            limpio                      // si falla, buscamos con lo que escribió
        }
    }
}
