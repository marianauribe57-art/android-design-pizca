package com.adso.pizca.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.adso.pizca.data.dto.Receta
import com.adso.pizca.data.repository.RecetaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class InicioViewModel : ViewModel() {

    private val todas = RecetaRepository.obtenerRecetas()

    // Las categorías salen de las recetas, sin repetir
    val categorias: List<String> = todas.map { it.categoria }.distinct()

    private val _categoriaSeleccionada = MutableStateFlow<String?>(null)
    val categoriaSeleccionada: StateFlow<String?> = _categoriaSeleccionada.asStateFlow()

    private val _recetas = MutableStateFlow(todas)
    val recetas: StateFlow<List<Receta>> = _recetas.asStateFlow()

    // Tocar una categoría filtra; tocarla de nuevo muestra todas
    fun seleccionarCategoria(categoria: String) {
        val nueva = if (_categoriaSeleccionada.value == categoria) null else categoria
        _categoriaSeleccionada.value = nueva
        _recetas.value = if (nueva == null) todas else todas.filter { it.categoria == nueva }
    }
}