package com.adso.pizca.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Asignamos los colores de Pizca a los "roles" de Material Design
private val PizcaColorScheme = lightColorScheme(
    primary = RosaCoral,
    onPrimary = CafeOscuro,
    secondary = Durazno,
    onSecondary = CafeOscuro,
    tertiary = Menta,
    onTertiary = CafeOscuro,
    background = FondoPizca,
    onBackground = CafeOscuro,
    surface = Blanco,
    onSurface = CafeOscuro,
    surfaceVariant = Crema,
    onSurfaceVariant = CafeOscuro
)

@Composable
fun PizcaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PizcaColorScheme,
        typography = Typography,
        content = content
    )
}