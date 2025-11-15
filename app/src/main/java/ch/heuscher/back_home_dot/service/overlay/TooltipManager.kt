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
import android.widget.LinearLayout
import android.widget.TextView
import ch.heuscher.back_home_dot.R
import ch.heuscher.back_home_dot.domain.model.DotPosition
import ch.heuscher.back_home_dot.domain.model.Gesture

/**
 * Manages tooltip display that shows action descriptions beside the button.
 * Shows comprehensive overlay on interaction and hides 2.5s after last interaction.
 */
class TooltipManager(
    private val context: Context,
    private val getCurrentPosition: () -> DotPosition?,
    private val getScreenSize: () -> Point
) {
    companion object {
        private const val TAG = "TooltipManager"
        private const val TOOLTIP_DISPLAY_DURATION_MS = 2500L
        private const val TOOLTIP_PADDING_DP = 16
        private const val TOOLTIP_TEXT_SIZE_SP = 18f
        private const val TOOLTIP_TITLE_SIZE_SP = 20f
        private const val TOOLTIP_LINE_SPACING_DP = 6
        private const val TOOLTIP_ALPHA = 0.92f
        private const val TOOLTIP_MARGIN_FROM_BUTTON_DP = 12
        private const val TOOLTIP_MAX_WIDTH_DP = 280
    }

    private var tooltipView: View? = null
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val handler = Handler(Looper.getMainLooper())
    private var hideTooltipRunnable: Runnable? = null
    private var currentTapBehavior: String? = null

    /**
     * Shows a tooltip with all possible action descriptions beside the button.
     * Resets the hide timer on each interaction.
     * Automatically hides 2.5s after last interaction.
     */
    fun showTooltip(gesture: Gesture, tapBehavior: String) {
        // Cancel any pending hide operation to reset timer
        hideTooltipRunnable?.let { handler.removeCallbacks(it) }

        // Only recreate if tooltip doesn't exist or tap behavior changed
        if (tooltipView == null || currentTapBehavior != tapBehavior) {
            showComprehensiveHelp(tapBehavior)
        }

        // Schedule auto-hide 2.5s after this interaction
        hideTooltipRunnable = Runnable {
            removeTooltip()
        }
        handler.postDelayed(hideTooltipRunnable!!, TOOLTIP_DISPLAY_DURATION_MS)
    }

    /**
     * Shows comprehensive help overlay with all interactions.
     */
    private fun showComprehensiveHelp(tapBehavior: String) {
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

        tooltipView = container
        currentTapBehavior = tapBehavior

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
            Log.d(TAG, "Comprehensive help overlay shown")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add comprehensive help overlay", e)
            tooltipView = null
            currentTapBehavior = null
            return
        }
    }

    /**
     * Returns all possible actions based on tap behavior.
     */
    private fun getAllActionsText(tapBehavior: String): List<String> {
        return when (tapBehavior) {
            "NAVI" -> listOf(
                "• ${context.getString(R.string.tooltip_tap_once)} → ${context.getString(R.string.action_back)}",
                "• ${context.getString(R.string.tooltip_tap_twice)} → ${context.getString(R.string.action_previous_app)}",
                "• ${context.getString(R.string.tooltip_tap_three)} → ${context.getString(R.string.action_recents_overview)}",
                "• ${context.getString(R.string.tooltip_long_press)} → ${context.getString(R.string.action_home)}",
                "• ${context.getString(R.string.tooltip_drag)} → ${context.getString(R.string.action_move_dot)}"
            )
            "SAFE_HOME" -> listOf(
                "• ${context.getString(R.string.tooltip_tap_any)} → ${context.getString(R.string.action_home)}",
                "• ${context.getString(R.string.tooltip_long_press_drag)} → ${context.getString(R.string.action_move_dot)}"
            )
            else -> listOf(
                "• ${context.getString(R.string.tooltip_tap_any)} → ${context.getString(R.string.action_home)}",
                "• ${context.getString(R.string.tooltip_long_press_drag)} → ${context.getString(R.string.action_move_dot)}"
            )
        }
    }

    /**
     * Positions the tooltip beside the button where it's most visible.
     * Prefers horizontal placement (left/right) over vertical (above/below).
     * Chooses left vs right based on which side has more space.
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

        // Calculate available space on each side
        val spaceRight = screenSize.x - (buttonPos.x + buttonSizePx)
        val spaceLeft = buttonPos.x
        val spaceBelow = screenSize.y - (buttonPos.y + buttonSizePx)
        val spaceAbove = buttonPos.y

        // Prefer horizontal placement (left/right) - choose side with more space
        val canFitRight = spaceRight >= tooltipWidth + marginPx
        val canFitLeft = spaceLeft >= tooltipWidth + marginPx
        val canFitBelow = spaceBelow >= tooltipHeight + marginPx
        val canFitAbove = spaceAbove >= tooltipHeight + marginPx

        val (tooltipX, tooltipY) = when {
            // If both horizontal sides can fit, choose the one with more space
            canFitRight && canFitLeft -> {
                if (spaceRight >= spaceLeft) {
                    // Right side has more space
                    val x = buttonPos.x + buttonSizePx + marginPx
                    val y = buttonCenterY - tooltipHeight / 2
                    Pair(x, y.coerceIn(0, screenSize.y - tooltipHeight))
                } else {
                    // Left side has more space
                    val x = buttonPos.x - tooltipWidth - marginPx
                    val y = buttonCenterY - tooltipHeight / 2
                    Pair(x, y.coerceIn(0, screenSize.y - tooltipHeight))
                }
            }
            // Only right fits
            canFitRight -> {
                val x = buttonPos.x + buttonSizePx + marginPx
                val y = buttonCenterY - tooltipHeight / 2
                Pair(x, y.coerceIn(0, screenSize.y - tooltipHeight))
            }
            // Only left fits
            canFitLeft -> {
                val x = buttonPos.x - tooltipWidth - marginPx
                val y = buttonCenterY - tooltipHeight / 2
                Pair(x, y.coerceIn(0, screenSize.y - tooltipHeight))
            }
            // Neither horizontal side fits - try vertical, but still prefer horizontal with overlap
            spaceLeft > spaceRight -> {
                // More space on left, use left even if it doesn't fully fit
                val x = buttonPos.x - tooltipWidth - marginPx
                val y = buttonCenterY - tooltipHeight / 2
                Pair(x.coerceAtLeast(0), y.coerceIn(0, screenSize.y - tooltipHeight))
            }
            // More space on right or equal, use right even if it doesn't fully fit
            else -> {
                val x = buttonPos.x + buttonSizePx + marginPx
                val y = buttonCenterY - tooltipHeight / 2
                Pair(x.coerceAtMost(screenSize.x - tooltipWidth), y.coerceIn(0, screenSize.y - tooltipHeight))
            }
        }

        // Update window parameters
        val params = tooltip.layoutParams as WindowManager.LayoutParams
        params.x = tooltipX
        params.y = tooltipY

        try {
            windowManager.updateViewLayout(tooltip, params)
            Log.d(TAG, "Tooltip positioned at ($tooltipX, $tooltipY) - spaceLeft=$spaceLeft, spaceRight=$spaceRight")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update tooltip position", e)
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
        currentTapBehavior = null
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
