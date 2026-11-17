package com.adso.pizca.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adso.pizca.presentation.components.ChefTeoImagen
import com.adso.pizca.presentation.components.ChipPildora
import com.adso.pizca.presentation.components.RecetaCard
import com.adso.pizca.presentation.viewmodel.InicioViewModel
import com.adso.pizca.ui.theme.AzulCielo
import com.adso.pizca.ui.theme.Blanco
import com.adso.pizca.ui.theme.Crema
import com.adso.pizca.ui.theme.RosaCoral

@Composable
fun InicioScreen(
    onRecetaClick: (String) -> Unit,
    viewModel: InicioViewModel = viewModel()
) {
    val recetas by viewModel.recetas.collectAsState()
    val seleccionada by viewModel.categoriaSeleccionada.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Saludo con Chef Teo
        item { BannerSaludo() }

        // 2. Categorías (bajan de línea si no caben)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Categorías", fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    viewModel.categorias.forEach { categoria ->
                        ChipPildora(
                            texto = categoria,
                            seleccionado = categoria == seleccionada,
                            colorNormal = AzulCielo,
                            onClick = { viewModel.seleccionarCategoria(categoria) }
                        )
                    }
                }
            }
        }

        // 3. Título de la lista con el conteo
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recetas recomendadas", fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    if (recetas.size == 1) "1 receta" else "${recetas.size} recetas",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // 4. Tarjetas de recetas (componente reutilizable)
        items(recetas, key = { it.id }) { receta ->
            RecetaCard(receta = receta, onClick = { onRecetaClick(receta.id) })
        }
    }
}

@Composable
private fun BannerSaludo() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Crema, RoundedCornerShape(28.dp))
            .padding(start = 20.dp, top = 18.dp, bottom = 18.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "¡HOLA, CHEF!",
                modifier = Modifier
                    .background(RosaCoral, RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Text("¿Qué cocinamos hoy?", fontSize = 28.sp, lineHeight = 31.sp, fontWeight = FontWeight.Bold)
            Text("Elige una categoría o una receta.", fontSize = 14.sp)
        }
        Box(
            modifier = Modifier.size(92.dp).background(Blanco, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            ChefTeoImagen(Modifier.size(width = 70.dp, height = 90.dp))
        }
    }
}