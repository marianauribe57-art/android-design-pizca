package com.adso.pizca.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adso.pizca.data.dto.Mensaje
import com.adso.pizca.presentation.components.ChefTeoImagen
import com.adso.pizca.presentation.viewmodel.ChefViewModel
import com.adso.pizca.ui.theme.Blanco
import com.adso.pizca.ui.theme.CafeOscuro
import com.adso.pizca.ui.theme.Crema
import com.adso.pizca.ui.theme.Durazno
import com.adso.pizca.ui.theme.Menta
import com.adso.pizca.ui.theme.RosaCoral

private val BordeSuave = Color(0xFFE3C9B8)
private val TextoSuave = Color(0xFF7E6E6E)

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
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Encabezado con Teo
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(64.dp).background(Durazno, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                ChefTeoImagen(Modifier.size(width = 46.dp, height = 60.dp))
            }
            Column(Modifier.weight(1f)) {
                Text("Chef Teo", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(
                    receta?.let { "Cocinando: ${it.nombre}" } ?: "Esperando una receta",
                    fontSize = 14.sp
                )
            }
            Text(
                "● Te escucha",
                modifier = Modifier
                    .background(Menta, RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Tarjeta de progreso
        val r = receta
        if (r != null && r.pasos.isNotEmpty()) {
            val progreso = ((paso + 1f) / r.pasos.size).coerceIn(0f, 1f)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Blanco, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Paso ${paso + 1} de ${r.pasos.size}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Crema)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progreso)
                            .clip(RoundedCornerShape(50))
                            .background(RosaCoral)
                    )
                }
                Text(r.pasos.getOrElse(paso) { "" }, fontSize = 14.sp)
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
            ChipRapido("Siguiente paso") { viewModel.siguientePaso() }
            ChipRapido("Ingredientes") { viewModel.decirIngredientes() }
        }

        // Campo para escribirle a Teo
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 52.dp)
                    .background(Blanco, RoundedCornerShape(50))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = texto,
                    onValueChange = { texto = it },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 15.sp, color = CafeOscuro),
                    cursorBrush = SolidColor(CafeOscuro),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { enviar() }),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { campo ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (texto.isEmpty()) Text("Pregúntale a Teo…", fontSize = 15.sp, color = TextoSuave)
                            campo()
                        }
                    }
                )
            }
            Surface(
                onClick = { enviar() },
                shape = CircleShape,
                color = RosaCoral,
                contentColor = CafeOscuro,
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
                }
            }
        }
    }
}

@Composable
private fun ChipRapido(texto: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = Blanco,
        contentColor = CafeOscuro,
        border = BorderStroke(1.dp, BordeSuave)
    ) {
        Box(
            modifier = Modifier.heightIn(min = 44.dp).padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(texto, fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
                .widthIn(max = 270.dp)
                .background(
                    if (mensaje.esDeTeo) Durazno else RosaCoral,
                    RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp),
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}