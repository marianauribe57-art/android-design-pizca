package com.adso.pizca.data.dto

// Un mensaje del chat: lo escribe Teo o el usuario
data class Mensaje(
    val id: Long,
    val texto: String,
    val esDeTeo: Boolean
)