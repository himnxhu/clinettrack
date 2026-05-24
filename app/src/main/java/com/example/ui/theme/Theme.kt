package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
      primary = Color(0xFF64B5F6), // Premium light blue accent
      secondary = Color(0xFF81C784), // Mint / green
      tertiary = Color(0xFFFFB74D), // Amber / orange dues alert
      background = Color(0xFF121212),
      surface = Color(0xFF1E1E1E),
      onPrimary = Color.Black,
      onSecondary = Color.Black,
      onBackground = Color.White,
      onSurface = Color.White
  )

private val LightColorScheme =
  lightColorScheme(
      primary = Color(0xFF1565C0), // Royal professional business blue
      secondary = Color(0xFF2E7D32), // Custom trust green for collections
      tertiary = Color(0xFFE65100), // Warnings and dues orange
      background = Color(0xFFF4F6F9), // Cozy light blue-gray
      surface = Color(0xFFFFFFFF),
      onPrimary = Color.White,
      onSecondary = Color.White,
      onBackground = Color(0xFF1C1B1F),
      onSurface = Color(0xFF1C1B1F)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is disabled by default to keep our branded theme cohesive
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
