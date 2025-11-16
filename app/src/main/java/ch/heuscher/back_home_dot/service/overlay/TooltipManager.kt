package ch.heuscher.back_home_dot.service.overlay

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import ch.heuscher.back_home_dot.R
import ch.heuscher.back_home_dot.domain.model.DotPosition
import ch.heuscher.back_home_dot.domain.model.Gesture

/**
 * Manages tooltip display that shows action descriptions beside the button.
 * Shows comprehensive overlay on interaction and hides 2.5s after last interaction.
 * Tooltip is added as a child view to the overlay container, ensuring proper Z-order control.
 */
class TooltipManager(
    private val context: Context,
    private val getCurrentPosition: () -> DotPosition?,
    private val getScreenSize: () -> Point,
    private val getTooltipContainer: () -> android.widget.FrameLayout?,
    private val bringButtonToFront: () -> Unit
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

        val tooltipContainer = getTooltipContainer() ?: run {
            Log.e(TAG, "Tooltip container not available")
            return
        }

        val density = context.resources.displayMetrics.density
        val paddingPx = (TOOLTIP_PADDING_DP * density).toInt()
        val lineSpacingPx = (TOOLTIP_LINE_SPACING_DP * density).toInt()
        val maxWidthPx = (TOOLTIP_MAX_WIDTH_DP * density).toInt()

        // Create tooltip content
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#2D2D2D"))
            setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
            alpha = TOOLTIP_ALPHA
            elevation = 8f * density  // Visual depth effect
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

        try {
            // Add tooltip as child of overlay container
            tooltipContainer.visibility = View.VISIBLE
            tooltipContainer.removeAllViews()
            tooltipContainer.addView(container)

            positionTooltip()

            // Bring button to front in Z-order (ViewGroup child reordering)
            bringButtonToFront()

            Log.d(TAG, "Tooltip shown as child view with button in front")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add tooltip", e)
            tooltipView = null
            currentTapBehavior = null
            tooltipContainer.visibility = View.GONE
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
     * Positions the tooltip below the button by default.
     * Ensures tooltip never overlaps the button.
     * Falls back to above, left, or right if there's not enough space below.
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

        // Calculate button bounds
        val buttonLeft = buttonPos.x
        val buttonRight = buttonPos.x + buttonSizePx
        val buttonTop = buttonPos.y
        val buttonBottom = buttonPos.y + buttonSizePx
        val buttonCenterX = buttonPos.x + buttonSizePx / 2
        val buttonCenterY = buttonPos.y + buttonSizePx / 2

        // Calculate available space on each side
        val spaceRight = screenSize.x - buttonRight
        val spaceLeft = buttonLeft
        val spaceBelow = screenSize.y - buttonBottom
        val spaceAbove = buttonTop

        // Check if tooltip can fit on each side without overlapping button
        val canFitBelow = spaceBelow >= tooltipHeight + marginPx
        val canFitAbove = spaceAbove >= tooltipHeight + marginPx
        val canFitRight = spaceRight >= tooltipWidth + marginPx
        val canFitLeft = spaceLeft >= tooltipWidth + marginPx

        val (tooltipX, tooltipY) = when {
            // Prefer below the button (centered horizontally)
            canFitBelow -> {
                val x = (buttonCenterX - tooltipWidth / 2).coerceIn(0, screenSize.x - tooltipWidth)
                val y = buttonBottom + marginPx
                Pair(x, y)
            }
            // Try above (centered horizontally)
            canFitAbove -> {
                val x = (buttonCenterX - tooltipWidth / 2).coerceIn(0, screenSize.x - tooltipWidth)
                val y = buttonTop - tooltipHeight - marginPx
                Pair(x, y.coerceAtLeast(0))
            }
            // Try left side (centered vertically)
            canFitLeft -> {
                val x = buttonLeft - tooltipWidth - marginPx
                val y = (buttonCenterY - tooltipHeight / 2).coerceIn(0, screenSize.y - tooltipHeight)
                Pair(x.coerceAtLeast(0), y)
            }
            // Try right side (centered vertically)
            canFitRight -> {
                val x = buttonRight + marginPx
                val y = (buttonCenterY - tooltipHeight / 2).coerceIn(0, screenSize.y - tooltipHeight)
                Pair(x, y)
            }
            // Last resort: position below but ensure it doesn't overlap button vertically
            else -> {
                val x = (buttonCenterX - tooltipWidth / 2).coerceIn(0, screenSize.x - tooltipWidth)
                // Ensure minimum spacing from button even if tooltip goes off-screen
                val y = (buttonBottom + marginPx).coerceIn(
                    buttonBottom + marginPx,  // Never above bottom of button
                    screenSize.y - tooltipHeight
                )
                Pair(x, y)
            }
        }

        // Update position using FrameLayout.LayoutParams
        val params = android.widget.FrameLayout.LayoutParams(
            android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
            android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = android.view.Gravity.TOP or android.view.Gravity.START
            leftMargin = tooltipX
            topMargin = tooltipY
        }
        tooltip.layoutParams = params

        Log.d(TAG, "Tooltip positioned at ($tooltipX, $tooltipY) - below button, button at ($buttonLeft, $buttonTop)")
    }

    /**
     * Removes the tooltip from the screen.
     */
    fun removeTooltip() {
        tooltipView?.let {
            try {
                getTooltipContainer()?.apply {
                    removeAllViews()
                    visibility = View.GONE
                }
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
