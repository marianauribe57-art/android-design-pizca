package com.adso.pizca.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adso.pizca.data.dto.ComentarioDto
import com.adso.pizca.data.dto.ResumenCalificaciones
import com.adso.pizca.ui.theme.Blanco
import com.adso.pizca.ui.theme.CafeOscuro
import com.adso.pizca.ui.theme.Durazno
import com.adso.pizca.ui.theme.FondoPizca
import com.adso.pizca.ui.theme.RosaCoral

private val Gris = Color(0xFF7E6E6E)

// 1. Resumen bajo el título: "⭐ 4.5 · 12 opiniones ›". Al tocarlo se abren los comentarios.
@Composable
fun ResumenCalificacion(
    resumen: ResumenCalificaciones?,
    cargando: Boolean,
    onVerComentarios: () -> Unit
) {
    val texto = when {
        cargando && resumen == null -> "Cargando calificaciones…"
        resumen == null -> "Calificaciones no disponibles"
        resumen.total == 0 -> "Sin calificaciones todavía"
        resumen.total == 1 -> "${resumen.promedio} · 1 opinión"
        else -> "${resumen.promedio} · ${resumen.total} opiniones"
    }
    val hayComentarios = (resumen?.total ?: 0) > 0
    Row(
        modifier = Modifier
            .clickable(enabled = hayComentarios, onClick = onVerComentarios)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(Icons.Filled.Star, contentDescription = null, tint = RosaCoral, modifier = Modifier.size(20.dp))
        Text(texto, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = CafeOscuro)
        if (hayComentarios) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Ver comentarios", tint = CafeOscuro)
        }
    }
}

// 2. Formulario al final de la receta: estrellas + comentario + botón Enviar
@Composable
fun FormularioCalificacion(
    enviando: Boolean,
    mensaje: String?,
    onEnviar: (estrellas: Int, comentario: String) -> Unit
) {
    var estrellas by rememberSaveable { mutableIntStateOf(0) }
    var comentario by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Blanco, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("¿Ya la preparaste?", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        FilaEstrellas(seleccionadas = estrellas, onElegir = { estrellas = it })
        OutlinedTextField(
            value = comentario,
            onValueChange = { if (it.length <= 300) comentario = it },
            placeholder = { Text("Escribe un comentario (opcional)") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 4,
            shape = RoundedCornerShape(16.dp)
        )
        Button(
            onClick = {
                onEnviar(estrellas, comentario)
                estrellas = 0
                comentario = ""
            },
            enabled = estrellas > 0 && !enviando,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = RosaCoral, contentColor = CafeOscuro),
            shape = RoundedCornerShape(50)
        ) {
            Text(if (enviando) "Enviando…" else "Enviar opinión", fontWeight = FontWeight.SemiBold)
        }
        if (mensaje != null) Text(mensaje, fontSize = 14.sp, color = Gris)
    }
}

@Composable
private fun FilaEstrellas(seleccionadas: Int, onElegir: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (i in 1..5) {
            IconButton(onClick = { onElegir(i) }) {
                Icon(
                    if (i <= seleccionadas) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "$i estrellas",
                    tint = RosaCoral,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

// 3. Hoja que sube desde abajo con los comentarios
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComentariosSheet(resumen: ResumenCalificaciones, onCerrar: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onCerrar, containerColor = FondoPizca) {
        Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Calificaciones", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Icon(Icons.Filled.Star, contentDescription = null, tint = RosaCoral)
                Text("${resumen.promedio}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            if (resumen.comentarios.isEmpty()) {
                Text("Todavía nadie ha dejado un comentario.", color = Gris)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(resumen.comentarios) { TarjetaComentario(it) }
                }
            }
        }
    }
}

@Composable
private fun TarjetaComentario(c: ComentarioDto) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Durazno, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row {
            repeat(c.estrellas) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = CafeOscuro, modifier = Modifier.size(16.dp))
            }
        }
        Text("\"${c.comentario}\"", fontSize = 15.sp)
        Text(c.fecha, fontSize = 12.sp, color = Gris)
    }
}
