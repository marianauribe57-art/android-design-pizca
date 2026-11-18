package com.adso.pizca

import com.adso.pizca.data.repository.RecetaRepositoryImpl
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class RecetaRepositoryTest {

    private val repo = RecetaRepositoryImpl()

    @Test
    fun buscarPorNombre_traeRecetas() = runBlocking {
        val resultado = repo.buscarPorNombre("Arrabiata")
        assertTrue("Falló: ${resultado.exceptionOrNull()}", resultado.isSuccess)
        assertTrue(resultado.getOrThrow().isNotEmpty())
    }

    @Test
    fun obtenerPorId_traeIngredientesYPasos() = runBlocking {
        val receta = repo.obtenerPorId("52772").getOrThrow()
        assertTrue(receta.ingredientes.isNotEmpty())
        assertTrue(receta.pasos.isNotEmpty())
    }

    @Test
    fun sinResultados_devuelveListaVacia() = runBlocking {
        val resultado = repo.buscarPorNombre("zzzzzz")
        assertTrue(resultado.isSuccess)                       // meals: null NO debe romper la app
        assertTrue(resultado.getOrThrow().isEmpty())
    }

    @Test
    fun buscarPorDosIngredientes_funciona() = runBlocking {
        val resultado = repo.buscarPorIngredientes(listOf("chicken", "garlic"))
        assertTrue("Falló: ${resultado.exceptionOrNull()}", resultado.isSuccess)
    }
}