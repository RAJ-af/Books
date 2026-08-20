package com.example.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.readerDataStore: DataStore<Preferences> by preferencesDataStore(name = "reader_preferences")

class ReaderSettingsRepository(private val context: Context) {

    private object PreferenceKeys {
        val FONT_SIZE = floatPreferencesKey("reader_font_size")
        val FONT_STYLE = stringPreferencesKey("reader_font_style")
        val LINE_SPACING = stringPreferencesKey("reader_line_spacing")
        val THEME_MODE = stringPreferencesKey("reader_theme_mode")
        val BRIGHTNESS = intPreferencesKey("reader_brightness")
    }

    val readerSettings: Flow<ReaderSettings> = context.readerDataStore.data.map { preferences ->
        val fontSize = preferences[PreferenceKeys.FONT_SIZE] ?: 18f
        val fontStyleStr = preferences[PreferenceKeys.FONT_STYLE] ?: ReaderFontStyle.SERIF_LORA.name
        val lineSpacingStr = preferences[PreferenceKeys.LINE_SPACING] ?: ReaderLineSpacing.NORMAL.name
        val themeModeStr = preferences[PreferenceKeys.THEME_MODE] ?: ReaderThemeMode.SEPIA.name
        val brightness = preferences[PreferenceKeys.BRIGHTNESS] ?: 85

        val fontStyle = try {
            ReaderFontStyle.valueOf(fontStyleStr)
        } catch (_: Exception) {
            ReaderFontStyle.SERIF_LORA
        }

        val lineSpacing = try {
            ReaderLineSpacing.valueOf(lineSpacingStr)
        } catch (_: Exception) {
            ReaderLineSpacing.NORMAL
        }

        val theme = try {
            ReaderThemeMode.valueOf(themeModeStr)
        } catch (_: Exception) {
            ReaderThemeMode.SEPIA
        }

        ReaderSettings(
            fontSizeSp = fontSize,
            fontStyle = fontStyle,
            lineSpacing = lineSpacing,
            theme = theme,
            brightness = brightness
        )
    }

    suspend fun updateFontSize(fontSizeSp: Float) {
        context.readerDataStore.edit { preferences ->
            preferences[PreferenceKeys.FONT_SIZE] = fontSizeSp.coerceIn(12f, 32f)
        }
    }

    suspend fun updateFontStyle(fontStyle: ReaderFontStyle) {
        context.readerDataStore.edit { preferences ->
            preferences[PreferenceKeys.FONT_STYLE] = fontStyle.name
        }
    }

    suspend fun updateLineSpacing(lineSpacing: ReaderLineSpacing) {
        context.readerDataStore.edit { preferences ->
            preferences[PreferenceKeys.LINE_SPACING] = lineSpacing.name
        }
    }

    suspend fun updateTheme(theme: ReaderThemeMode) {
        context.readerDataStore.edit { preferences ->
            preferences[PreferenceKeys.THEME_MODE] = theme.name
        }
    }

    suspend fun updateBrightness(brightness: Int) {
        context.readerDataStore.edit { preferences ->
            preferences[PreferenceKeys.BRIGHTNESS] = brightness.coerceIn(10, 100)
        }
    }
}
