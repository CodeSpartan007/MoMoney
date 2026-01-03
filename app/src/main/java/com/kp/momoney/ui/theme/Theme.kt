package com.kp.momoney.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun MoMoneyTheme(
    themeConfig: AppThemeConfig,
    content: @Composable () -> Unit
) {
    val colorScheme = generateColorScheme(
        seedColor = themeConfig.seedColor,
        isDark = themeConfig.isDark
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}