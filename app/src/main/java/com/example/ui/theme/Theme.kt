package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Studio Dark Slate Scheme (Premium deep charcoal grey/slate with low fatigue, perfect for creative studio tasks)
private val StudioDarkColorScheme = darkColorScheme(
    primary = ElegantPrimary,
    onPrimary = ElegantOnPrimary,
    primaryContainer = ElegantPrimaryContainer,
    onPrimaryContainer = ElegantOnPrimaryContainer,
    secondary = Color(0xFF222030),
    onSecondary = Color(0xFFECEBFA),
    secondaryContainer = Color(0xFF14131C),
    onSecondaryContainer = Color(0xFFECEBFA),
    tertiary = ElegantTertiary,
    onTertiary = ElegantOnTertiary,
    background = Color(0xFF0F0E17), // Rich cinematic deep slate background
    onBackground = Color(0xFFECEBFA),
    surface = Color(0xFF191726), // Lighter slate surface for cards/panels
    onSurface = Color(0xFFECEBFA),
    surfaceVariant = Color(0xFF222030), // Deep slate variant for inputs and chips
    onSurfaceVariant = Color(0xFFA2A0B4), // Calming gray subtext to eliminate strain
    error = CardExpensesBg,
    onError = CardExpensesText
)

// OLED Obsidian Scheme (Pure obsidian pitch-black theme for pitch-dark environments & ultimate battery savings)
private val OledObsidianColorScheme = darkColorScheme(
    primary = ElegantPrimary,
    onPrimary = ElegantOnPrimary,
    primaryContainer = ElegantPrimaryContainer,
    onPrimaryContainer = ElegantOnPrimaryContainer,
    secondary = ElegantSurfaceVariant,
    onSecondary = ElegantOnSurfaceVariant,
    secondaryContainer = ElegantNavBarBg,
    onSecondaryContainer = ElegantOnBg,
    tertiary = ElegantTertiary,
    onTertiary = ElegantOnTertiary,
    background = Color(0xFF000000), // Pure black
    onBackground = Color(0xFFECEBFA),
    surface = Color(0xFF0D0B12), // Extremely dark pitch obsidian surface
    onSurface = Color(0xFFECEBFA),
    surfaceVariant = Color(0xFF181622),
    onSurfaceVariant = Color(0xFF838291),
    error = CardExpensesBg,
    onError = CardExpensesText
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // If true, OLED Obsidian; if false, Studio Dark Slate
  dynamicColor: Boolean = false, // Set to false to ensure our premium studio theme is applied
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) OledObsidianColorScheme else StudioDarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

