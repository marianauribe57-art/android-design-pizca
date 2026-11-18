package com.adso.pizca.data.repository

import com.adso.pizca.PizcaApp
import com.adso.pizca.data.dto.PerfilUsuario
import com.adso.pizca.data.local.PerfilDao
import com.adso.pizca.data.local.toEntity
import com.adso.pizca.data.local.toPerfil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// La "cocina" del menú PerfilRepository: guarda el perfil en Room
class PerfilRepositoryImpl(
    private val dao: PerfilDao = PizcaApp.database.perfilDao()
) : PerfilRepository {

    override fun observarPerfil(): Flow<PerfilUsuario?> =
        dao.observar().map { it?.toPerfil() }

    override suspend fun guardarPerfil(perfil: PerfilUsuario): Result<Unit> = runCatching {
        dao.guardar(perfil.toEntity())
    }
}
