package com.adso.pizca.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adso.pizca.data.dto.Receta
import com.adso.pizca.presentation.components.ChefTeoImagen
import com.adso.pizca.presentation.components.IconoReceta
import com.adso.pizca.presentation.viewmodel.BusquedaViewModel
import com.adso.pizca.presentation.viewmodel.ModoBusqueda
import com.adso.pizca.ui.theme.Blanco
import com.adso.pizca.ui.theme.CafeOscuro
import com.adso.pizca.ui.theme.Durazno
import com.adso.pizca.ui.theme.Menta
import com.adso.pizca.ui.theme.RosaCoral

private val TextoSuave = Color(0xFF7E6E6E)

@Composable
fun BusquedaScreen(
    onRecetaClick: (String) -> Unit,
    viewModel: BusquedaViewModel = viewModel()
) {
    val modo by viewModel.modo.collectAsState()
    val texto by viewModel.texto.collectAsState()
    val ingredientes by viewModel.ingredientes.collectAsState()
    val resultados by viewModel.resultados.collectAsState()

    val etiqueta = when (modo) {
        ModoBusqueda.NOMBRE -> "Nombre de la receta"
        ModoBusqueda.INGREDIENTES -> "Escribe un ingrediente y toca +"
        ModoBusqueda.PAIS -> "País"
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(start = 20.dp, end = 20.dp, top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Buscar recetas", fontSize = 26.sp, fontWeight = FontWeight.Bold)

        // Buscador blanco tipo píldora
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .background(Blanco, RoundedCornerShape(50))
                .padding(start = 16.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Outlined.Search, contentDescription = null, modifier = Modifier.size(20.dp))
            BasicTextField(
                value = texto,
                onValueChange = { viewModel.cambiarTexto(it) },
                singleLine = true,
                textStyle = TextStyle(fontSize = 15.sp, color = CafeOscuro),
                cursorBrush = SolidColor(CafeOscuro),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (modo == ModoBusqueda.INGREDIENTES) viewModel.agregarIngrediente()
                }),
                modifier = Modifier.weight(1f),
                decorationBox = { campo ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (texto.isEmpty()) Text(etiqueta, fontSize = 15.sp, color = TextoSuave)
                        campo()
                    }
                }
            )
            if (modo == ModoBusqueda.INGREDIENTES) {
                Surface(
                    onClick = { viewModel.agregarIngrediente() },
                    shape = CircleShape,
                    color = RosaCoral,
                    contentColor = CafeOscuro,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Add, contentDescription = "Agregar ingrediente")
                    }
                }
            }
        }

        // Los 3 modos de búsqueda
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ModoBusqueda.entries.forEach { m ->
                ChipModo(texto = m.titulo, seleccionado = modo == m, onClick = { viewModel.cambiarModo(m) })
            }
        }

        // Ingredientes agregados (solo en ese modo)
        if (modo == ModoBusqueda.INGREDIENTES && ingredientes.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ingredientes.forEach { ingrediente ->
                    ChipIngrediente(nombre = ingrediente, onQuitar = { viewModel.quitarIngrediente(ingrediente) })
                }
            }
        }

        // Mensaje de Chef Teo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Durazno, RoundedCornerShape(22.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ChefTeoImagen(Modifier.size(width = 40.dp, height = 52.dp))
            Text(
                if (resultados.isEmpty()) "Mmm… no encontré recetas. ¡Prueba con otra palabra!"
                else "¡Encontré ${resultados.size} receta(s) para ti!",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Resultados
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(resultados, key = { it.id }) { receta ->
                FilaResultado(receta = receta, onClick = { onRecetaClick(receta.id) })
            }
        }
    }
}

@Composable
private fun ChipModo(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (seleccionado) RosaCoral else Blanco,
        contentColor = CafeOscuro
    ) {
        Box(
            modifier = Modifier.heightIn(min = 44.dp).padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(texto, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ChipIngrediente(nombre: String, onQuitar: () -> Unit) {
    Surface(
        onClick = onQuitar,
        shape = RoundedCornerShape(50),
        color = Menta,
        contentColor = CafeOscuro
    ) {
        Row(
            modifier = Modifier.heightIn(min = 40.dp).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(nombre, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Icon(Icons.Outlined.Close, contentDescription = "Quitar $nombre", modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun FilaResultado(receta: Receta, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Blanco,
        contentColor = CafeOscuro
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconoReceta(receta.categoria, 46.dp)
            Column(Modifier.weight(1f)) {
                Text(receta.nombre, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(receta.pais, fontSize = 13.sp)
            }
            Text("Ver ›", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}