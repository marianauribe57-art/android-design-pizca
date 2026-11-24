package com.adso.pizca.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adso.pizca.data.dto.Mensaje
import com.adso.pizca.data.dto.Receta
import com.adso.pizca.data.repository.ChefTeoRepository
import com.adso.pizca.data.repository.ChefTeoRepositoryImpl
import com.adso.pizca.data.repository.RecetaRepository
import com.adso.pizca.data.repository.RecetaRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChefViewModel(
    private val repository: RecetaRepository = RecetaRepositoryImpl(),
    private val chefTeo: ChefTeoRepository = ChefTeoRepositoryImpl()   // NUEVO: Teo con Gemini
) : ViewModel() {

    private var siguienteId = 0L

    private val _receta = MutableStateFlow<Receta?>(null)
    val receta: StateFlow<Receta?> = _receta.asStateFlow()

    private val _pasoActual = MutableStateFlow(0)
    val pasoActual: StateFlow<Int> = _pasoActual.asStateFlow()

    private val _mensajes = MutableStateFlow<List<Mensaje>>(emptyList())
    val mensajes: StateFlow<List<Mensaje>> = _mensajes.asStateFlow()

    // true mientras Teo "piensa" (esperando a Gemini)
    private val _escribiendo = MutableStateFlow(false)
    val escribiendo: StateFlow<Boolean> = _escribiendo.asStateFlow()

    fun cargarReceta(id: String?) {
        if (id == null) {
            if (_mensajes.value.isEmpty()) {
                agregarDeTeo("¡Hola! Soy Chef Teo 🐭. Elige una receta en Inicio y toca \"Cocinar con Chef Teo\" para empezar.")
            }
            return
        }
        if (_receta.value?.id == id) return // ya la estamos cocinando
        viewModelScope.launch {
            repository.obtenerPorId(id)
                .onSuccess { receta ->
                    _receta.value = receta
                    _pasoActual.value = 0
                    _mensajes.value = emptyList()
                    agregarDeTeo("¡Vamos a cocinar ${receta.nombre}! ${receta.emoji}")
                    traducir(receta)   // CAMBIA: antes decía "te la leo en inglés"
                }
                .onFailure {
                    agregarDeTeo("Uy, no pude traer la receta 😢 Revisa tu internet e inténtalo otra vez.")
                }
        }
    }

    // NUEVO: le pedimos a Gemini la receta en español. Si falla, seguimos en inglés.
    private suspend fun traducir(receta: Receta) {
        _escribiendo.value = true
        chefTeo.traducir(receta)
            .onSuccess { traducida ->
                _receta.value = traducida
                agregarDeTeo("Te traduje la receta al español. 📖")
            }
            .onFailure {
                agregarDeTeo("No pude traducirla, así que te la leo en inglés como viene del recetario. 📖")
            }
        _escribiendo.value = false
        _receta.value?.pasos?.firstOrNull()?.let { agregarDeTeo("Paso 1: $it") }
    }

    fun enviarMensaje(texto: String) {
        if (texto.isBlank() || _escribiendo.value) return
        agregar(texto.trim(), esDeTeo = false)
        responder(texto.trim())   // CAMBIA: va tal cual lo escribió (antes iba en minúsculas)
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

    // CAMBIA: los comandos cortos se resuelven aquí (rápido y gratis);
    // cualquier otra pregunta la responde Gemini.
    private fun responder(texto: String) {
        val receta = _receta.value
        val comando = texto.lowercase()
        when {
            receta == null ->
                agregarDeTeo("Primero elige una receta en Inicio y toca \"Cocinar con Chef Teo\". 🐭")
            comando in setOf("siguiente", "listo", "sigo") -> siguientePaso()
            comando == "ingredientes" -> decirIngredientes()
            else -> preguntarATeo(receta, texto)
        }
    }

    // NUEVO: la conversación con Gemini
    private fun preguntarATeo(receta: Receta, pregunta: String) {
        viewModelScope.launch {
            _escribiendo.value = true
            chefTeo.responder(receta, _pasoActual.value, _mensajes.value, pregunta)
                .onSuccess { agregarDeTeo(it) }
                .onFailure { agregarDeTeo("Uy, se me enredó la cocina 🐭 Inténtalo otra vez en un momento.") }
            _escribiendo.value = false
        }
    }

    private fun agregarDeTeo(texto: String) = agregar(texto, esDeTeo = true)

    private fun agregar(texto: String, esDeTeo: Boolean) {
        _mensajes.value = _mensajes.value + Mensaje(siguienteId++, texto, esDeTeo)
    }
}