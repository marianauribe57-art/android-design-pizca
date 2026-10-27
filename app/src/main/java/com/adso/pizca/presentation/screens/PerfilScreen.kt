package com.adso.pizca.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adso.pizca.data.dto.Objetivo
import com.adso.pizca.presentation.viewmodel.PerfilViewModel
import com.adso.pizca.ui.theme.Crema
import com.adso.pizca.ui.theme.Durazno
import com.adso.pizca.ui.theme.Menta
import com.adso.pizca.ui.theme.RosaCoral

@Composable
fun PerfilScreen(viewModel: PerfilViewModel = viewModel()) {
    val meta by viewModel.meta.collectAsState()
    val error by viewModel.error.collectAsState()


    // Si ya se calculó antes, los campos arrancan con esos datos
    val guardado = viewModel.perfil.value
    var peso by rememberSaveable { mutableStateOf(guardado?.pesoKg?.let { numeroTexto(it) } ?: "") }
    var estatura by rememberSaveable { mutableStateOf(guardado?.estaturaCm?.let { numeroTexto(it) } ?: "") }
    var edad by rememberSaveable { mutableStateOf(guardado?.edad?.toString() ?: "") }
    var esMujer by rememberSaveable { mutableStateOf(guardado?.esMujer ?: true) }
    var objetivo by rememberSaveable { mutableStateOf(guardado?.objetivo ?: Objetivo.MANTENER) }

    val colorChip = FilterChipDefaults.filterChipColors(selectedContainerColor = RosaCoral)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Saludo
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(64.dp).background(Durazno, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("👩‍🍳", fontSize = 32.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("¡Hola! 👋", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Nutrición personalizada con Pizca", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Text("Mis datos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CampoNumero(peso, { peso = it }, "Peso (kg)", Modifier.weight(1f))
            CampoNumero(estatura, { estatura = it }, "Estatura (cm)", Modifier.weight(1f))
        }
        CampoNumero(edad, { edad = it }, "Edad", Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = esMujer, onClick = { esMujer = true }, label = { Text("Mujer") }, colors = colorChip)
            FilterChip(selected = !esMujer, onClick = { esMujer = false }, label = { Text("Hombre") }, colors = colorChip)
        }

        Text("Mi objetivo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(Objetivo.entries) { o ->
                FilterChip(
                    selected = objetivo == o,
                    onClick = { objetivo = o },
                    label = { Text(o.titulo) },
                    colors = colorChip
                )
            }
        }

        Button(
            onClick = { viewModel.calcular(peso, estatura, edad, esMujer, objetivo) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50)
        ) {
            Text("Calcular mi meta diaria", fontWeight = FontWeight.Bold)
        }

        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        // Resultado
        meta?.let { m ->
            Text("Mi meta diaria", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaMeta("${m.calorias}", "kcal al día", RosaCoral, Modifier.weight(1f))
                TarjetaMeta("${m.proteinas} g", "proteína al día", Menta, Modifier.weight(1f))
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = Crema),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("🍽️ Tu plato ideal (por comida)", fontWeight = FontWeight.Bold)
                    Text("• ½ plato de verduras")
                    Text("• ¼ de proteína (≈ ${m.proteinas / 3} g de proteína)")
                    Text("• ¼ de carbohidratos (arroz, papa o pasta)")
                    Text("≈ ${m.calorias / 3} kcal por comida, si haces 3 comidas al día.")
                }
            }
            Text(
                "Estos valores son una referencia general y no reemplazan la recomendación de un profesional de la salud.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun CampoNumero(valor: String, onCambio: (String) -> Unit, etiqueta: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    )
}

@Composable
private fun TarjetaMeta(valor: String, etiqueta: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(color, RoundedCornerShape(20.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(valor, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(etiqueta, style = MaterialTheme.typography.bodySmall)
    }
}

private fun numeroTexto(n: Double): String =
    if (n % 1.0 == 0.0) n.toInt().toString() else n.toString()