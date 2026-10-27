package com.adso.pizca.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.adso.pizca.data.dto.Receta
import com.adso.pizca.data.repository.RecetaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Las 3 formas de buscar
enum class ModoBusqueda(val titulo: String) {
    NOMBRE("Por nombre"),
    INGREDIENTES("Por ingredientes"),
    PAIS("Por país")
}

class BusquedaViewModel : ViewModel() {

    private val todas = RecetaRepository.obtenerRecetas()

    private val _modo = MutableStateFlow(ModoBusqueda.NOMBRE)
    val modo: StateFlow<ModoBusqueda> = _modo.asStateFlow()

    private val _texto = MutableStateFlow("")
    val texto: StateFlow<String> = _texto.asStateFlow()

    private val _ingredientes = MutableStateFlow<List<String>>(emptyList())
    val ingredientes: StateFlow<List<String>> = _ingredientes.asStateFlow()

    private val _resultados = MutableStateFlow(todas)
    val resultados: StateFlow<List<Receta>> = _resultados.asStateFlow()

    fun cambiarModo(nuevo: ModoBusqueda) {
        _modo.value = nuevo
        _texto.value = ""
        actualizarResultados()
    }

    fun cambiarTexto(nuevo: String) {
        _texto.value = nuevo
        actualizarResultados()
    }

    fun agregarIngrediente() {
        val ingrediente = _texto.value.trim()
        if (ingrediente.isNotEmpty() && ingrediente !in _ingredientes.value) {
            _ingredientes.value = _ingredientes.value + ingrediente
        }
        _texto.value = ""
        actualizarResultados()
    }

    fun quitarIngrediente(ingrediente: String) {
        _ingredientes.value = _ingredientes.value - ingrediente
        actualizarResultados()
    }

    private fun actualizarResultados() {
        val t = _texto.value.trim()
        _resultados.value = when (_modo.value) {
            ModoBusqueda.NOMBRE -> todas.filter { it.nombre.contains(t, ignoreCase = true) }
            ModoBusqueda.PAIS -> todas.filter { it.pais.contains(t, ignoreCase = true) }
            ModoBusqueda.INGREDIENTES ->
                if (_ingredientes.value.isEmpty()) todas
                else todas.filter { receta ->
                    receta.ingredientes.any { ing ->
                        _ingredientes.value.any { ing.nombre.contains(it, ignoreCase = true) }
                    }
                }
        }
    }
}