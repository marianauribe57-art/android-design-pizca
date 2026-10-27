package com.adso.pizca.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adso.pizca.data.dto.Mensaje
import com.adso.pizca.presentation.viewmodel.ChefViewModel
import com.adso.pizca.ui.theme.Crema
import com.adso.pizca.ui.theme.Durazno
import com.adso.pizca.ui.theme.Menta
import com.adso.pizca.ui.theme.RosaCoral

@Composable
fun ChefScreen(
    recetaId: String?,
    viewModel: ChefViewModel = viewModel()
) {
    LaunchedEffect(recetaId) { viewModel.cargarReceta(recetaId) }

    val receta by viewModel.receta.collectAsState()
    val paso by viewModel.pasoActual.collectAsState()
    val mensajes by viewModel.mensajes.collectAsState()
    var texto by remember { mutableStateOf("") }

    // Baja automáticamente al último mensaje
    val listState = rememberLazyListState()
    LaunchedEffect(mensajes.size) {
        if (mensajes.isNotEmpty()) listState.animateScrollToItem(mensajes.lastIndex)
    }

    fun enviar() {
        viewModel.enviarMensaje(texto)
        texto = ""
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Encabezado con Teo
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(64.dp).background(Durazno, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🐭", fontSize = 36.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Chef Teo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    receta?.let { "Cocinando: ${it.nombre}" } ?: "Esperando una receta",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                "● Te escucha",
                modifier = Modifier
                    .background(Menta, RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium
            )
        }

        // Tarjeta de progreso
        val r = receta
        if (r != null) {
            Card(shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Paso ${paso + 1} de ${r.pasos.size}", fontWeight = FontWeight.Bold)
                    LinearProgressIndicator(
                        progress = { (paso + 1f) / r.pasos.size },
                        modifier = Modifier.fillMaxWidth(),
                        color = RosaCoral,
                        trackColor = Crema
                    )
                    Text(r.pasos.getOrElse(paso) { "" })
                }
            }
        }

        // Conversación
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(mensajes, key = { it.id }) { mensaje ->
                BurbujaMensaje(mensaje)
            }
        }

        // Respuestas rápidas
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = { viewModel.siguientePaso() }, label = { Text("Siguiente paso") })
            AssistChip(onClick = { viewModel.decirIngredientes() }, label = { Text("Ingredientes") })
        }

        // Campo para escribirle a Teo
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it },
                placeholder = { Text("Pregúntale a Teo…") },
                singleLine = true,
                shape = RoundedCornerShape(50),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { enviar() }),
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            FilledIconButton(
                onClick = { enviar() },
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = RosaCoral)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
            }
        }
    }
}

@Composable
private fun BurbujaMensaje(mensaje: Mensaje) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (mensaje.esDeTeo) Arrangement.Start else Arrangement.End
    ) {
        Text(
            mensaje.texto,
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    if (mensaje.esDeTeo) Durazno else RosaCoral,
                    RoundedCornerShape(20.dp)
                )
                .padding(12.dp)
        )
    }
}