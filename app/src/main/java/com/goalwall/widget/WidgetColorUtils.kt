package com.goalwall.widget

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

internal const val DEFAULT_WIDGET_BRAND_COLOR = "#6750A4"

private const val LUMINANCE_RED_WEIGHT = 0.299
private const val LUMINANCE_GREEN_WEIGHT = 0.587
private const val LUMINANCE_BLUE_WEIGHT = 0.114
private const val LUMINANCE_THRESHOLD = 0.5

internal fun parseWidgetColor(hex: String?): Color =
    runCatching {
        Color(android.graphics.Color.parseColor(hex ?: DEFAULT_WIDGET_BRAND_COLOR))
    }.getOrDefault(Color(android.graphics.Color.parseColor(DEFAULT_WIDGET_BRAND_COLOR)))

internal fun contrastTextColor(backgroundArgb: Int): Color {
    val red = android.graphics.Color.red(backgroundArgb)
    val green = android.graphics.Color.green(backgroundArgb)
    val blue = android.graphics.Color.blue(backgroundArgb)
    val luminance =
        (LUMINANCE_RED_WEIGHT * red + LUMINANCE_GREEN_WEIGHT * green + LUMINANCE_BLUE_WEIGHT * blue) / 255.0
    return if (luminance > LUMINANCE_THRESHOLD) Color.Black else Color.White
}

internal fun Color.toContrastTextColor(): Color = contrastTextColor(toArgb())
