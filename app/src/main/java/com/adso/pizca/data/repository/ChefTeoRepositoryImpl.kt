package com.adso.pizca.data.repository

import com.adso.pizca.data.dto.Mensaje
import com.adso.pizca.data.dto.Receta
import com.adso.pizca.data.dto.TraduccionDto
import com.adso.pizca.data.network.GeminiClient
import com.google.firebase.ai.GenerativeModel
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
                "{\"pasos\": [\"...\"], \"ingredientes\": [{\"nombre\": \"...\", \"medida\": \"...\"}]}. " +
                "Mantén el mismo número y orden de pasos e ingredientes. " +
                "Convierte medidas: 1 cup = 1 taza, 1 tbsp = 1 cucharada, 1 tsp = 1 cucharadita.\n\n" +
                "PASOS:\n$pasos\n\nINGREDIENTES:\n$ingredientes"

        val texto = traductor.generateContent(prompt).text ?: error("Gemini no respondió")
        val traduccion = json.decodeFromString<TraduccionDto>(texto)

        // Si Gemini se comió o inventó pasos, mejor no usar la traducción
        if (traduccion.pasos.size != receta.pasos.size ||
            traduccion.ingredientes.size != receta.ingredientes.size
        ) error("La traducción no coincide con la receta")

        receta.copy(
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
        // Solo los últimos 10 mensajes: así la petición no crece sin fin
        val conversacion = historial.takeLast(10).joinToString("\n") {
            (if (it.esDeTeo) "Teo: " else "Usuario: ") + it.texto
        }
        val prompt = "Receta: ${receta.nombre}\n" +
                "Ingredientes: ${receta.ingredientes.joinToString { "${it.nombre} (${it.medida})" }}\n" +
                "Pasos:\n${receta.pasos.mapIndexed { i, p -> "${i + 1}. $p" }.joinToString("\n")}\n" +
                "El usuario va en el paso ${pasoActual + 1}.\n\n" +
                "Conversación reciente:\n$conversacion\n\n" +
                "Responde como Chef Teo, en máximo 3 frases, a: $pregunta"

        chat.generateContent(prompt).text?.trim() ?: error("Gemini no respondió")
    }
}