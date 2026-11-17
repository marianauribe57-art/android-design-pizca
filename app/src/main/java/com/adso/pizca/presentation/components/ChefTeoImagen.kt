package com.adso.pizca.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.adso.pizca.R

// La imagen de Chef Teo (res/drawable/chef_teo.png)
@Composable
fun ChefTeoImagen(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.chef_teo),
        contentDescription = "Chef Teo",
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}