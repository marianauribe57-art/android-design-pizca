package com.adso.pizca.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Tarjeta de un dato con número grande: kcal, proteína, metas diarias
@Composable
fun StatCard(
    valor: String,
    etiqueta: String,
    color: Color,
    modifier: Modifier = Modifier,
    tamanoValor: TextUnit = 24.sp
) {
    Column(
        modifier = modifier
            .background(color, RoundedCornerShape(20.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(valor, fontSize = tamanoValor, fontWeight = FontWeight.Bold)
        Text(etiqueta, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}