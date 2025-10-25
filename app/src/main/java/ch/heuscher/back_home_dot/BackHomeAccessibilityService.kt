package ch.heuscher.back_home_dot

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent

/**
 * Accessibility service for performing system navigation actions
 */
class BackHomeAccessibilityService : AccessibilityService() {

    private lateinit var appSwitcher: AppSwitcherUtil
    private val handler = Handler(Looper.getMainLooper())

    override fun onServiceConnected() {
        super.onServiceConnected()

        // Configure service info
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
            notificationTimeout = 100
        }

        appSwitcher = AppSwitcherUtil(this)
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Not needed for this functionality
    }

    override fun onInterrupt() {
        // Service interrupted
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        return super.onUnbind(intent)
    }

    /**
     * Perform back navigation action
     */
    fun performBackAction() {
        performGlobalAction(GLOBAL_ACTION_BACK)
    }

    /**
     * Perform home action
     */
    fun performHomeAction() {
        performGlobalAction(GLOBAL_ACTION_HOME)
    }

    /**
     * Switch to previous app
     * Tries ActivityManager first, then UsageStatsManager, falls back to double-RECENTS
     */
    fun performRecentsAction() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Try intelligent app switching
            if (appSwitcher.switchToPreviousApp()) {
                return
            }
        }

        // Fallback: double-tap recents
        performDoubleRecents()
    }

    /**
     * Open recent apps overview
     */
    fun performRecentsOverviewAction() {
        performGlobalAction(GLOBAL_ACTION_RECENTS)
    }

    /**
     * Fallback method: Double-tap recents to switch to previous app
     */
    private fun performDoubleRecents() {
        performGlobalAction(GLOBAL_ACTION_RECENTS)
        handler.postDelayed({
            performGlobalAction(GLOBAL_ACTION_RECENTS)
        }, RECENTS_DOUBLE_TAP_DELAY)
    }

    companion object {
        private const val RECENTS_DOUBLE_TAP_DELAY = 250L

        var instance: BackHomeAccessibilityService? = null
            private set

        fun isServiceEnabled(): Boolean = instance != null
    }
}
