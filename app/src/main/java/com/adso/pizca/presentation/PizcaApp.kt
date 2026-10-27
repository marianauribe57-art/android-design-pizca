package com.adso.pizca.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.adso.pizca.presentation.screens.*

// Las 4 pestañas de la barra inferior
enum class Pestana(val titulo: String, val icono: ImageVector) {
    INICIO("Inicio", Icons.Outlined.Home),
    BUSCAR("Buscar", Icons.Outlined.Search),
    CHEF("Chef", Icons.Outlined.RestaurantMenu),
    PERFIL("Perfil", Icons.Outlined.Person)
}

@Composable
fun PizcaApp() {
    // Qué pestaña está abierta
    var pestana by rememberSaveable { mutableStateOf(Pestana.INICIO) }
    // Si no es null, mostramos el Detalle de esa receta
    var recetaDetalle by rememberSaveable { mutableStateOf<String?>(null) }
    // La receta que se está cocinando con Chef Teo
    var recetaCocinando by rememberSaveable { mutableStateOf<String?>(null) }

    // El botón "atrás" del celular cierra el Detalle
    BackHandler(enabled = recetaDetalle != null) { recetaDetalle = null }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (recetaDetalle == null) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    Pestana.entries.forEach { item ->
                        NavigationBarItem(
                            selected = pestana == item,
                            onClick = { pestana = item },
                            icon = { Icon(item.icono, contentDescription = item.titulo) },
                            label = { Text(item.titulo) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            val id = recetaDetalle
            if (id != null) {
                DetalleScreen(
                    recetaId = id,
                    onVolver = { recetaDetalle = null },
                    onCocinar = { idReceta ->
                        recetaCocinando = idReceta
                        recetaDetalle = null
                        pestana = Pestana.CHEF
                    }
                )
            } else {
                when (pestana) {
                    Pestana.INICIO -> InicioScreen(onRecetaClick = { recetaDetalle = it })
                    Pestana.BUSCAR -> BusquedaScreen(onRecetaClick = { recetaDetalle = it })
                    Pestana.CHEF -> ChefScreen(recetaId = recetaCocinando)
                    Pestana.PERFIL -> PerfilScreen()
                }
            }
        }
    }
}