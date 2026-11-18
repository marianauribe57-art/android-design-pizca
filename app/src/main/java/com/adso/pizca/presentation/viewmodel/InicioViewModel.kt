package com.adso.pizca.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adso.pizca.data.dto.Receta
import com.adso.pizca.data.repository.RecetaRepositoryImpl
import com.adso.pizca.data.repository.RecetaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InicioViewModel(
    private val repository: RecetaRepository = RecetaRepositoryImpl()
) : ViewModel() {

    // CAMBIA: las categorías ahora llegan de la API, por eso son StateFlow
    private val _categorias = MutableStateFlow<List<String>>(emptyList())
    val categorias: StateFlow<List<String>> = _categorias.asStateFlow()

    private val _categoriaSeleccionada = MutableStateFlow<String?>(null)
    val categoriaSeleccionada: StateFlow<String?> = _categoriaSeleccionada.asStateFlow()

    private val _recetas = MutableStateFlow<List<Receta>>(emptyList())
    val recetas: StateFlow<List<Receta>> = _recetas.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        cargarRecetas()
    }

    fun cargarRecetas() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            if (_categorias.value.isEmpty()) {
                repository.obtenerCategorias().onSuccess { _categorias.value = it }
            }

            val categoria = _categoriaSeleccionada.value
            val resultado =
                if (categoria == null) repository.obtenerRecetas()
                else repository.obtenerPorCategoria(categoria)

            resultado
                .onSuccess { lista -> _recetas.value = lista }
                .onFailure { _errorMessage.value = "No pudimos cargar las recetas. Revisa tu internet." }

            _isLoading.value = false
        }
    }

    // Tocar una categoría filtra; tocarla de nuevo muestra todas
    fun seleccionarCategoria(categoria: String) {
        _categoriaSeleccionada.value =
            if (_categoriaSeleccionada.value == categoria) null else categoria
        cargarRecetas()
    }
}