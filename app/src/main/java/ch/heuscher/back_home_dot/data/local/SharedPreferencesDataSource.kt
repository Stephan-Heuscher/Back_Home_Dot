package ch.heuscher.back_home_dot.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import ch.heuscher.back_home_dot.util.AppConstants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * SharedPreferences implementation of SettingsDataSource.
 * Handles all settings persistence using Android SharedPreferences.
 */
class SharedPreferencesDataSource(
    private val context: Context
) : SettingsDataSource {

    companion object {
        private const val TAG = "SharedPrefsDataSource"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(
        AppConstants.PREFS_NAME,
        Context.MODE_PRIVATE
    )

    init {
        Log.d(TAG, "Initialized SharedPreferences: ${AppConstants.PREFS_NAME}")
        Log.d(TAG, "Initial position: x=${prefs.getInt(AppConstants.KEY_POSITION_X, -1)}, y=${prefs.getInt(AppConstants.KEY_POSITION_Y, -1)}")
        Log.d(TAG, "Initial position percent: x=${prefs.getFloat(AppConstants.KEY_POSITION_X_PERCENT, -1f)}, y=${prefs.getFloat(AppConstants.KEY_POSITION_Y_PERCENT, -1f)}")
        Log.d(TAG, "Initial screen: ${prefs.getInt(AppConstants.KEY_SCREEN_WIDTH, -1)}x${prefs.getInt(AppConstants.KEY_SCREEN_HEIGHT, -1)}")
    }

    // Helper function to create flows that emit on preference changes
    private fun <T> getPreferenceFlow(
        key: String,
        defaultValue: T,
        getter: (SharedPreferences, String, T) -> T
    ): Flow<T> = callbackFlow {
        // Send initial value
        trySend(getter(prefs, key, defaultValue))

        // Listen for changes
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
            if (changedKey == key) {
                trySend(getter(prefs, key, defaultValue))
            }
        }

        prefs.registerOnSharedPreferenceChangeListener(listener)

        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.distinctUntilChanged()

    override fun isOverlayEnabled(): Flow<Boolean> =
        getPreferenceFlow(AppConstants.KEY_ENABLED, AppConstants.DEFAULT_ENABLED) { prefs, key, default ->
            prefs.getBoolean(key, default)
        }

    override suspend fun setOverlayEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(AppConstants.KEY_ENABLED, enabled).apply()
    }

    override fun getColor(): Flow<Int> =
        getPreferenceFlow(AppConstants.KEY_COLOR, AppConstants.DEFAULT_COLOR) { prefs, key, default ->
            prefs.getInt(key, default)
        }

    override suspend fun setColor(color: Int) {
        prefs.edit().putInt(AppConstants.KEY_COLOR, color).apply()
    }

    override fun getAlpha(): Flow<Int> =
        getPreferenceFlow(AppConstants.KEY_ALPHA, AppConstants.ALPHA_DEFAULT) { prefs, key, default ->
            prefs.getInt(key, default)
        }

    override suspend fun setAlpha(alpha: Int) {
        prefs.edit().putInt(AppConstants.KEY_ALPHA, alpha).apply()
    }

    override fun getPositionX(): Flow<Int> =
        getPreferenceFlow(AppConstants.KEY_POSITION_X, AppConstants.DEFAULT_POSITION_X_PX) { prefs, key, default ->
            prefs.getInt(key, default)
        }

    override suspend fun setPositionX(x: Int) {
        val oldValue = prefs.getInt(AppConstants.KEY_POSITION_X, -1)
        if (oldValue != x) {
            Log.d(TAG, "setPositionX: $oldValue -> $x")
            prefs.edit().putInt(AppConstants.KEY_POSITION_X, x).apply()
        }
    }

    override fun getPositionY(): Flow<Int> =
        getPreferenceFlow(AppConstants.KEY_POSITION_Y, AppConstants.DEFAULT_POSITION_Y_PX) { prefs, key, default ->
            prefs.getInt(key, default)
        }

    override suspend fun setPositionY(y: Int) {
        val oldValue = prefs.getInt(AppConstants.KEY_POSITION_Y, -1)
        if (oldValue != y) {
            Log.d(TAG, "setPositionY: $oldValue -> $y")
            prefs.edit().putInt(AppConstants.KEY_POSITION_Y, y).apply()
        }
    }

    override fun getPositionXPercent(): Flow<Float> =
        getPreferenceFlow(AppConstants.KEY_POSITION_X_PERCENT, AppConstants.DEFAULT_POSITION_X_PERCENT) { prefs, key, default ->
            prefs.getFloat(key, default)
        }

    override suspend fun setPositionXPercent(percent: Float) {
        val oldValue = prefs.getFloat(AppConstants.KEY_POSITION_X_PERCENT, -1f)
        if (oldValue != percent) {
            Log.d(TAG, "setPositionXPercent: $oldValue -> $percent")
            prefs.edit().putFloat(AppConstants.KEY_POSITION_X_PERCENT, percent).apply()
        }
    }

    override fun getPositionYPercent(): Flow<Float> =
        getPreferenceFlow(AppConstants.KEY_POSITION_Y_PERCENT, AppConstants.DEFAULT_POSITION_Y_PERCENT) { prefs, key, default ->
            prefs.getFloat(key, default)
        }

    override suspend fun setPositionYPercent(percent: Float) {
        val oldValue = prefs.getFloat(AppConstants.KEY_POSITION_Y_PERCENT, -1f)
        if (oldValue != percent) {
            Log.d(TAG, "setPositionYPercent: $oldValue -> $percent")
            prefs.edit().putFloat(AppConstants.KEY_POSITION_Y_PERCENT, percent).apply()
        }
    }

    override fun getRecentsTimeout(): Flow<Long> =
        getPreferenceFlow(AppConstants.KEY_RECENTS_TIMEOUT, AppConstants.RECENTS_TIMEOUT_DEFAULT_MS) { prefs, key, default ->
            prefs.getLong(key, default)
        }

    override suspend fun setRecentsTimeout(timeout: Long) {
        prefs.edit().putLong(AppConstants.KEY_RECENTS_TIMEOUT, timeout).apply()
    }

    override fun isKeyboardAvoidanceEnabled(): Flow<Boolean> =
        getPreferenceFlow(AppConstants.KEY_KEYBOARD_AVOIDANCE, AppConstants.DEFAULT_KEYBOARD_AVOIDANCE) { prefs, key, default ->
            prefs.getBoolean(key, default)
        }

    override suspend fun setKeyboardAvoidanceEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(AppConstants.KEY_KEYBOARD_AVOIDANCE, enabled).apply()
    }

    override fun getTapBehavior(): Flow<String> =
        getPreferenceFlow(AppConstants.KEY_TAP_BEHAVIOR, AppConstants.DEFAULT_TAP_BEHAVIOR) { prefs, key, default ->
            prefs.getString(key, default) ?: default
        }

    override suspend fun setTapBehavior(behavior: String) {
        prefs.edit().putString(AppConstants.KEY_TAP_BEHAVIOR, behavior).apply()
    }

    override fun getScreenWidth(): Flow<Int> =
        getPreferenceFlow(AppConstants.KEY_SCREEN_WIDTH, AppConstants.DEFAULT_SCREEN_WIDTH) { prefs, key, default ->
            prefs.getInt(key, default)
        }

    override suspend fun setScreenWidth(width: Int) {
        val oldValue = prefs.getInt(AppConstants.KEY_SCREEN_WIDTH, -1)
        if (oldValue != width) {
            Log.d(TAG, "setScreenWidth: $oldValue -> $width")
            prefs.edit().putInt(AppConstants.KEY_SCREEN_WIDTH, width).apply()
        }
    }

    override fun getScreenHeight(): Flow<Int> =
        getPreferenceFlow(AppConstants.KEY_SCREEN_HEIGHT, AppConstants.DEFAULT_SCREEN_HEIGHT) { prefs, key, default ->
            prefs.getInt(key, default)
        }

    override suspend fun setScreenHeight(height: Int) {
        val oldValue = prefs.getInt(AppConstants.KEY_SCREEN_HEIGHT, -1)
        if (oldValue != height) {
            Log.d(TAG, "setScreenHeight: $oldValue -> $height")
            prefs.edit().putInt(AppConstants.KEY_SCREEN_HEIGHT, height).apply()
        }
    }

    override fun getRotation(): Flow<Int> =
        getPreferenceFlow(AppConstants.KEY_ROTATION, 0) { prefs, key, default ->
            prefs.getInt(key, default)
        }

    override suspend fun setRotation(rotation: Int) {
        val oldValue = prefs.getInt(AppConstants.KEY_ROTATION, -1)
        if (oldValue != rotation) {
            Log.d(TAG, "setRotation: $oldValue -> $rotation")
            prefs.edit().putInt(AppConstants.KEY_ROTATION, rotation).apply()
        }
    }
}