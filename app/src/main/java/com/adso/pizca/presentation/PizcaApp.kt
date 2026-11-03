package com.adso.pizca.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.adso.pizca.presentation.screens.*
import kotlin.reflect.KClass

// Las 4 pestañas de la barra inferior, cada una con su ruta
enum class Pestana(
    val titulo: String,
    val icono: ImageVector,
    val ruta: Any,
    val clase: KClass<*>
) {
    INICIO("Inicio", Icons.Outlined.Home, InicioRuta, InicioRuta::class),
    BUSCAR("Buscar", Icons.Outlined.Search, BusquedaRuta, BusquedaRuta::class),
    CHEF("Chef", Icons.Outlined.RestaurantMenu, ChefRuta(), ChefRuta::class),
    PERFIL("Perfil", Icons.Outlined.Person, PerfilRuta, PerfilRuta::class)
}

@Composable
fun PizcaApp() {
    // 1. El "GPS" de la app
    val navController = rememberNavController()

    // 2. Saber en qué pantalla estamos (para marcar la pestaña y ocultar la barra)
    val entradaActual by navController.currentBackStackEntryAsState()
    val destinoActual = entradaActual?.destination
    val mostrarBarra = destinoActual?.hasRoute(DetalleRuta::class) != true

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (mostrarBarra) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    Pestana.entries.forEach { pestana ->
                        val seleccionada =
                            destinoActual?.hierarchy?.any { it.hasRoute(pestana.clase) } == true
                        NavigationBarItem(
                            selected = seleccionada,
                            onClick = {
                                navController.navigate(pestana.ruta) {
                                    // Evita apilar pestañas repetidas y recuerda cada una
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(pestana.icono, contentDescription = pestana.titulo) },
                            label = { Text(pestana.titulo) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        // 3. El mapa de pantallas: cada ruta con su pantalla
        NavHost(
            navController = navController,
            startDestination = InicioRuta,
            modifier = Modifier.padding(padding)
        ) {
            composable<InicioRuta> {
                InicioScreen(onRecetaClick = { id -> navController.navigate(DetalleRuta(id)) })
            }

            composable<BusquedaRuta> {
                BusquedaScreen(onRecetaClick = { id -> navController.navigate(DetalleRuta(id)) })
            }

            composable<DetalleRuta> { entrada ->
                // toRoute() recupera el recetaId que viajó en la ruta
                val ruta: DetalleRuta = entrada.toRoute()
                DetalleScreen(
                    recetaId = ruta.recetaId,
                    onVolver = { navController.popBackStack() },
                    onCocinar = { id ->
                        navController.navigate(ChefRuta(id)) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable<ChefRuta> { entrada ->
                val ruta: ChefRuta = entrada.toRoute()
                ChefScreen(recetaId = ruta.recetaId)
            }

            composable<PerfilRuta> {
                PerfilScreen()
            }
        }
    }
}