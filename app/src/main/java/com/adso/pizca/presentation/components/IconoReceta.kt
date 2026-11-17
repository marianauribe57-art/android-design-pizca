package com.adso.pizca.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RamenDining
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.adso.pizca.ui.theme.AzulCielo
import com.adso.pizca.ui.theme.CafeOscuro
import com.adso.pizca.ui.theme.Crema
import com.adso.pizca.ui.theme.Durazno
import com.adso.pizca.ui.theme.Menta
import com.adso.pizca.ui.theme.RosaCoral

// Cuadrito de color con ícono de plato. El color depende de la categoría.
@Composable
fun IconoReceta(categoria: String, tamano: Dp = 52.dp) {
    val color = when (categoria) {
        "Cerdo" -> RosaCoral
        "Pollo" -> Crema
        "Vegetariano" -> Menta
        "Postres" -> AzulCielo
        else -> Durazno
    }
    Box(
        modifier = Modifier
            .size(tamano)
            .background(color, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.RamenDining,
            contentDescription = null,
            tint = CafeOscuro,
            modifier = Modifier.size(tamano * 0.55f)
        )
    }
}