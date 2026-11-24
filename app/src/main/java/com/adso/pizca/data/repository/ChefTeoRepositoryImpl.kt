package com.adso.pizca.data.repository

import com.adso.pizca.data.dto.Mensaje
import com.adso.pizca.data.dto.Receta
import com.adso.pizca.data.dto.TraduccionDto
import com.adso.pizca.data.network.GeminiClient
import com.google.firebase.ai.GenerativeModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

class ChefTeoRepositoryImpl(
    private val chat: GenerativeModel = GeminiClient.chat,
    private val traductor: GenerativeModel = GeminiClient.traductor
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

        val texto = traductor.generateContent(prompt).text ?: error("Gemini no respondió")
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
        chat.generateContent(armarPrompt(receta, pasoActual, historial, pregunta))
            .text?.trim() ?: error("Gemini no respondió")
    }

    // NUEVO: Gemini va mandando la respuesta por pedacitos; juntamos los pedacitos
    // y entregamos el texto completo hasta ese momento.
    override fun responderEnVivo(
        receta: Receta,
        pasoActual: Int,
        historial: List<Mensaje>,
        pregunta: String
    ): Flow<String> = flow {
        var acumulado = ""
        chat.generateContentStream(armarPrompt(receta, pasoActual, historial, pregunta))
            .collect { pedacito ->
                acumulado += pedacito.text.orEmpty()
                if (acumulado.isNotBlank()) emit(acumulado.trim())
            }
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
