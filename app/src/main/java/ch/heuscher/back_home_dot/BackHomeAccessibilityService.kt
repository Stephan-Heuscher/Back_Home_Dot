package ch.heuscher.back_home_dot

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent

/**
 * Accessibility service for performing system navigation actions
 */
class BackHomeAccessibilityService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var settings: OverlaySettings

    override fun onServiceConnected() {
        super.onServiceConnected()

        settings = OverlaySettings(this)

        // Configure service info
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
            notificationTimeout = 100
        }

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
     * Switch to previous app using double-tap recents
     * Uses configurable timeout from settings
     */
    fun performRecentsAction() {
        performGlobalAction(GLOBAL_ACTION_RECENTS)
        val delay = settings.recentsTimeout
        handler.postDelayed({
            performGlobalAction(GLOBAL_ACTION_RECENTS)
        }, delay)
    }

    /**
     * Open recent apps overview
     */
    fun performRecentsOverviewAction() {
        performGlobalAction(GLOBAL_ACTION_RECENTS)
    }

    companion object {
        var instance: BackHomeAccessibilityService? = null
            private set

        fun isServiceEnabled(): Boolean = instance != null
    }
}
