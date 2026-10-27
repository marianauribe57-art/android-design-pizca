package com.adso.pizca.data.repository

import com.adso.pizca.data.dto.Ingrediente
import com.adso.pizca.data.dto.Receta

// Por ahora las recetas están escritas aquí.
// Más adelante este repositorio las traerá de TheMealDB con Retrofit.
object RecetaRepository {

    fun obtenerRecetas(): List<Receta> = listOf(
        Receta(
            id = "1",
            nombre = "Tacos al Pastor",
            categoria = "Cerdo",
            pais = "México",
            emoji = "🌮",
            ingredientes = listOf(
                Ingrediente("Cerdo", "500 g", 1200, 130),
                Ingrediente("Piña", "1 taza", 80, 1),
                Ingrediente("Tortillas", "8 unidades", 480, 12),
                Ingrediente("Cebolla", "1 unidad", 45, 1)
            ),
            pasos = listOf(
                "Marina la carne con achiote y piña.",
                "Calienta la sartén a fuego medio-alto.",
                "Dora la carne en tandas.",
                "Calienta las tortillas y arma los tacos."
            )
        ),
        Receta(
            id = "2",
            nombre = "Arroz con Pollo",
            categoria = "Pollo",
            pais = "Perú",
            emoji = "🍚",
            ingredientes = listOf(
                Ingrediente("Pollo", "4 presas", 900, 110),
                Ingrediente("Arroz", "2 tazas", 1300, 26),
                Ingrediente("Tomate", "2 unidades", 40, 2),
                Ingrediente("Arveja", "1 taza", 120, 8)
            ),
            pasos = listOf(
                "Dora las presas de pollo.",
                "Sofríe el tomate y la cebolla.",
                "Agrega el arroz y el caldo.",
                "Cocina tapado 20 minutos."
            )
        ),
        Receta(
            id = "3",
            nombre = "Pasta Pomodoro",
            categoria = "Vegetariano",
            pais = "Italia",
            emoji = "🍝",
            ingredientes = listOf(
                Ingrediente("Pasta", "250 g", 900, 32),
                Ingrediente("Tomate", "4 unidades", 80, 4),
                Ingrediente("Ajo", "2 dientes", 10, 0),
                Ingrediente("Albahaca", "1 manojo", 5, 0)
            ),
            pasos = listOf(
                "Cocina la pasta en agua con sal.",
                "Sofríe el ajo y agrega el tomate.",
                "Mezcla la pasta con la salsa.",
                "Sirve con albahaca fresca."
            )
        ),
        Receta(
            id = "4",
            nombre = "Brownie de Chocolate",
            categoria = "Postres",
            pais = "Estados Unidos",
            emoji = "🍫",
            ingredientes = listOf(
                Ingrediente("Chocolate", "200 g", 1100, 10),
                Ingrediente("Huevo", "3 unidades", 210, 18),
                Ingrediente("Azúcar", "1 taza", 770, 0),
                Ingrediente("Harina", "1/2 taza", 230, 6)
            ),
            pasos = listOf(
                "Derrite el chocolate.",
                "Bate los huevos con el azúcar.",
                "Mezcla todo con la harina.",
                "Hornea 25 minutos a 180 °C."
            )
        )
    )

    fun buscarPorId(id: String): Receta? =
        obtenerRecetas().find { it.id == id }
}