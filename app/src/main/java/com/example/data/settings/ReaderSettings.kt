package com.example.data.settings

enum class ReaderThemeMode(val displayName: String) {
    LIGHT("Light"),
    SEPIA("Sepia"),
    DARK("Dark")
}

enum class ReaderFontStyle(val displayName: String) {
    SERIF_LORA("Lora Serif"),
    SERIF_PLAYFAIR("Playfair Display"),
    SYSTEM_SERIF("Classic Serif"),
    SYSTEM_SANS("Clean Sans")
}

enum class ReaderLineSpacing(val valueMultiplier: Float, val displayName: String) {
    COMPACT(1.3f, "Compact"),
    NORMAL(1.6f, "Normal"),
    RELAXED(2.0f, "Relaxed")
}

data class ReaderSettings(
    val fontSizeSp: Float = 18f,
    val fontStyle: ReaderFontStyle = ReaderFontStyle.SERIF_LORA,
    val lineSpacing: ReaderLineSpacing = ReaderLineSpacing.NORMAL,
    val theme: ReaderThemeMode = ReaderThemeMode.SEPIA,
    val brightness: Int = 85 // 0-100%
)
