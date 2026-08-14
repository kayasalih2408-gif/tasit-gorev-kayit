package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekGreenPale
import com.example.ui.theme.SleekGreenPrimary

/**
 * Modern circular avatar for Salih Kaya with photo and vector fallback.
 */
@Composable
fun DriverAvatar(
    photoUri: String?,
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    borderWidth: Dp = 2.dp,
    borderColor: Color = Color.White,
    elevation: Dp = 2.dp,
    onClick: (() -> Unit)? = null
) {
    val clickModifier = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else Modifier

    Surface(
        shape = CircleShape,
        color = SleekGreenPale,
        border = BorderStroke(borderWidth, borderColor),
        shadowElevation = elevation,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .then(clickModifier)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (!photoUri.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photoUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Şoför Fotoğrafı",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = painterResource(id = R.drawable.ic_salih_kaya_avatar)
                )
            } else {
                // High-fidelity illustrated vector portrait of Salih Kaya
                Image(
                    painter = painterResource(id = R.drawable.ic_salih_kaya_avatar),
                    contentDescription = "Salih Kaya Profil Fotoğrafı",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
