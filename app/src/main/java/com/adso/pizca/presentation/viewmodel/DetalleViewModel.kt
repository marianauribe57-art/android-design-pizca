package com.adso.pizca.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.adso.pizca.data.dto.Receta
import com.adso.pizca.data.repository.RecetaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DetalleViewModel : ViewModel() {

    // Las recetas de ejemplo rinden 4 raciones
    private val racionesBase = 4

    private val _receta = MutableStateFlow<Receta?>(null)
    val receta: StateFlow<Receta?> = _receta.asStateFlow()

    private val _raciones = MutableStateFlow(racionesBase)
    val raciones: StateFlow<Int> = _raciones.asStateFlow()

    // Ids de las recetas marcadas con el corazón
    private val _favoritas = MutableStateFlow<Set<String>>(emptySet())
    val favoritas: StateFlow<Set<String>> = _favoritas.asStateFlow()

    fun cargarReceta(id: String) {
        _receta.value = RecetaRepository.buscarPorId(id)
        _raciones.value = racionesBase
    }

    fun sumarRacion() {
        _raciones.value = _raciones.value + 1
    }

    fun restarRacion() {
        if (_raciones.value > 1) _raciones.value = _raciones.value - 1
    }

    fun cambiarFavorita(id: String) {
        _favoritas.value =
            if (id in _favoritas.value) _favoritas.value - id else _favoritas.value + id
    }

    // Calorías y proteínas ajustadas al número de raciones
    fun calorias(receta: Receta, raciones: Int): Int =
        receta.ingredientes.sumOf { it.calorias } * raciones / racionesBase

    fun proteinas(receta: Receta, raciones: Int): Int =
        receta.ingredientes.sumOf { it.proteinas } * raciones / racionesBase
}