package com.adso.pizca.data.mapper

/**
 * Categorías y países de TheMealDB en español.
 * Son pocos y casi nunca cambian, así que no hace falta Gemini: es instantáneo y gratis.
 * OJO: la app sigue guardando el valor en inglés (TheMealDB solo entiende inglés);
 * esto se usa solo para MOSTRARLO en pantalla.
 */
object Diccionario {

    private val categorias = mapOf(
        "Beef" to "Res",
        "Breakfast" to "Desayuno",
        "Chicken" to "Pollo",
        "Dessert" to "Postres",
        "Goat" to "Cabra",
        "Lamb" to "Cordero",
        "Miscellaneous" to "Variados",
        "Pasta" to "Pasta",
        "Pork" to "Cerdo",
        "Seafood" to "Mariscos",
        "Side" to "Acompañamientos",
        "Starter" to "Entradas",
        "Vegan" to "Vegano",
        "Vegetarian" to "Vegetariano"
    )

    private val paises = mapOf(
        "Algerian" to "Argelina",
        "American" to "Estadounidense",
        "Argentinian" to "Argentina",
        "Australian" to "Australiana",
        "Belgian" to "Belga",
        "British" to "Británica",
        "Canadian" to "Canadiense",
        "Chinese" to "China",
        "Colombian" to "Colombiana",
        "Croatian" to "Croata",
        "Dutch" to "Neerlandesa",
        "Egyptian" to "Egipcia",
        "Filipino" to "Filipina",
        "French" to "Francesa",
        "Greek" to "Griega",
        "Indian" to "India",
        "Irish" to "Irlandesa",
        "Italian" to "Italiana",
        "Jamaican" to "Jamaiquina",
        "Japanese" to "Japonesa",
        "Kenyan" to "Keniana",
        "Malaysian" to "Malasia",
        "Mexican" to "Mexicana",
        "Moroccan" to "Marroquí",
        "Norwegian" to "Noruega",
        "Polish" to "Polaca",
        "Portuguese" to "Portuguesa",
        "Russian" to "Rusa",
        "Saudi Arabian" to "Saudí",
        "Slovakian" to "Eslovaca",
        "Spanish" to "Española",
        "Syrian" to "Siria",
        "Thai" to "Tailandesa",
        "Tunisian" to "Tunecina",
        "Turkish" to "Turca",
        "Ukrainian" to "Ucraniana",
        "Uruguayan" to "Uruguaya",
        "Venezulan" to "Venezolana",    // así viene escrito en TheMealDB
        "Venezuelan" to "Venezolana",
        "Vietnamese" to "Vietnamita",
        "Unknown" to "Otra"
    )

    // Si llega una que no está en la lista, se muestra tal cual (no se rompe nada)
    fun categoria(ingles: String): String = categorias[ingles] ?: ingles

    fun pais(ingles: String): String = paises[ingles] ?: ingles
}
