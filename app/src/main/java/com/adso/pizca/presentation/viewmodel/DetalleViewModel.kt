package com.adso.pizca.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adso.pizca.data.dto.Receta
import com.adso.pizca.data.dto.ResumenCalificaciones
import com.adso.pizca.data.repository.CalificacionRepository
import com.adso.pizca.data.repository.CalificacionRepositoryImpl
import com.adso.pizca.data.repository.RecetaRepositoryImpl
import com.adso.pizca.data.repository.FavoritaRepositoryImpl
import com.adso.pizca.data.repository.FavoritaRepository
import com.adso.pizca.data.repository.RecetaRepository
import com.adso.pizca.data.repository.TraduccionRepository
import com.adso.pizca.data.repository.TraduccionRepositoryImpl
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetalleViewModel(
    private val recetaRepository: RecetaRepository = RecetaRepositoryImpl(),
    private val favoritaRepository: FavoritaRepository = FavoritaRepositoryImpl(),
    private val traduccion: TraduccionRepository = TraduccionRepositoryImpl(),   // NUEVO
    private val calificaciones: CalificacionRepository = CalificacionRepositoryImpl()   // NUEVO: Apps Script
) : ViewModel() {

    // Las recetas de ejemplo rinden 4 raciones
    private val racionesBase = 4

    private val _receta = MutableStateFlow<Receta?>(null)
    val receta: StateFlow<Receta?> = _receta.asStateFlow()

    private val _raciones = MutableStateFlow(racionesBase)
    val raciones: StateFlow<Int> = _raciones.asStateFlow()

    // CAMBIA: antes era un Set en memoria; ahora lo dice el repositorio (Room)
    private val _esFavorita = MutableStateFlow(false)
    val esFavorita: StateFlow<Boolean> = _esFavorita.asStateFlow()

    // NUEVO: estados de carga y error
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var favoritaJob: Job? = null

    // NUEVO: aviso cuando Gemini no pudo traducir (por ejemplo, porque está saturado)
    private val _avisoTraduccion = MutableStateFlow<String?>(null)
    val avisoTraduccion: StateFlow<String?> = _avisoTraduccion.asStateFlow()

    // NUEVO: ¿el error es porque Gemini está saturado ("high demand", "overloaded", 503)?
    private fun esAltaDemanda(error: Throwable): Boolean {
        val texto = error.message.orEmpty().lowercase()
        return "demand" in texto || "overloaded" in texto || "503" in texto
    }

    // NUEVO: calificaciones de la comunidad (Apps Script + Google Sheet)
    private val _resumen = MutableStateFlow<ResumenCalificaciones?>(null)
    val resumen: StateFlow<ResumenCalificaciones?> = _resumen.asStateFlow()

    private val _cargandoCalificaciones = MutableStateFlow(false)
    val cargandoCalificaciones: StateFlow<Boolean> = _cargandoCalificaciones.asStateFlow()

    private val _enviando = MutableStateFlow(false)
    val enviando: StateFlow<Boolean> = _enviando.asStateFlow()

    private val _mensajeCalificacion = MutableStateFlow<String?>(null)
    val mensajeCalificacion: StateFlow<String?> = _mensajeCalificacion.asStateFlow()

    fun cargarReceta(id: String) {
        _raciones.value = racionesBase
        _avisoTraduccion.value = null   // NUEVO: receta nueva, aviso limpio
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val resultado = recetaRepository.obtenerPorId(id)
            resultado
                .onSuccess { _receta.value = it }
                .onFailure { _errorMessage.value = "No pudimos cargar la receta." }
            _isLoading.value = false

            // NUEVO: cuando llega la traducción, cambiamos la receta por la versión en español.
            // Si falla, se queda en inglés.
            resultado.getOrNull()?.let { original ->
                traduccion.receta(original)
                    .onSuccess { traducida ->
                        if (_receta.value?.id == traducida.id) _receta.value = traducida
                    }
                    .onFailure { e ->
                        // NUEVO: aviso debajo del nombre; la receta se sigue viendo en inglés
                        if (_receta.value?.id == original.id) {
                            _avisoTraduccion.value =
                                if (esAltaDemanda(e)) "🐭 Gemini está muy ocupado ahora mismo, por eso la receta se ve en inglés. Intenta en un ratico."
                                else "No pudimos traducir la receta, por eso se ve en inglés."
                        }
                    }
            }
        }
        // NUEVO: las calificaciones se cargan aparte; si fallan, la receta igual se muestra
        cargarCalificaciones(id)

        // Escuchamos si es favorita: Room avisa solo cada vez que cambia
        favoritaJob?.cancel()
        favoritaJob = viewModelScope.launch {
            favoritaRepository.esFavorita(id).collect { _esFavorita.value = it }
        }
    }

    // NUEVO: pide al Apps Script el promedio y los comentarios (doGet)
    fun cargarCalificaciones(id: String) {
        _mensajeCalificacion.value = null
        viewModelScope.launch {
            _cargandoCalificaciones.value = true
            calificaciones.obtenerResumen(id)
                .onSuccess { _resumen.value = it }
                .onFailure { _resumen.value = null }
            _cargandoCalificaciones.value = false
        }
    }

    // NUEVO: envía la calificación al Apps Script (doPost) y refresca el resumen
    fun enviarCalificacion(estrellas: Int, comentario: String) {
        val receta = _receta.value ?: return
        viewModelScope.launch {
            _enviando.value = true
            calificaciones.enviar(receta.id, estrellas, comentario)
                .onSuccess {
                    _mensajeCalificacion.value = "¡Gracias por tu opinión!"
                    calificaciones.obtenerResumen(receta.id).onSuccess { _resumen.value = it }
                }
                .onFailure { _mensajeCalificacion.value = "No pudimos enviar tu opinión. Revisa tu conexión." }
            _enviando.value = false
        }
    }

    fun sumarRacion() {
        _raciones.value = _raciones.value + 1
    }

    fun restarRacion() {
        if (_raciones.value > 1) _raciones.value = _raciones.value - 1
    }

    // CAMBIA: ya no recibe id, usa la receta cargada
    fun cambiarFavorita() {
        val receta = _receta.value ?: return
        viewModelScope.launch {
            favoritaRepository.alternarFavorita(receta)
                .onFailure { _errorMessage.value = "No pudimos guardar la favorita." }
        }
    }

    // NUEVO: TheMealDB no trae calorías; solo mostramos las StatCard si hay datos
    fun tieneNutricion(receta: Receta): Boolean = receta.ingredientes.any { it.calorias > 0 }

    // Calorías y proteínas ajustadas al número de raciones
    fun calorias(receta: Receta, raciones: Int): Int =
        receta.ingredientes.sumOf { it.calorias } * raciones / racionesBase

    fun proteinas(receta: Receta, raciones: Int): Int =
        receta.ingredientes.sumOf { it.proteinas } * raciones / racionesBase
}