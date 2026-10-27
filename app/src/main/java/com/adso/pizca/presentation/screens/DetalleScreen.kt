package com.adso.pizca.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adso.pizca.presentation.viewmodel.DetalleViewModel
import com.adso.pizca.ui.theme.AzulCielo
import com.adso.pizca.ui.theme.Crema
import com.adso.pizca.ui.theme.Menta
import com.adso.pizca.ui.theme.RosaCoral

@Composable
fun DetalleScreen(
    recetaId: String,
    onVolver: () -> Unit,
    onCocinar: (String) -> Unit,
    viewModel: DetalleViewModel = viewModel()
) {
    // Cada vez que llega un id nuevo, cargamos esa receta
    LaunchedEffect(recetaId) { viewModel.cargarReceta(recetaId) }

    val receta by viewModel.receta.collectAsState()
    val raciones by viewModel.raciones.collectAsState()
    val favoritas by viewModel.favoritas.collectAsState()

    val r = receta
    if (r == null || r.id != recetaId) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 72.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Barra superior: volver, título y favorita
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
                Text(
                    "Detalle de receta",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { viewModel.cambiarFavorita(r.id) }) {
                    Icon(
                        if (r.id in favoritas) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorita",
                        tint = RosaCoral
                    )
                }
            }

            // "Foto" del plato (por ahora un emoji grande)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Crema, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(r.emoji, fontSize = 96.sp)
            }

            Text(r.nombre, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Etiqueta(r.pais, Menta)
                Etiqueta(r.categoria, AzulCielo)
            }

            // Raciones con botones - y +
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Raciones", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = { viewModel.restarRacion() }) {
                    Icon(Icons.Outlined.Remove, contentDescription = "Menos raciones")
                }
                Text("$raciones", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                IconButton(onClick = { viewModel.sumarRacion() }) {
                    Icon(Icons.Outlined.Add, contentDescription = "Más raciones")
                }
            }

            // Valores nutricionales
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DatoNutricional("${viewModel.calorias(r, raciones)}", "kcal", RosaCoral, Modifier.weight(1f))
                DatoNutricional("${viewModel.proteinas(r, raciones)} g", "proteína", Menta, Modifier.weight(1f))
            }

            Text("Ingredientes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            r.ingredientes.forEach { ingrediente ->
                Row(Modifier.fillMaxWidth()) {
                    Text("• ${ingrediente.nombre}", modifier = Modifier.weight(1f))
                    Text(ingrediente.medida, fontWeight = FontWeight.Bold)
                }
            }

            Text("Preparación", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            r.pasos.forEachIndexed { i, paso ->
                Text("${i + 1}. $paso")
            }
        }

        // Botón fijo abajo
        Button(
            onClick = { onCocinar(r.id) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(50)
        ) {
            Text("🐭 Cocinar con Chef Teo", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun Etiqueta(texto: String, color: Color) {
    Text(
        texto,
        modifier = Modifier
            .background(color, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelLarge
    )
}

@Composable
private fun DatoNutricional(valor: String, etiqueta: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(color, RoundedCornerShape(20.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(valor, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(etiqueta, style = MaterialTheme.typography.bodySmall)
    }
}