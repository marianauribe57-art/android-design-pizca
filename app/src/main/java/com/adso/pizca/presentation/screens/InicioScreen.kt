package com.adso.pizca.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.adso.pizca.data.dto.Receta
import com.adso.pizca.presentation.viewmodel.InicioViewModel
import com.adso.pizca.ui.theme.AzulCielo
import com.adso.pizca.ui.theme.Durazno
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
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("¡Hola, chef! 🐭", style = MaterialTheme.typography.labelLarge)
            Text(
                "¿Qué cocinamos hoy?",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        item {
            Text("Categorías", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(viewModel.categorias) { categoria ->
                    FilterChip(
                        selected = categoria == seleccionada,
                        onClick = { viewModel.seleccionarCategoria(categoria) },
                        label = { Text(categoria) },
                        shape = RoundedCornerShape(50),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = AzulCielo,
                            selectedContainerColor = RosaCoral
                        )
                    )
                }
            }
        }
        item {
            Text("Recetas recomendadas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        items(recetas, key = { it.id }) { receta ->
            TarjetaReceta(receta = receta, onClick = { onRecetaClick(receta.id) })
        }
    }
}

@Composable
private fun TarjetaReceta(receta: Receta, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Durazno)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(receta.emoji, fontSize = 40.sp)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(receta.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${receta.pais} · ${receta.categoria}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}