package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.example.R

val GoogleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val PlayfairFontName = GoogleFont("Playfair Display")
val LoraFontName = GoogleFont("Lora")
val InterFontName = GoogleFont("Inter")

val PlayfairDisplayFontFamily = FontFamily(
    Font(googleFont = PlayfairFontName, fontProvider = GoogleFontProvider, weight = FontWeight.Normal),
    Font(googleFont = PlayfairFontName, fontProvider = GoogleFontProvider, weight = FontWeight.Medium),
    Font(googleFont = PlayfairFontName, fontProvider = GoogleFontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = PlayfairFontName, fontProvider = GoogleFontProvider, weight = FontWeight.Bold),
    Font(googleFont = PlayfairFontName, fontProvider = GoogleFontProvider, weight = FontWeight.Normal, style = FontStyle.Italic)
)

val LoraFontFamily = FontFamily(
    Font(googleFont = LoraFontName, fontProvider = GoogleFontProvider, weight = FontWeight.Normal),
    Font(googleFont = LoraFontName, fontProvider = GoogleFontProvider, weight = FontWeight.Medium),
    Font(googleFont = LoraFontName, fontProvider = GoogleFontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = LoraFontName, fontProvider = GoogleFontProvider, weight = FontWeight.Bold),
    Font(googleFont = LoraFontName, fontProvider = GoogleFontProvider, weight = FontWeight.Normal, style = FontStyle.Italic)
)

val EditorialSerif = PlayfairDisplayFontFamily
val ContentSerif = LoraFontFamily
val SystemSerif = FontFamily.Serif
val SystemSans = FontFamily.SansSerif

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = EditorialSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 42.sp,
        lineHeight = 48.sp,
        letterSpacing = (-0.5).sp,
        color = ObsidianBlack
    ),
    displayMedium = TextStyle(
        fontFamily = EditorialSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.25).sp,
        color = ObsidianBlack
    ),
    displaySmall = TextStyle(
        fontFamily = EditorialSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        color = ObsidianBlack
    ),
    headlineLarge = TextStyle(
        fontFamily = EditorialSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        color = ObsidianBlack
    ),
    headlineMedium = TextStyle(
        fontFamily = EditorialSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        color = ObsidianBlack
    ),
    headlineSmall = TextStyle(
        fontFamily = EditorialSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        color = ObsidianBlack
    ),
    titleLarge = TextStyle(
        fontFamily = SystemSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        color = ObsidianBlack
    ),
    titleMedium = TextStyle(
        fontFamily = SystemSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.15.sp,
        color = ObsidianBlack
    ),
    titleSmall = TextStyle(
        fontFamily = SystemSans,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        color = ObsidianBlack
    ),
    bodyLarge = TextStyle(
        fontFamily = ContentSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 27.sp,
        letterSpacing = 0.2.sp,
        color = ObsidianBlack
    ),
    bodyMedium = TextStyle(
        fontFamily = ContentSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 23.sp,
        letterSpacing = 0.2.sp,
        color = ObsidianBlack
    ),
    bodySmall = TextStyle(
        fontFamily = SystemSans,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.2.sp,
        color = TextMuted
    ),
    labelLarge = TextStyle(
        fontFamily = SystemSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        color = ObsidianBlack
    ),
    labelMedium = TextStyle(
        fontFamily = SystemSans,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        color = TextMuted
    ),
    labelSmall = TextStyle(
        fontFamily = SystemSans,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp,
        color = TextLightMuted
    )
)
