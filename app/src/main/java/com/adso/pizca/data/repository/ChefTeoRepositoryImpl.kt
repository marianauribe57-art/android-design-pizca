package com.adso.pizca.data.repository

import com.adso.pizca.data.dto.Mensaje
import com.adso.pizca.data.dto.Receta
import com.adso.pizca.data.dto.TraduccionDto
import android.util.Log
import com.adso.pizca.data.network.GeminiClient
import com.adso.pizca.data.network.probarEnOrden
import com.google.firebase.ai.GenerativeModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

class ChefTeoRepositoryImpl(
    // CAMBIA: listas de modelos; si uno está saturado se usa el siguiente
    private val chats: List<GenerativeModel> = GeminiClient.chats,
    private val traductores: List<GenerativeModel> = GeminiClient.traductores
) : ChefTeoRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun traducir(receta: Receta): Result<Receta> = runCatching {
        val pasos = receta.pasos.mapIndexed { i, p -> "${i + 1}. $p" }.joinToString("\n")
        val ingredientes = receta.ingredientes.joinToString("\n") { "- ${it.nombre} | ${it.medida}" }
        val prompt = "Traduce al español de Colombia esta receta. " +
                "Responde SOLO con JSON con esta forma: " +
                "{\"nombre\": \"...\", \"pasos\": [\"...\"], \"ingredientes\": [{\"nombre\": \"...\", \"medida\": \"...\"}]}. " +
                "Mantén el mismo número y orden de pasos e ingredientes. " +
                "Si el nombre del plato es conocido así en Colombia (ej: Lasagna, Sushi), déjalo igual. " +
                "Convierte medidas: 1 cup = 1 taza, 1 tbsp = 1 cucharada, 1 tsp = 1 cucharadita.\n\n" +
                "NOMBRE: ${receta.nombre}\n\n" +                                   // NUEVO
                "PASOS:\n$pasos\n\nINGREDIENTES:\n$ingredientes"

        val texto = probarEnOrden(traductores) { it.generateContent(prompt).text }   // CAMBIA
            ?: error("Gemini no respondió")
        val traduccion = json.decodeFromString<TraduccionDto>(texto)

        // Si Gemini se comió o inventó pasos, mejor no usar la traducción
        if (traduccion.pasos.size != receta.pasos.size ||
            traduccion.ingredientes.size != receta.ingredientes.size
        ) error("La traducción no coincide con la receta")

        receta.copy(
            nombre = traduccion.nombre.ifBlank { receta.nombre },                  // NUEVO
            pasos = traduccion.pasos,
            ingredientes = receta.ingredientes.zip(traduccion.ingredientes) { original, t ->
                original.copy(nombre = t.nombre, medida = t.medida)
            }
        )
    }

    override suspend fun responder(
        receta: Receta,
        pasoActual: Int,
        historial: List<Mensaje>,
        pregunta: String
    ): Result<String> = runCatching {
        val prompt = armarPrompt(receta, pasoActual, historial, pregunta)
        probarEnOrden(chats) { it.generateContent(prompt).text }?.trim()            // CAMBIA
            ?: error("Gemini no respondió")
    }

    // Gemini va mandando la respuesta por pedacitos; juntamos los pedacitos
    // y entregamos el texto completo hasta ese momento.
    // CAMBIA: si un modelo falla ANTES de escribir algo, se prueba el siguiente.
    override fun responderEnVivo(
        receta: Receta,
        pasoActual: Int,
        historial: List<Mensaje>,
        pregunta: String
    ): Flow<String> = flow {
        val prompt = armarPrompt(receta, pasoActual, historial, pregunta)
        var ultimoError: Exception? = null
        for ((i, modelo) in chats.withIndex()) {
            var acumulado = ""
            try {
                modelo.generateContentStream(prompt).collect { pedacito ->
                    acumulado += pedacito.text.orEmpty()
                    if (acumulado.isNotBlank()) emit(acumulado.trim())
                }
                return@flow                              // terminó bien: listo
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (acumulado.isNotBlank()) throw e      // ya se veía media respuesta: no la repetimos
                ultimoError = e
                Log.w("Gemini", "Modelo ${i + 1} de ${chats.size} falló, pruebo el siguiente", e)
                if (i < chats.lastIndex) delay(1_000)
            }
        }
        throw ultimoError ?: IllegalStateException("No hay modelos de Gemini configurados")
    }

    // CAMBIA: el prompt ahora es más corto (más rápido): solo el paso actual,
    // los nombres de los ingredientes y los últimos 6 mensajes.
    private fun armarPrompt(
        receta: Receta,
        pasoActual: Int,
        historial: List<Mensaje>,
        pregunta: String
    ): String {
        val conversacion = historial.takeLast(6).joinToString("\n") {
            (if (it.esDeTeo) "Teo: " else "Usuario: ") + it.texto
        }
        val paso = receta.pasos.getOrNull(pasoActual).orEmpty()
        return "Receta: ${receta.nombre}\n" +
                "Ingredientes: ${receta.ingredientes.joinToString { it.nombre }}\n" +
                "Paso actual (${pasoActual + 1} de ${receta.pasos.size}): $paso\n\n" +
                "Conversación reciente:\n$conversacion\n\n" +
                "Responde como Chef Teo, en máximo 3 frases, a: $pregunta"
    }
}
