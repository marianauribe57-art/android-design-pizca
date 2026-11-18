package com.adso.pizca.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adso.pizca.data.dto.Receta
import com.adso.pizca.data.repository.RecetaRepositoryImpl
import com.adso.pizca.data.repository.FavoritaRepositoryImpl
import com.adso.pizca.data.repository.FavoritaRepository
import com.adso.pizca.data.repository.RecetaRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetalleViewModel(
    private val recetaRepository: RecetaRepository = RecetaRepositoryImpl(),
    private val favoritaRepository: FavoritaRepository = FavoritaRepositoryImpl()
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

    fun cargarReceta(id: String) {
        _raciones.value = racionesBase
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            recetaRepository.obtenerPorId(id)
                .onSuccess { _receta.value = it }
                .onFailure { _errorMessage.value = "No pudimos cargar la receta." }
            _isLoading.value = false
        }
        // Escuchamos si es favorita: Room avisa solo cada vez que cambia
        favoritaJob?.cancel()
        favoritaJob = viewModelScope.launch {
            favoritaRepository.esFavorita(id).collect { _esFavorita.value = it }
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