package com.adso.pizca.data.network

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig

object GeminiClient {

    private const val MODELO = "gemini-3.5-flash"

    /** La personalidad de Teo: Gemini la lee antes de cada respuesta */
    private val personalidad = content {
        text(
            "Eres Chef Teo, un ratón chef simpático de la app Pizca. " +
                    "Hablas siempre en español de Colombia, con frases cortas y amables, " +
                    "y usas máximo un emoji por mensaje. " +
                    "Solo hablas de cocina y de la receta que el usuario está preparando."
        )
    }

    /** Para conversar: responde texto normal */
    val chat by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(modelName = MODELO, systemInstruction = personalidad)
    }

    /** Para traducir: responde SOLO JSON */
    val traductor by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(
                modelName = MODELO,
                generationConfig = generationConfig { responseMimeType = "application/json" }
            )
    }
}