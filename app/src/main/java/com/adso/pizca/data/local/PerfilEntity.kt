package com.adso.pizca.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.adso.pizca.data.dto.Objetivo
import com.adso.pizca.data.dto.PerfilUsuario

// Tabla "perfil": hay un solo usuario, así que siempre usamos la fila 1
@Entity(tableName = "perfil")
data class PerfilEntity(
    @PrimaryKey val id: Int = 1,
    val pesoKg: Double,
    val estaturaCm: Double,
    val edad: Int,
    val esMujer: Boolean,
    val objetivo: String                 // el nombre del enum: "BAJAR", "MANTENER" o "GANAR"
)

// Traductores: de la fila a nuestro PerfilUsuario y al revés
fun PerfilEntity.toPerfil(): PerfilUsuario =
    PerfilUsuario(pesoKg, estaturaCm, edad, esMujer, Objetivo.valueOf(objetivo))

fun PerfilUsuario.toEntity(): PerfilEntity =
    PerfilEntity(pesoKg = pesoKg, estaturaCm = estaturaCm, edad = edad, esMujer = esMujer, objetivo = objetivo.name)
