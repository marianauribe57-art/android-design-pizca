
package com.adso.pizca.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
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
    val ruta: Destination,
    val clase: KClass<*>
) {
    INICIO("Inicio", Icons.Outlined.Home, Destination.Inicio, Destination.Inicio::class),
    BUSCAR("Buscar", Icons.Outlined.Search, Destination.Busqueda, Destination.Busqueda::class),
    CHEF("Chef", Icons.Outlined.RestaurantMenu, Destination.Chef(), Destination.Chef::class),
    PERFIL("Perfil", Icons.Outlined.Person, Destination.Perfil, Destination.Perfil::class)
}

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    // Saber en qué pantalla estamos (para marcar la pestaña y ocultar la barra)
    val entradaActual by navController.currentBackStackEntryAsState()
    val destinoActual = entradaActual?.destination
    val mostrarBarra = destinoActual?.hasRoute(Destination.Detalle::class) != true

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (mostrarBarra) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                ) {
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
        // El mapa de pantallas: cada ruta con su pantalla
        NavHost(
            navController = navController,
            startDestination = Destination.Inicio,
            modifier = Modifier.padding(padding)
        ) {
            // Pantalla 1: Inicio
            composable<Destination.Inicio> {
                InicioScreen(onRecetaClick = { id -> navController.navigate(Destination.Detalle(id)) })
            }

            // Pantalla 2: Búsqueda
            composable<Destination.Busqueda> {
                BusquedaScreen(onRecetaClick = { id -> navController.navigate(Destination.Detalle(id)) })
            }

            // Pantalla 3: Detalle (recibe el recetaId con toRoute)
            composable<Destination.Detalle> { entrada ->
                val ruta: Destination.Detalle = entrada.toRoute()
                DetalleScreen(
                    recetaId = ruta.recetaId,
                    onVolver = { navController.popBackStack() },
                    onCocinar = { id ->
                        navController.navigate(Destination.Chef(id)) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // Pantalla 4: Chef Teo (recetaId opcional)
            composable<Destination.Chef> { entrada ->
                val ruta: Destination.Chef = entrada.toRoute()
                ChefScreen(recetaId = ruta.recetaId)
            }

            // Pantalla 5: Perfil
            composable<Destination.Perfil> {
                PerfilScreen()
            }
        }
    }
}