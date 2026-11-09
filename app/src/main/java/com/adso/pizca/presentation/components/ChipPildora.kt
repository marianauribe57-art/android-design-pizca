package com.adso.pizca.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adso.pizca.ui.theme.Blanco
import com.adso.pizca.ui.theme.CafeOscuro
import com.adso.pizca.ui.theme.RosaCoral

// Chip redondo que se pone rosado cuando está seleccionado
@Composable
fun ChipPildora(
    texto: String,
    seleccionado: Boolean,
    colorNormal: Color = Blanco,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (seleccionado) RosaCoral else colorNormal,
        contentColor = CafeOscuro
    ) {
        Box(
            modifier = Modifier.heightIn(min = 44.dp).padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(texto, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}