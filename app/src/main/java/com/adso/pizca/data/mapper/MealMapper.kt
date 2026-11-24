package com.adso.pizca.data.mapper

import com.adso.pizca.data.dto.Ingrediente
import com.adso.pizca.data.dto.MealDto
import com.adso.pizca.data.dto.Receta

/**
 * Convierte una receta de TheMealDB en una Receta de Pizca.
 * categoriaPorDefecto: filter.php no trae la categoría, pero nosotros sí la sabemos.
 */
fun MealDto.toReceta(categoriaPorDefecto: String = ""): Receta {

    // 1. Los 20 pares (ingrediente, medida)
    val pares = listOf(
        strIngredient1 to strMeasure1, strIngredient2 to strMeasure2, strIngredient3 to strMeasure3,
        strIngredient4 to strMeasure4, strIngredient5 to strMeasure5, strIngredient6 to strMeasure6,
        strIngredient7 to strMeasure7, strIngredient8 to strMeasure8, strIngredient9 to strMeasure9,
        strIngredient10 to strMeasure10, strIngredient11 to strMeasure11, strIngredient12 to strMeasure12,
        strIngredient13 to strMeasure13, strIngredient14 to strMeasure14, strIngredient15 to strMeasure15,
        strIngredient16 to strMeasure16, strIngredient17 to strMeasure17, strIngredient18 to strMeasure18,
        strIngredient19 to strMeasure19, strIngredient20 to strMeasure20
    )

    // 2. Nos quedamos solo con los que tienen nombre
    val ingredientes = pares.mapNotNull { (nombre, medida) ->
        if (nombre.isNullOrBlank()) null
        else Ingrediente(nombre = nombre.trim(), medida = medida?.trim().orEmpty())
    }

    // 3. Las instrucciones vienen en un solo texto: lo partimos por saltos de línea
    //    y quitamos líneas vacías o que solo dicen "STEP 1", "1", "2." o "3)"
    val pasos = strInstructions.orEmpty()
        .split(Regex("\\r?\\n"))
        .map { it.trim() }
        .filter { it.isNotBlank() && !it.matches(Regex("(?i)(step\\s*)?\\d+[.):]?")) }   // CAMBIA

    return Receta(
        id = idMeal,
        nombre = strMeal,
        categoria = strCategory ?: categoriaPorDefecto,
        pais = strArea.orEmpty(),
        imagenUrl = strMealThumb.orEmpty(),
        ingredientes = ingredientes,
        pasos = pasos
    )
}