package com.adso.pizca.data.network

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.ThinkingLevel
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.ai.type.thinkingConfig
import kotlinx.coroutines.delay
import kotlin.coroutines.cancellation.CancellationException

object GeminiClient {

    // CAMBIA: ya no es un solo modelo, sino una lista en orden de preferencia.
    // Si uno está saturado ("high demand"), se prueba el siguiente.
    private val MODELOS = listOf(
        "gemini-3.5-flash",        // principal
        "gemini-3.6-flash",        // respaldo 1: más nuevo
        "gemini-3.5-flash-lite"    // respaldo 2: el más liviano
    )

    /** La personalidad de Teo: Gemini la lee antes de cada respuesta */
    private val personalidad = content {
        text(
            "Eres Chef Teo, un ratón chef simpático de la app Pizca. " +
                    "Hablas siempre en español de Colombia, con frases cortas y amables, " +
                    "y usas máximo un emoji por mensaje. " +
                    "Solo hablas de cocina y de la receta que el usuario está preparando."
        )
    }

    // Gemini "piensa" antes de responder. Con LOW piensa menos y responde más rápido.
    private val pensarPoco = thinkingConfig { thinkingLevel = ThinkingLevel.LOW }

    private fun modelo(nombre: String, json: Boolean, conPersonalidad: Boolean): GenerativeModel =
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(
                modelName = nombre,
                generationConfig = generationConfig {
                    if (json) responseMimeType = "application/json"
                    thinkingConfig = pensarPoco
                },
                systemInstruction = if (conPersonalidad) personalidad else null
            )

    /** Para conversar: texto normal, con la personalidad de Teo */
    val chats: List<GenerativeModel> by lazy {
        MODELOS.map { modelo(it, json = false, conPersonalidad = true) }
    }

    /** Para traducir una receta completa: responde SOLO JSON */
    val traductores: List<GenerativeModel> by lazy {
        MODELOS.map { modelo(it, json = true, conPersonalidad = false) }
    }

    /** Para traducciones cortas (nombres, búsquedas): SOLO JSON, empezando por el más liviano */
    val ligeros: List<GenerativeModel> by lazy { traductores.reversed() }
}

/**
 * NUEVO: le pide algo a Gemini probando los modelos en orden.
 * Si uno falla (por ejemplo, "high demand"), espera 1 segundo y prueba el siguiente.
 * Si fallan todos, sale el error del último.
 */
suspend fun <T> probarEnOrden(
    modelos: List<GenerativeModel>,
    pedir: suspend (GenerativeModel) -> T
): T {
    var ultimoError: Exception? = null
    for ((i, modelo) in modelos.withIndex()) {
        try {
            return pedir(modelo)
        } catch (e: CancellationException) {
            throw e                                  // se cerró la pantalla: no es un error
        } catch (e: Exception) {
            ultimoError = e
            Log.w("Gemini", "Modelo ${i + 1} de ${modelos.size} falló, pruebo el siguiente", e)
            if (i < modelos.lastIndex) delay(1_000)
        }
    }
    throw ultimoError ?: IllegalStateException("No hay modelos de Gemini configurados")
}
