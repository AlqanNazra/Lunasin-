package com.example.lunasin.theme

import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

val LunasinColorScheme = lightColorScheme(
    primary = PrimaryColor,
    onPrimary = White, // Warna teks pada tombol utama
    background = Grey99, // Warna latar belakang utama
    surface = White, // Warna background card login
    onSurface = HeaderColor, // Warna teks pada card
)
val LunasinShapes = androidx.compose.material3.Shapes(
    medium = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
)

@Composable
fun LunasinTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LunasinColorScheme,
        typography = LunasinTypography, // Pastikan typography diperbarui
        shapes = LunasinShapes,
        content = content
    )
}