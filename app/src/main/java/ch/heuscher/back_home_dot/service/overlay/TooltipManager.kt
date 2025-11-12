package ch.heuscher.back_home_dot.service.overlay

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import ch.heuscher.back_home_dot.R
import ch.heuscher.back_home_dot.domain.model.DotPosition
import ch.heuscher.back_home_dot.domain.model.Gesture

/**
 * Manages tooltip display that shows action descriptions beside the button.
 * On first interaction, shows a large overlay with all possible interactions.
 * On subsequent interactions, shows current action for 500ms.
 */
class TooltipManager(
    private val context: Context,
    private val getCurrentPosition: () -> DotPosition?,
    private val getScreenSize: () -> Point
) {
    companion object {
        private const val TAG = "TooltipManager"
        private const val TOOLTIP_DISPLAY_DURATION_MS = 500L
        private const val FIRST_TIME_DISPLAY_DURATION_MS = 5000L
        private const val TOOLTIP_PADDING_DP = 16
        private const val TOOLTIP_TEXT_SIZE_SP = 18f
        private const val TOOLTIP_TITLE_SIZE_SP = 20f
        private const val TOOLTIP_LINE_SPACING_DP = 6
        private const val TOOLTIP_ALPHA = 0.92f
        private const val TOOLTIP_MARGIN_FROM_BUTTON_DP = 12
        private const val TOOLTIP_MAX_WIDTH_DP = 280

        private const val PREFS_NAME = "tooltip_preferences"
        private const val KEY_FIRST_INTERACTION = "first_interaction_shown"
    }

    private var tooltipView: View? = null
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val handler = Handler(Looper.getMainLooper())
    private var hideTooltipRunnable: Runnable? = null
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Shows a tooltip with the action description beside the button.
     * On first interaction, shows all possible actions.
     * Automatically hides after duration.
     */
    fun showTooltip(gesture: Gesture, tapBehavior: String) {
        // Cancel any pending hide operation
        hideTooltipRunnable?.let { handler.removeCallbacks(it) }

        val isFirstTime = !prefs.getBoolean(KEY_FIRST_INTERACTION, false)

        if (isFirstTime) {
            // Show comprehensive help on first interaction
            showFirstTimeHelp(tapBehavior)
            // Mark as shown
            prefs.edit().putBoolean(KEY_FIRST_INTERACTION, true).apply()
        } else {
            // Show quick action tooltip
            showQuickTooltip(gesture, tapBehavior)
        }
    }

    /**
     * Shows comprehensive help overlay with all interactions on first use.
     */
    private fun showFirstTimeHelp(tapBehavior: String) {
        // Remove existing tooltip if present
        removeTooltip()

        val density = context.resources.displayMetrics.density
        val paddingPx = (TOOLTIP_PADDING_DP * density).toInt()
        val lineSpacingPx = (TOOLTIP_LINE_SPACING_DP * density).toInt()
        val maxWidthPx = (TOOLTIP_MAX_WIDTH_DP * density).toInt()

        // Create container layout
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#2D2D2D"))
            setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
            alpha = TOOLTIP_ALPHA
            elevation = 12f * density
        }

        // Add title
        val title = TextView(context).apply {
            text = context.getString(R.string.tooltip_all_actions_title)
            textSize = TOOLTIP_TITLE_SIZE_SP
            setTextColor(Color.parseColor("#FFFFFF"))
            setPadding(0, 0, 0, lineSpacingPx * 2)
            gravity = Gravity.START
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        container.addView(title)

        // Add all gesture descriptions
        val actions = getAllActionsText(tapBehavior)
        actions.forEach { actionText ->
            val textView = TextView(context).apply {
                text = actionText
                textSize = TOOLTIP_TEXT_SIZE_SP
                setTextColor(Color.parseColor("#E0E0E0"))
                setPadding(0, lineSpacingPx / 2, 0, lineSpacingPx / 2)
                gravity = Gravity.START
                setLineSpacing(lineSpacingPx.toFloat(), 1f)
            }
            container.addView(textView)
        }

        // Add dismissal hint at bottom
        val hint = TextView(context).apply {
            text = context.getString(R.string.tooltip_auto_dismiss)
            textSize = TOOLTIP_TEXT_SIZE_SP - 2
            setTextColor(Color.parseColor("#999999"))
            setPadding(0, lineSpacingPx * 2, 0, 0)
            gravity = Gravity.CENTER
            setTypeface(null, android.graphics.Typeface.ITALIC)
        }
        container.addView(hint)

        tooltipView = container

        // Measure the view
        container.measure(
            View.MeasureSpec.makeMeasureSpec(maxWidthPx, View.MeasureSpec.AT_MOST),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START

        try {
            windowManager.addView(tooltipView, params)
            positionTooltip()
            Log.d(TAG, "First-time help overlay shown")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add first-time help overlay", e)
            tooltipView = null
            return
        }

        // Schedule auto-hide after longer duration
        hideTooltipRunnable = Runnable {
            removeTooltip()
        }
        handler.postDelayed(hideTooltipRunnable!!, FIRST_TIME_DISPLAY_DURATION_MS)
    }

    /**
     * Shows quick single-action tooltip for subsequent interactions.
     */
    private fun showQuickTooltip(gesture: Gesture, tapBehavior: String) {
        val actionText = getActionDescription(gesture, tapBehavior)
        if (actionText.isEmpty()) {
            Log.d(TAG, "No action text for gesture=$gesture, behavior=$tapBehavior")
            return
        }

        // Remove existing tooltip if present
        removeTooltip()

        // Create and show new tooltip
        createQuickTooltipView(actionText)
        positionTooltip()

        // Schedule auto-hide
        hideTooltipRunnable = Runnable {
            removeTooltip()
        }
        handler.postDelayed(hideTooltipRunnable!!, TOOLTIP_DISPLAY_DURATION_MS)

        Log.d(TAG, "Showing quick tooltip: '$actionText' for gesture=$gesture")
    }

    /**
     * Creates a quick single-action tooltip view.
     */
    private fun createQuickTooltipView(text: String) {
        val density = context.resources.displayMetrics.density
        val paddingPx = (TOOLTIP_PADDING_DP * density).toInt()

        tooltipView = TextView(context).apply {
            this.text = text
            textSize = TOOLTIP_TEXT_SIZE_SP
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#2D2D2D"))
            alpha = TOOLTIP_ALPHA
            setPadding(paddingPx, paddingPx / 2, paddingPx, paddingPx / 2)
            gravity = Gravity.CENTER
            elevation = 10f * density
        }

        // Measure the view to get its size
        tooltipView?.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START

        try {
            windowManager.addView(tooltipView, params)
            Log.d(TAG, "Quick tooltip view added to window")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add quick tooltip view", e)
            tooltipView = null
        }
    }

    /**
     * Returns all possible actions based on tap behavior.
     */
    private fun getAllActionsText(tapBehavior: String): List<String> {
        return when (tapBehavior) {
            "STANDARD" -> listOf(
                "• ${context.getString(R.string.tooltip_tap_once)} → ${context.getString(R.string.action_home)}",
                "• ${context.getString(R.string.tooltip_tap_twice)} → ${context.getString(R.string.action_back)}",
                "• ${context.getString(R.string.tooltip_tap_three)} → ${context.getString(R.string.action_recents_overview)}",
                "• ${context.getString(R.string.tooltip_tap_four)} → ${context.getString(R.string.action_open_app)}",
                "• ${context.getString(R.string.tooltip_long_press)} → ${context.getString(R.string.action_home)}"
            )
            "NAVI" -> listOf(
                "• ${context.getString(R.string.tooltip_tap_once)} → ${context.getString(R.string.action_back)}",
                "• ${context.getString(R.string.tooltip_tap_twice)} → ${context.getString(R.string.action_switch_to_previous)}",
                "• ${context.getString(R.string.tooltip_tap_three)} → ${context.getString(R.string.action_recents_overview)}",
                "• ${context.getString(R.string.tooltip_tap_four)} → ${context.getString(R.string.action_open_app)}",
                "• ${context.getString(R.string.tooltip_long_press)} → ${context.getString(R.string.action_home)}"
            )
            "SAFE_HOME" -> listOf(
                "• ${context.getString(R.string.tooltip_tap_once)} → ${context.getString(R.string.action_home)}",
                "• ${context.getString(R.string.tooltip_tap_twice)} → ${context.getString(R.string.action_home)}",
                "• ${context.getString(R.string.tooltip_tap_three)} → ${context.getString(R.string.action_home)}",
                "• ${context.getString(R.string.tooltip_tap_four)} → ${context.getString(R.string.action_open_app)}",
                "• ${context.getString(R.string.tooltip_long_press)} → ${context.getString(R.string.action_drag_mode)}"
            )
            else -> listOf()
        }
    }

    /**
     * Positions the tooltip beside the button where it's most visible.
     * Chooses the best side based on available screen space.
     */
    private fun positionTooltip() {
        val tooltip = tooltipView ?: return
        val buttonPos = getCurrentPosition() ?: return
        val screenSize = getScreenSize()
        val density = context.resources.displayMetrics.density

        val tooltipWidth = tooltip.measuredWidth
        val tooltipHeight = tooltip.measuredHeight
        val marginPx = (TOOLTIP_MARGIN_FROM_BUTTON_DP * density).toInt()
        val buttonSizePx = (48 * density).toInt() // DOT_SIZE_DP

        // Calculate center of button
        val buttonCenterX = buttonPos.x + buttonSizePx / 2
        val buttonCenterY = buttonPos.y + buttonSizePx / 2

        // Determine best position based on available space
        val spaceRight = screenSize.x - (buttonPos.x + buttonSizePx)
        val spaceLeft = buttonPos.x
        val spaceBelow = screenSize.y - (buttonPos.y + buttonSizePx)
        val spaceAbove = buttonPos.y

        val (tooltipX, tooltipY) = when {
            // Prefer right side if enough space
            spaceRight >= tooltipWidth + marginPx -> {
                val x = buttonPos.x + buttonSizePx + marginPx
                val y = buttonCenterY - tooltipHeight / 2
                Pair(x, y.coerceIn(0, screenSize.y - tooltipHeight))
            }
            // Try left side
            spaceLeft >= tooltipWidth + marginPx -> {
                val x = buttonPos.x - tooltipWidth - marginPx
                val y = buttonCenterY - tooltipHeight / 2
                Pair(x, y.coerceIn(0, screenSize.y - tooltipHeight))
            }
            // Try below
            spaceBelow >= tooltipHeight + marginPx -> {
                val x = buttonCenterX - tooltipWidth / 2
                val y = buttonPos.y + buttonSizePx + marginPx
                Pair(x.coerceIn(0, screenSize.x - tooltipWidth), y)
            }
            // Try above
            spaceAbove >= tooltipHeight + marginPx -> {
                val x = buttonCenterX - tooltipWidth / 2
                val y = buttonPos.y - tooltipHeight - marginPx
                Pair(x.coerceIn(0, screenSize.x - tooltipWidth), y)
            }
            // Default: right side with overflow
            else -> {
                val x = buttonPos.x + buttonSizePx + marginPx
                val y = buttonCenterY - tooltipHeight / 2
                Pair(x, y.coerceIn(0, screenSize.y - tooltipHeight))
            }
        }

        // Update window parameters
        val params = tooltip.layoutParams as WindowManager.LayoutParams
        params.x = tooltipX
        params.y = tooltipY

        try {
            windowManager.updateViewLayout(tooltip, params)
            Log.d(TAG, "Tooltip positioned at ($tooltipX, $tooltipY)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update tooltip position", e)
        }
    }

    /**
     * Returns the localized action description based on gesture and tap behavior.
     */
    private fun getActionDescription(gesture: Gesture, tapBehavior: String): String {
        return when (gesture) {
            Gesture.TAP -> when (tapBehavior) {
                "STANDARD" -> context.getString(R.string.action_home)
                "NAVI" -> context.getString(R.string.action_back)
                "SAFE_HOME" -> context.getString(R.string.action_home)
                else -> context.getString(R.string.action_back)
            }
            Gesture.DOUBLE_TAP -> when (tapBehavior) {
                "STANDARD" -> context.getString(R.string.action_back)
                "NAVI" -> context.getString(R.string.action_switch_to_previous)
                "SAFE_HOME" -> context.getString(R.string.action_home)
                else -> context.getString(R.string.action_recents)
            }
            Gesture.TRIPLE_TAP -> if (tapBehavior == "SAFE_HOME") {
                context.getString(R.string.action_home)
            } else {
                context.getString(R.string.action_recents_overview)
            }
            Gesture.QUADRUPLE_TAP -> context.getString(R.string.action_open_app)
            Gesture.LONG_PRESS -> if (tapBehavior == "SAFE_HOME") {
                context.getString(R.string.action_drag_mode)
            } else {
                context.getString(R.string.action_home)
            }
            else -> "" // DRAG gestures don't show tooltips
        }
    }

    /**
     * Removes the tooltip from the screen.
     */
    fun removeTooltip() {
        tooltipView?.let {
            try {
                windowManager.removeView(it)
                Log.d(TAG, "Tooltip removed")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to remove tooltip", e)
            }
            tooltipView = null
        }
        hideTooltipRunnable?.let { handler.removeCallbacks(it) }
        hideTooltipRunnable = null
    }

    /**
     * Resets the first-time help flag (for testing or user preference).
     */
    fun resetFirstTimeHelp() {
        prefs.edit().putBoolean(KEY_FIRST_INTERACTION, false).apply()
        Log.d(TAG, "First-time help flag reset")
    }

    /**
     * Cleanup method to be called when the service is destroyed.
     */
    fun cleanup() {
        removeTooltip()
        handler.removeCallbacksAndMessages(null)
    }
}
