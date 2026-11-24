package com.adso.pizca.data.network

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.ThinkingLevel
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.ai.type.thinkingConfig

object GeminiClient {

    private const val MODELO = "gemini-3.5-flash"
    private const val MODELO_LIGERO = "gemini-3.5-flash-lite"   // NUEVO: más rápido, para tareas cortas

    /** La personalidad de Teo: Gemini la lee antes de cada respuesta */
    private val personalidad = content {
        text(
            "Eres Chef Teo, un ratón chef simpático de la app Pizca. " +
                    "Hablas siempre en español de Colombia, con frases cortas y amables, " +
                    "y usas máximo un emoji por mensaje. " +
                    "Solo hablas de cocina y de la receta que el usuario está preparando."
        )
    }

    // NUEVO: Gemini "piensa" antes de responder. Con LOW piensa menos y responde más rápido.
    // Para conversar y traducir una receta no hace falta pensar mucho.
    private val pensarPoco = thinkingConfig { thinkingLevel = ThinkingLevel.LOW }

    /** Para conversar: responde texto normal */
    val chat by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(
                modelName = MODELO,
                generationConfig = generationConfig { thinkingConfig = pensarPoco },   // CAMBIA
                systemInstruction = personalidad
            )
    }

    /** Para traducir una receta completa: responde SOLO JSON */
    val traductor by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(
                modelName = MODELO,
                generationConfig = generationConfig {
                    responseMimeType = "application/json"
                    thinkingConfig = pensarPoco                                        // CAMBIA
                }
            )
    }

    /** NUEVO: para traducciones cortas (nombres de recetas, lo que se busca): responde SOLO JSON */
    val ligero by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(
                modelName = MODELO_LIGERO,
                generationConfig = generationConfig { responseMimeType = "application/json" }
            )
    }
}
