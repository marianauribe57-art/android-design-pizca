package com.adso.pizca.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adso.pizca.data.dto.Mensaje
import com.adso.pizca.data.dto.Receta
import com.adso.pizca.data.repository.ChefTeoRepository
import com.adso.pizca.data.repository.ChefTeoRepositoryImpl
import com.adso.pizca.data.repository.RecetaRepository
import com.adso.pizca.data.repository.RecetaRepositoryImpl
import com.adso.pizca.data.repository.TraduccionRepository
import com.adso.pizca.data.repository.TraduccionRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class ChefViewModel(
    private val repository: RecetaRepository = RecetaRepositoryImpl(),
    private val chefTeo: ChefTeoRepository = ChefTeoRepositoryImpl(),                // Teo con Gemini
    private val traduccion: TraduccionRepository = TraduccionRepositoryImpl()        // NUEVO: traducciones guardadas
) : ViewModel() {

    private var siguienteId = 0L

    private val _receta = MutableStateFlow<Receta?>(null)
    val receta: StateFlow<Receta?> = _receta.asStateFlow()

    private val _pasoActual = MutableStateFlow(0)
    val pasoActual: StateFlow<Int> = _pasoActual.asStateFlow()

    private val _mensajes = MutableStateFlow<List<Mensaje>>(emptyList())
    val mensajes: StateFlow<List<Mensaje>> = _mensajes.asStateFlow()

    // true mientras Teo "piensa" (antes de que llegue la primera palabra de Gemini)
    private val _escribiendo = MutableStateFlow(false)
    val escribiendo: StateFlow<Boolean> = _escribiendo.asStateFlow()

    // NUEVO: true mientras la respuesta todavía está llegando por pedacitos
    private var respondiendo = false

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
                    traducir(receta)
                }
                .onFailure {
                    agregarDeTeo("Uy, no pude traer la receta 😢 Revisa tu internet e inténtalo otra vez.")
                }
        }
    }

    // CAMBIA: la traducción sale del TraduccionRepository. Si ya se tradujo antes
    // (por ejemplo, en Detalle), está guardada y es instantánea.
    private suspend fun traducir(receta: Receta) {
        _escribiendo.value = true
        traduccion.receta(receta)
            .onSuccess { traducida ->
                _receta.value = traducida
                agregarDeTeo("¡Vamos a cocinar ${traducida.nombre}! ${traducida.emoji}")
            }
            .onFailure { error ->
                Log.e("ChefTeo", "Falló traducir", error)
                agregarDeTeo("¡Vamos a cocinar ${receta.nombre}! ${receta.emoji}")
                agregarDeTeo("No pude traducirla, así que te la leo en inglés como viene del recetario. 📖")
            }
        _escribiendo.value = false
        _receta.value?.pasos?.firstOrNull()?.let { agregarDeTeo("Paso 1: $it") }
    }

    fun enviarMensaje(texto: String) {
        if (texto.isBlank() || _escribiendo.value || respondiendo) return   // CAMBIA: + respondiendo
        agregar(texto.trim(), esDeTeo = false)
        responder(texto.trim())
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

    // Los comandos cortos se resuelven aquí (rápido y gratis);
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

    // CAMBIA: la respuesta de Gemini llega "en vivo". Con la primera palabra aparece
    // la burbuja de Teo, y se va llenando mientras Gemini sigue escribiendo.
    private fun preguntarATeo(receta: Receta, pregunta: String) {
        viewModelScope.launch {
            _escribiendo.value = true
            respondiendo = true
            var idRespuesta: Long? = null
            try {
                chefTeo.responderEnVivo(receta, _pasoActual.value, _mensajes.value, pregunta)
                    .collect { textoHastaAhora ->
                        val id = idRespuesta
                        if (id == null) {
                            _escribiendo.value = false               // ya llegó la primera palabra
                            idRespuesta = agregarDeTeo(textoHastaAhora)
                        } else {
                            reemplazar(id, textoHastaAhora)
                        }
                    }
                if (idRespuesta == null) {
                    agregarDeTeo("Uy, me quedé sin palabras 🐭 Pregúntame otra vez.")
                }
            } catch (e: CancellationException) {
                throw e                                  // se cerró la pantalla: no es un error
            } catch (e: Exception) {
                Log.e("ChefTeo", "Falló responder", e)
                if (idRespuesta == null) {
                    agregarDeTeo("Uy, se me enredó la cocina 🐭 Inténtalo otra vez en un momento.")
                }
            } finally {
                _escribiendo.value = false
                respondiendo = false
            }
        }
    }

    private fun agregarDeTeo(texto: String): Long = agregar(texto, esDeTeo = true)

    // CAMBIA: devuelve el id del mensaje, para poder ir actualizándolo
    private fun agregar(texto: String, esDeTeo: Boolean): Long {
        val id = siguienteId++
        _mensajes.value = _mensajes.value + Mensaje(id, texto, esDeTeo)
        return id
    }

    // NUEVO: cambia el texto de un mensaje que ya está en el chat
    private fun reemplazar(id: Long, texto: String) {
        _mensajes.value = _mensajes.value.map { if (it.id == id) it.copy(texto = texto) else it }
    }
}
