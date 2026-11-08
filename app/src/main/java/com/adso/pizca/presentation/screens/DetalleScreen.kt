package com.adso.pizca.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.RamenDining
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adso.pizca.presentation.components.ChefTeoImagen
import com.adso.pizca.presentation.viewmodel.DetalleViewModel
import com.adso.pizca.ui.theme.AzulCielo
import com.adso.pizca.ui.theme.Blanco
import com.adso.pizca.ui.theme.CafeOscuro
import com.adso.pizca.ui.theme.Crema
import com.adso.pizca.ui.theme.FondoPizca
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
    val esFavorita = r.id in favoritas

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Barra superior: volver, título y favorita
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BotonCircular(onClick = onVolver) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
                Text(
                    "Detalle de receta",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                BotonCircular(onClick = { viewModel.cambiarFavorita(r.id) }) {
                    Icon(
                        if (esFavorita) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorita",
                        tint = RosaCoral
                    )
                }
            }

            // Imagen del plato
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Crema, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.RamenDining,
                    contentDescription = null,
                    tint = CafeOscuro,
                    modifier = Modifier.size(84.dp)
                )
            }

            // Nombre y etiquetas
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(r.nombre, fontSize = 26.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Etiqueta(r.pais, Menta)
                    Etiqueta(r.categoria, AzulCielo)
                }
            }

            // Raciones con botones - y +
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Blanco, RoundedCornerShape(20.dp))
                    .padding(start = 16.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Raciones", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                BotonCircular(onClick = { viewModel.restarRacion() }, fondo = FondoPizca) {
                    Icon(Icons.Outlined.Remove, contentDescription = "Menos raciones")
                }
                Text(
                    "$raciones",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(min = 36.dp)
                )
                BotonCircular(onClick = { viewModel.sumarRacion() }, fondo = FondoPizca) {
                    Icon(Icons.Outlined.Add, contentDescription = "Más raciones")
                }
            }

            // Valores nutricionales
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DatoNutricional("${viewModel.calorias(r, raciones)}", "kcal", RosaCoral, Modifier.weight(1f))
                DatoNutricional("${viewModel.proteinas(r, raciones)} g", "proteína", Menta, Modifier.weight(1f))
            }

            // Ingredientes
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Ingredientes", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                r.ingredientes.forEach { ingrediente ->
                    Row(Modifier.fillMaxWidth()) {
                        Text(ingrediente.nombre, fontSize = 14.sp, modifier = Modifier.weight(1f))
                        Text(ingrediente.medida, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Preparación
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Preparación", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                r.pasos.forEachIndexed { i, paso ->
                    Text("${i + 1}. $paso", fontSize = 14.sp)
                }
            }
        }

        // Botón fijo abajo con Chef Teo
        Surface(
            onClick = { onCocinar(r.id) },
            shape = RoundedCornerShape(50),
            color = RosaCoral,
            contentColor = CafeOscuro,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(20.dp)
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChefTeoImagen(Modifier.size(width = 30.dp, height = 38.dp))
                Spacer(Modifier.width(10.dp))
                Text("Cocinar con Chef Teo", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun BotonCircular(
    onClick: () -> Unit,
    fondo: Color = Blanco,
    contenido: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = fondo,
        contentColor = CafeOscuro,
        modifier = Modifier.size(44.dp)
    ) {
        Box(contentAlignment = Alignment.Center) { contenido() }
    }
}

@Composable
private fun Etiqueta(texto: String, color: Color) {
    Text(
        texto,
        modifier = Modifier
            .background(color, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun DatoNutricional(valor: String, etiqueta: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(color, RoundedCornerShape(20.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(valor, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(etiqueta, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}