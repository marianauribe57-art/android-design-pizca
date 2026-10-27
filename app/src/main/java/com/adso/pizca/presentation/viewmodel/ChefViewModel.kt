package com.adso.pizca.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.adso.pizca.data.dto.Mensaje
import com.adso.pizca.data.dto.Receta
import com.adso.pizca.data.repository.RecetaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChefViewModel : ViewModel() {

    private var siguienteId = 0L

    private val _receta = MutableStateFlow<Receta?>(null)
    val receta: StateFlow<Receta?> = _receta.asStateFlow()

    private val _pasoActual = MutableStateFlow(0)
    val pasoActual: StateFlow<Int> = _pasoActual.asStateFlow()

    private val _mensajes = MutableStateFlow<List<Mensaje>>(emptyList())
    val mensajes: StateFlow<List<Mensaje>> = _mensajes.asStateFlow()

    fun cargarReceta(id: String?) {
        if (id == null) {
            if (_mensajes.value.isEmpty()) {
                agregarDeTeo("¡Hola! Soy Chef Teo 🐭. Elige una receta en Inicio y toca \"Cocinar con Chef Teo\" para empezar.")
            }
            return
        }
        if (_receta.value?.id == id) return // ya la estamos cocinando
        val receta = RecetaRepository.buscarPorId(id) ?: return
        _receta.value = receta
        _pasoActual.value = 0
        _mensajes.value = emptyList()
        agregarDeTeo("¡Vamos a cocinar ${receta.nombre}! ${receta.emoji}")
        agregarDeTeo("Paso 1: ${receta.pasos[0]}")
    }

    fun enviarMensaje(texto: String) {
        if (texto.isBlank()) return
        agregar(texto.trim(), esDeTeo = false)
        responder(texto.lowercase())
    }

    fun siguientePaso() {
        val receta = _receta.value ?: return
        if (_pasoActual.value < receta.pasos.lastIndex) {
            _pasoActual.value = _pasoActual.value + 1
            agregarDeTeo("Paso ${_pasoActual.value + 1}: ${receta.pasos[_pasoActual.value]}")
        } else {
            agregarDeTeo("¡Terminaste! Buen provecho 🎉")
        }
    }

    fun decirIngredientes() {
        val receta = _receta.value ?: return
        agregarDeTeo(
            "Para ${receta.nombre} necesitas: " +
                    receta.ingredientes.joinToString { "${it.nombre} (${it.medida})" }
        )
    }

    // Respuestas simples por palabras clave. Más adelante esto lo hará Gemini.
    private fun responder(texto: String) {
        val receta = _receta.value
        when {
            receta == null ->
                agregarDeTeo("Primero elige una receta en Inicio y toca \"Cocinar con Chef Teo\". 🐭")
            "siguiente" in texto || "listo" in texto || "sigo" in texto -> siguientePaso()
            "ingrediente" in texto -> decirIngredientes()
            "paso" in texto ->
                agregarDeTeo("Vamos en el paso ${_pasoActual.value + 1}: ${receta.pasos[_pasoActual.value]}")
            else ->
                agregarDeTeo("¡Buena pregunta! Por ahora entiendo \"siguiente\", \"ingredientes\" y \"paso\". Pronto responderé todo con Gemini. 🐭")
        }
    }

    private fun agregarDeTeo(texto: String) = agregar(texto, esDeTeo = true)

    private fun agregar(texto: String, esDeTeo: Boolean) {
        _mensajes.value = _mensajes.value + Mensaje(siguienteId++, texto, esDeTeo)
    }
}