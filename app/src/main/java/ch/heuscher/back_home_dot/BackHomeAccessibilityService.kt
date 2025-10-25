package ch.heuscher.back_home_dot

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Build
import android.view.accessibility.AccessibilityEvent

class BackHomeAccessibilityService : AccessibilityService() {

    private lateinit var appSwitcher: AppSwitcherUtil

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
            notificationTimeout = 100
        }
        serviceInfo = info

        appSwitcher = AppSwitcherUtil(this)

        // Notify that service is connected
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

    fun performBackAction() {
        performGlobalAction(GLOBAL_ACTION_BACK)
    }

    fun performHomeAction() {
        performGlobalAction(GLOBAL_ACTION_HOME)
    }

    fun performRecentsAction() {
        // Use UsageStatsManager to switch to previous app directly
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val success = appSwitcher.switchToPreviousApp()
            if (!success) {
                // Fallback to double-tap recents if UsageStats fails
                performGlobalAction(GLOBAL_ACTION_RECENTS)
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    performGlobalAction(GLOBAL_ACTION_RECENTS)
                }, 250)
            }
        } else {
            // For older Android versions, use double-tap recents
            performGlobalAction(GLOBAL_ACTION_RECENTS)
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                performGlobalAction(GLOBAL_ACTION_RECENTS)
            }, 250)
        }
    }

    fun performRecentsOverviewAction() {
        // Single recents action to open task overview
        performGlobalAction(GLOBAL_ACTION_RECENTS)
    }

    companion object {
        var instance: BackHomeAccessibilityService? = null
            private set

        fun isServiceEnabled(): Boolean {
            return instance != null
        }
    }
}
