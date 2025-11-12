package ch.heuscher.back_home_dot.service.overlay

import android.content.Context
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
import android.widget.TextView
import ch.heuscher.back_home_dot.R
import ch.heuscher.back_home_dot.domain.model.DotPosition
import ch.heuscher.back_home_dot.domain.model.Gesture

/**
 * Manages tooltip display that shows action descriptions beside the button.
 * Tooltips appear when gestures are detected and remain visible for 500ms after action.
 */
class TooltipManager(
    private val context: Context,
    private val getCurrentPosition: () -> DotPosition?,
    private val getScreenSize: () -> Point
) {
    companion object {
        private const val TAG = "TooltipManager"
        private const val TOOLTIP_DISPLAY_DURATION_MS = 500L
        private const val TOOLTIP_PADDING_DP = 12
        private const val TOOLTIP_TEXT_SIZE_SP = 16f
        private const val TOOLTIP_ALPHA = 0.9f
        private const val TOOLTIP_MARGIN_FROM_BUTTON_DP = 8
    }

    private var tooltipView: TextView? = null
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val handler = Handler(Looper.getMainLooper())
    private var hideTooltipRunnable: Runnable? = null

    /**
     * Shows a tooltip with the action description beside the button.
     * Automatically hides after TOOLTIP_DISPLAY_DURATION_MS.
     */
    fun showTooltip(gesture: Gesture, tapBehavior: String) {
        // Cancel any pending hide operation
        hideTooltipRunnable?.let { handler.removeCallbacks(it) }

        val actionText = getActionDescription(gesture, tapBehavior)
        if (actionText.isEmpty()) {
            Log.d(TAG, "No action text for gesture=$gesture, behavior=$tapBehavior")
            return
        }

        // Remove existing tooltip if present
        removeTooltip()

        // Create and show new tooltip
        createTooltipView(actionText)
        positionTooltip()

        // Schedule auto-hide
        hideTooltipRunnable = Runnable {
            removeTooltip()
        }
        handler.postDelayed(hideTooltipRunnable!!, TOOLTIP_DISPLAY_DURATION_MS)

        Log.d(TAG, "Showing tooltip: '$actionText' for gesture=$gesture")
    }

    /**
     * Creates the tooltip view with styling.
     */
    private fun createTooltipView(text: String) {
        val density = context.resources.displayMetrics.density
        val paddingPx = (TOOLTIP_PADDING_DP * density).toInt()

        tooltipView = TextView(context).apply {
            this.text = text
            textSize = TOOLTIP_TEXT_SIZE_SP
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#333333"))
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
            Log.d(TAG, "Tooltip view added to window")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add tooltip view", e)
            tooltipView = null
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
     * Cleanup method to be called when the service is destroyed.
     */
    fun cleanup() {
        removeTooltip()
        handler.removeCallbacksAndMessages(null)
    }
}
