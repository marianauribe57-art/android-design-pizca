package com.adso.pizca.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// Tabla "traducciones": guarda lo que Gemini ya tradujo para no pedirlo dos veces.
// clave = qué se tradujo (ej: "receta:52772", "nombre:52772", "ingles:pollo")
// valor = la traducción (texto o JSON)
@Entity(tableName = "traducciones")
data class TraduccionEntity(
    @PrimaryKey val clave: String,
    val valor: String
)
