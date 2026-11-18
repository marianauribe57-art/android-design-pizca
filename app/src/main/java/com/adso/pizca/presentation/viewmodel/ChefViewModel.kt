package com.adso.pizca.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adso.pizca.data.dto.Mensaje
import com.adso.pizca.data.dto.Receta
import com.adso.pizca.data.repository.FakeRecetaRepository
import com.adso.pizca.data.repository.RecetaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// CAMBIA: el repositorio entra por el constructor
class ChefViewModel(
    private val repository: RecetaRepository = FakeRecetaRepository()   // Fase 2: RecetaRepositoryImpl()
) : ViewModel() {

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
        // CAMBIA: la receta llega del repositorio dentro de una corrutina
        viewModelScope.launch {
            repository.obtenerPorId(id)
                .onSuccess { receta ->
                    _receta.value = receta
                    _pasoActual.value = 0
                    _mensajes.value = emptyList()
                    agregarDeTeo("¡Vamos a cocinar ${receta.nombre}! ${receta.emoji}")
                    agregarDeTeo("Te leo la receta en inglés, como viene del recetario. 📖")
                    // firstOrNull: si la receta no trae pasos, no se cierra la app
                    receta.pasos.firstOrNull()?.let { agregarDeTeo("Paso 1: $it") }
                }
                .onFailure {
                    agregarDeTeo("Uy, no pude traer la receta 😢 Revisa tu internet e inténtalo otra vez.")
                }
        }
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
                agregarDeTeo(
                    "Vamos en el paso ${_pasoActual.value + 1}: " +
                            (receta.pasos.getOrNull(_pasoActual.value) ?: "Esta receta no trae pasos.")   // CAMBIA
                )
            else ->
                agregarDeTeo("¡Buena pregunta! Por ahora entiendo \"siguiente\", \"ingredientes\" y \"paso\". Pronto responderé todo con Gemini. 🐭")
        }
    }

    private fun agregarDeTeo(texto: String) = agregar(texto, esDeTeo = true)

    private fun agregar(texto: String, esDeTeo: Boolean) {
        _mensajes.value = _mensajes.value + Mensaje(siguienteId++, texto, esDeTeo)
    }
}