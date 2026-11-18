package com.adso.pizca.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adso.pizca.data.dto.Receta
import com.adso.pizca.data.repository.FakeRecetaRepository
import com.adso.pizca.data.repository.RecetaRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

// Las 3 formas de buscar (queda igual)
enum class ModoBusqueda(val titulo: String) {
    NOMBRE("Por nombre"),
    INGREDIENTES("Por ingredientes"),
    PAIS("Por país")
}

class BusquedaViewModel(
    private val repository: RecetaRepository = FakeRecetaRepository()   // Fase 2: RecetaRepositoryImpl()
) : ViewModel() {

    private val _modo = MutableStateFlow(ModoBusqueda.NOMBRE)
    val modo: StateFlow<ModoBusqueda> = _modo.asStateFlow()

    private val _texto = MutableStateFlow("")
    val texto: StateFlow<String> = _texto.asStateFlow()

    private val _ingredientes = MutableStateFlow<List<String>>(emptyList())
    val ingredientes: StateFlow<List<String>> = _ingredientes.asStateFlow()

    // NUEVO: países de la API y el elegido
    private val _paises = MutableStateFlow<List<String>>(emptyList())
    val paises: StateFlow<List<String>> = _paises.asStateFlow()

    private val _paisSeleccionado = MutableStateFlow<String?>(null)
    val paisSeleccionado: StateFlow<String?> = _paisSeleccionado.asStateFlow()

    // CAMBIA: empieza vacía, la llena el repositorio
    private val _resultados = MutableStateFlow<List<Receta>>(emptyList())
    val resultados: StateFlow<List<Receta>> = _resultados.asStateFlow()

    // NUEVO: estados de carga y error
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var busquedaJob: Job? = null

    init {
        viewModelScope.launch { repository.obtenerPaises().onSuccess { _paises.value = it } }
        actualizarResultados()
    }

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

    // NUEVO: tocar un país lo elige; tocarlo otra vez lo quita
    fun seleccionarPais(pais: String) {
        _paisSeleccionado.value = if (_paisSeleccionado.value == pais) null else pais
        actualizarResultados()
    }

    // CAMBIA: ahora es pública (la usa el botón Reintentar) y llama al repositorio
    fun actualizarResultados() {
        busquedaJob?.cancel()                    // cancela la búsqueda anterior
        busquedaJob = viewModelScope.launch {
            delay(400)                           // espera a que deje de escribir
            _isLoading.value = true
            _errorMessage.value = null

            val pais = _paisSeleccionado.value
            val resultado = when (_modo.value) {
                ModoBusqueda.NOMBRE -> repository.buscarPorNombre(_texto.value.trim())
                ModoBusqueda.INGREDIENTES ->
                    if (_ingredientes.value.isEmpty()) Result.success(emptyList())
                    else repository.buscarPorIngredientes(_ingredientes.value)
                ModoBusqueda.PAIS ->
                    if (pais == null) Result.success(emptyList())
                    else repository.buscarPorPais(pais)
            }

            resultado
                .onSuccess { _resultados.value = it }
                .onFailure { error ->
                    // si la cancelamos nosotros (el usuario siguió escribiendo) no es un error real
                    if (error !is CancellationException) {
                        _errorMessage.value = "No pudimos buscar. Revisa tu internet."
                    }
                }
            _isLoading.value = false
        }
    }
}