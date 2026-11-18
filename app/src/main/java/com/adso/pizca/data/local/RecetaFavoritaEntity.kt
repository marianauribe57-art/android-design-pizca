package com.adso.pizca.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.adso.pizca.data.dto.Receta

// Tabla "recetas_favoritas": una fila por cada receta con corazón
@Entity(tableName = "recetas_favoritas")
data class RecetaFavoritaEntity(
    @PrimaryKey val id: String,          // el id de TheMealDB, nunca se repite
    val nombre: String,
    val categoria: String,
    val pais: String,
    val imagenUrl: String
)

// Traductores: de la fila a nuestra Receta y al revés
fun RecetaFavoritaEntity.toReceta(): Receta =
    Receta(id = id, nombre = nombre, categoria = categoria, pais = pais, imagenUrl = imagenUrl)

fun Receta.toFavoritaEntity(): RecetaFavoritaEntity =
    RecetaFavoritaEntity(id = id, nombre = nombre, categoria = categoria, pais = pais, imagenUrl = imagenUrl)
