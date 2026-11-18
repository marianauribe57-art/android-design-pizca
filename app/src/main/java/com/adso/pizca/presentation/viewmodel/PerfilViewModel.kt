package com.adso.pizca.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adso.pizca.data.dto.MetaDiaria
import com.adso.pizca.data.dto.Objetivo
import com.adso.pizca.data.dto.PerfilUsuario
import com.adso.pizca.data.dto.Receta
import com.adso.pizca.data.repository.FavoritaRepository
import com.adso.pizca.data.repository.FavoritaRepositoryImpl
import com.adso.pizca.data.repository.PerfilRepository
import com.adso.pizca.data.repository.PerfilRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class PerfilViewModel(
    private val perfilRepository: PerfilRepository = PerfilRepositoryImpl(),
    favoritaRepository: FavoritaRepository = FavoritaRepositoryImpl()
) : ViewModel() {

    private val _perfil = MutableStateFlow<PerfilUsuario?>(null)
    val perfil: StateFlow<PerfilUsuario?> = _perfil.asStateFlow()

    private val _meta = MutableStateFlow<MetaDiaria?>(null)
    val meta: StateFlow<MetaDiaria?> = _meta.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // NUEVO: la lista de favoritas sale directo de Room y se actualiza sola
    val favoritas: StateFlow<List<Receta>> = favoritaRepository.observarFavoritas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // NUEVO: al abrir, cargamos el perfil guardado en Room (si existe)
    init {
        viewModelScope.launch {
            perfilRepository.observarPerfil().collect { guardado ->
                if (guardado != null) {
                    _perfil.value = guardado
                    _meta.value = calcularMeta(guardado)
                }
            }
        }
    }

    fun calcular(peso: String, estatura: String, edad: String, esMujer: Boolean, objetivo: Objetivo) {
        val p = peso.replace(',', '.').toDoubleOrNull()
        val e = estatura.replace(',', '.').toDoubleOrNull()
        val a = edad.toIntOrNull()

        if (p == null || e == null || a == null || p <= 0 || e <= 0 || a <= 0) {
            _error.value = "Revisa tus datos: escribe solo números en peso, estatura y edad."
            return
        }

        _error.value = null
        val perfil = PerfilUsuario(p, e, a, esMujer, objetivo)
        _perfil.value = perfil
        _meta.value = calcularMeta(perfil)

        // NUEVO: lo guardamos en Room
        viewModelScope.launch {
            perfilRepository.guardarPerfil(perfil)
                .onFailure { _error.value = "No pudimos guardar tu perfil. Intenta de nuevo." }
        }
    }

    // Fórmula de Mifflin-St Jeor con actividad ligera (x 1.375).
    // Es una referencia general, no reemplaza a un profesional de la salud.
    private fun calcularMeta(p: PerfilUsuario): MetaDiaria {
        val ajusteSexo = if (p.esMujer) -161 else 5
        val base = 10 * p.pesoKg + 6.25 * p.estaturaCm - 5 * p.edad + ajusteSexo
        val mantenimiento = base * 1.375

        val calorias = when (p.objetivo) {
            Objetivo.BAJAR -> mantenimiento - 400
            Objetivo.MANTENER -> mantenimiento
            Objetivo.GANAR -> mantenimiento + 300
        }
        val gramosPorKg = when (p.objetivo) {
            Objetivo.BAJAR -> 1.6
            Objetivo.MANTENER -> 1.2
            Objetivo.GANAR -> 1.8
        }
        return MetaDiaria(calorias.roundToInt(), (p.pesoKg * gramosPorKg).roundToInt())
    }
}