package ch.heuscher.back_home_dot

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color

class OverlaySettings(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "overlay_settings"
        private const val KEY_ENABLED = "overlay_enabled"
        private const val KEY_COLOR = "overlay_color"
        private const val KEY_ALPHA = "overlay_alpha"

        private const val DEFAULT_COLOR = 0xFF2196F3.toInt() // Blue
        private const val DEFAULT_ALPHA = 255 // Fully opaque
    }

    var isEnabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_ENABLED, value).apply()

    var color: Int
        get() = prefs.getInt(KEY_COLOR, DEFAULT_COLOR)
        set(value) = prefs.edit().putInt(KEY_COLOR, value).apply()

    var alpha: Int
        get() = prefs.getInt(KEY_ALPHA, DEFAULT_ALPHA)
        set(value) = prefs.edit().putInt(KEY_ALPHA, value.coerceIn(0, 255)).apply()

    fun getColorWithAlpha(): Int {
        val baseColor = color
        return Color.argb(
            alpha,
            Color.red(baseColor),
            Color.green(baseColor),
            Color.blue(baseColor)
        )
    }
}
