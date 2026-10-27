package com.adso.pizca.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Pantalla temporal mientras cada integrante construye la suya
@Composable
fun PantallaEnConstruccion(
    titulo: String,
    responsable: String,
    acciones: @Composable ColumnScope.() -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
    ) {
        Text("🐭👨‍🍳", style = MaterialTheme.typography.displayMedium)
        Text(titulo, style = MaterialTheme.typography.headlineMedium)
        Text("En construcción · $responsable", style = MaterialTheme.typography.bodyMedium)
        acciones()
    }
}