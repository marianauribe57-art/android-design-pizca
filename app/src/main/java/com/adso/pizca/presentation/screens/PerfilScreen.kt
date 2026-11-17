package com.adso.pizca.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adso.pizca.data.dto.Objetivo
import com.adso.pizca.presentation.components.ChipPildora
import com.adso.pizca.presentation.components.StatCard
import com.adso.pizca.presentation.viewmodel.PerfilViewModel
import com.adso.pizca.ui.theme.Blanco
import com.adso.pizca.ui.theme.CafeOscuro
import com.adso.pizca.ui.theme.Crema
import com.adso.pizca.ui.theme.Durazno
import com.adso.pizca.ui.theme.Menta
import com.adso.pizca.ui.theme.RosaCoral

private val RojoError = Color(0xFFA12B2B)

@Composable
fun PerfilScreen(viewModel: PerfilViewModel = viewModel()) {
    val meta by viewModel.meta.collectAsState()
    val error by viewModel.error.collectAsState()

    // Si ya se calculó antes, los campos arrancan con esos datos
    val guardado by viewModel.perfil.collectAsState()
    var peso by rememberSaveable { mutableStateOf(guardado?.pesoKg?.let { numeroTexto(it) } ?: "") }
    var estatura by rememberSaveable { mutableStateOf(guardado?.estaturaCm?.let { numeroTexto(it) } ?: "") }
    var edad by rememberSaveable { mutableStateOf(guardado?.edad?.toString() ?: "") }
    var esMujer by rememberSaveable { mutableStateOf(guardado?.esMujer ?: true) }
    var objetivo by rememberSaveable { mutableStateOf(guardado?.objetivo ?: Objetivo.MANTENER) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Saludo
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(56.dp).background(Durazno, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Person, contentDescription = null, modifier = Modifier.size(28.dp))
            }
            Column {
                Text("¡Hola!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Nutrición personalizada con Pizca", fontSize = 14.sp)
            }
        }

        // Mis datos: los 3 campos en una sola fila
        Text("Mis datos", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CampoNumero(peso, { peso = it }, "Peso (kg)", KeyboardType.Decimal, Modifier.weight(1f))
            CampoNumero(estatura, { estatura = it }, "Estatura (cm)", KeyboardType.Decimal, Modifier.weight(1f))
            CampoNumero(edad, { edad = it }, "Edad", KeyboardType.Number, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChipPildora("Mujer", esMujer) { esMujer = true }
            ChipPildora("Hombre", !esMujer) { esMujer = false }
        }

        // Objetivo (baja de línea si no cabe)
        Text("Mi objetivo", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Objetivo.entries.forEach { o ->
                ChipPildora(o.titulo, objetivo == o) { objetivo = o }
            }
        }

        Button(
            onClick = { viewModel.calcular(peso, estatura, edad, esMujer, objetivo) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            shape = RoundedCornerShape(50)
        ) {
            Text("Calcular mi meta diaria", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        }

        error?.let { Text(it, color = RojoError, fontSize = 14.sp, fontWeight = FontWeight.Bold) }

        // Resultado
        meta?.let { m ->
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Mi meta diaria", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("${m.calorias}", "kcal al día", RosaCoral, Modifier.weight(1f), tamanoValor = 26.sp)
                    StatCard("${m.proteinas} g", "proteína al día", Menta, Modifier.weight(1f), tamanoValor = 26.sp)
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Crema, RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Tu plato ideal (por comida)", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text("• ½ plato de verduras", fontSize = 14.sp)
                    Text("• ¼ de proteína (≈ ${m.proteinas / 3} g de proteína)", fontSize = 14.sp)
                    Text("• ¼ de carbohidratos (arroz, papa o pasta)", fontSize = 14.sp)
                    Text("≈ ${m.calorias / 3} kcal por comida, si haces 3 comidas al día.", fontSize = 14.sp)
                }
                Text(
                    "Estos valores son una referencia general y no reemplazan la recomendación de un profesional de la salud.",
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun CampoNumero(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    teclado: KeyboardType,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Blanco, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(etiqueta, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        BasicTextField(
            value = valor,
            onValueChange = onCambio,
            singleLine = true,
            textStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = CafeOscuro),
            cursorBrush = SolidColor(CafeOscuro),
            keyboardOptions = KeyboardOptions(keyboardType = teclado),
            modifier = Modifier.fillMaxWidth().heightIn(min = 28.dp)
        )
    }
}

private fun numeroTexto(n: Double): String =
    if (n % 1.0 == 0.0) n.toInt().toString() else n.toString()