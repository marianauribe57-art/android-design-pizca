package com.adso.pizca.data.dto

// Un ingrediente dentro de una receta (ej: "Pollo", "500 g")
data class Ingrediente(
    val nombre: String,
    val medida: String,
    val calorias: Int,
    val proteinas: Int
)

// Una receta completa. Los campos siguen la idea de TheMealDB
data class Receta(
    val id: String,
    val nombre: String,
    val categoria: String,
    val pais: String,
    val emoji: String,                    // imagen temporal mientras no usamos fotos
    val ingredientes: List<Ingrediente>,
    val pasos: List<String>
)