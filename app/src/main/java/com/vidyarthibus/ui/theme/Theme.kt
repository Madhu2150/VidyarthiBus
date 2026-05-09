package com.vidyarthibus.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Dark color scheme using #011C31 as base
private val DarkColorScheme = darkColorScheme(
    primary          = BrandBlueLight,
    onPrimary        = Color(0xFF011C31),
    primaryContainer = Color(0xFF023560),
    onPrimaryContainer = Color(0xFFD0E4FF),
    secondary        = CrowdGreen,
    onSecondary      = Color.White,
    background       = AppBackground,       // #011C31
    onBackground     = OnSurface,
    surface          = Surface,             // slightly lighter
    onSurface        = OnSurface,
    surfaceVariant   = SurfaceVariant,
    onSurfaceVariant = OnSurfaceDim,
    error            = CrowdRed,
    onError          = Color.White
)

// Light color scheme (fallback)
private val LightColorScheme = lightColorScheme(
    primary          = BrandBlue,
    onPrimary        = Color.White,
    primaryContainer = BrandBlueLight,
    secondary        = CrowdGreen,
    background       = AppBackground,       // still use dark background
    onBackground     = OnSurface,
    surface          = Surface,
    onSurface        = OnSurface,
    error            = CrowdRed
)

@Composable
fun VidyarthiBusTheme(
    darkTheme: Boolean = true,              // default to dark
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme      // always use dark for #011C31 bg

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}