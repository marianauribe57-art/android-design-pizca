package com.adso.pizca.data.repository

import com.adso.pizca.data.dto.PerfilUsuario
import kotlinx.coroutines.flow.Flow

interface PerfilRepository {
    fun observarPerfil(): Flow<PerfilUsuario?>    // null = todavía no ha llenado el perfil
    suspend fun guardarPerfil(perfil: PerfilUsuario): Result<Unit>
}