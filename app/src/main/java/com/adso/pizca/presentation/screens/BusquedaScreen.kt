package com.adso.pizca.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adso.pizca.data.dto.Receta
import com.adso.pizca.presentation.viewmodel.BusquedaViewModel
import com.adso.pizca.presentation.viewmodel.ModoBusqueda
import com.adso.pizca.ui.theme.Durazno
import com.adso.pizca.ui.theme.Menta
import com.adso.pizca.ui.theme.RosaCoral

@Composable
fun BusquedaScreen(
    onRecetaClick: (String) -> Unit,
    viewModel: BusquedaViewModel = viewModel()
) {
    val modo by viewModel.modo.collectAsState()
    val texto by viewModel.texto.collectAsState()
    val ingredientes by viewModel.ingredientes.collectAsState()
    val resultados by viewModel.resultados.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Buscar recetas", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = texto,
            onValueChange = { viewModel.cambiarTexto(it) },
            label = {
                Text(
                    when (modo) {
                        ModoBusqueda.NOMBRE -> "Nombre de la receta"
                        ModoBusqueda.INGREDIENTES -> "Escribe un ingrediente y toca +"
                        ModoBusqueda.PAIS -> "País"
                    }
                )
            },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            trailingIcon = if (modo == ModoBusqueda.INGREDIENTES) {
                {
                    IconButton(onClick = { viewModel.agregarIngrediente() }) {
                        Icon(Icons.Outlined.Add, contentDescription = "Agregar ingrediente")
                    }
                }
            } else null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                if (modo == ModoBusqueda.INGREDIENTES) viewModel.agregarIngrediente()
            }),
            shape = RoundedCornerShape(50),
            modifier = Modifier.fillMaxWidth()
        )

        // Los 3 modos de búsqueda
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ModoBusqueda.entries) { m ->
                FilterChip(
                    selected = modo == m,
                    onClick = { viewModel.cambiarModo(m) },
                    label = { Text(m.titulo) },
                    shape = RoundedCornerShape(50),
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RosaCoral)
                )
            }
        }

        // Ingredientes agregados (solo en ese modo)
        if (modo == ModoBusqueda.INGREDIENTES && ingredientes.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ingredientes) { ingrediente ->
                    InputChip(
                        selected = false,
                        onClick = { viewModel.quitarIngrediente(ingrediente) },
                        label = { Text(ingrediente) },
                        trailingIcon = {
                            Icon(Icons.Outlined.Close, contentDescription = "Quitar", Modifier.size(18.dp))
                        },
                        colors = InputChipDefaults.inputChipColors(containerColor = Menta),
                        shape = RoundedCornerShape(50)
                    )
                }
            }
        }

        // Mensaje de Chef Teo
        Card(colors = CardDefaults.cardColors(containerColor = Durazno), shape = RoundedCornerShape(20.dp)) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("🐭", fontSize = 32.sp)
                Spacer(Modifier.width(12.dp))
                Text(
                    if (resultados.isEmpty()) "Mmm… no encontré recetas. ¡Prueba con otra palabra!"
                    else "¡Encontré ${resultados.size} receta(s) para ti!"
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(resultados, key = { it.id }) { receta ->
                FilaResultado(receta = receta, onClick = { onRecetaClick(receta.id) })
            }
        }
    }
}

@Composable
private fun FilaResultado(receta: Receta, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(receta.emoji, fontSize = 32.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(receta.nombre, fontWeight = FontWeight.Bold)
                Text(receta.pais, style = MaterialTheme.typography.bodySmall)
            }
            Text("Ver ›", color = RosaCoral, fontWeight = FontWeight.Bold)
        }
    }
}