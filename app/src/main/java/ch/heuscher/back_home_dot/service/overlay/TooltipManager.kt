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
import android.widget.LinearLayout
import android.widget.TextView
import ch.heuscher.back_home_dot.R
import ch.heuscher.back_home_dot.domain.model.DotPosition
import ch.heuscher.back_home_dot.domain.model.Gesture

/**
 * Manages tooltip display that shows action descriptions beside the button.
 * Shows comprehensive overlay on interaction and hides 2.5s after last interaction.
 * Tooltip has FLAG_NOT_TOUCHABLE so touches pass through to the button below.
 */
class TooltipManager(
    private val context: Context,
    private val getCurrentPosition: () -> DotPosition?,
    private val getScreenSize: () -> Point,
    private val onBringButtonToFront: () -> Unit = {}
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
    private var tooltipContainer: LinearLayout? = null
    private val handler = Handler(Looper.getMainLooper())
    private var hideTooltipRunnable: Runnable? = null
    private var currentTapBehavior: String? = null
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager
    private var isInitialized = false

    /**
     * Initialize tooltip by creating the window once on startup.
     * This should be called after the button is created.
     * Brings button to front after tooltip window is created.
     */
    fun initialize(tapBehavior: String) {
        if (isInitialized) return

        createTooltipWindow(tapBehavior)
        tooltipView?.visibility = View.GONE

        // Bring button to front once after both windows are initialized
        handler.postDelayed({
            onBringButtonToFront()
            isInitialized = true
            Log.d(TAG, "Tooltip initialized and button brought to front")
        }, 50)
    }

    /**
     * Shows the tooltip with action descriptions beside the button.
     * Resets the hide timer on each interaction.
     * Automatically hides 2.5s after last interaction.
     */
    fun showTooltip(gesture: Gesture, tapBehavior: String) {
        // Cancel any pending hide operation to reset timer
        hideTooltipRunnable?.let { handler.removeCallbacks(it) }

        // Update content if tap behavior changed
        if (currentTapBehavior != tapBehavior) {
            updateTooltipContent(tapBehavior)
        }

        // Position and show the tooltip
        positionTooltip()
        tooltipView?.visibility = View.VISIBLE

        // Schedule auto-hide 2.5s after this interaction
        hideTooltipRunnable = Runnable {
            hideTooltip()
        }
        handler.postDelayed(hideTooltipRunnable!!, TOOLTIP_DISPLAY_DURATION_MS)
    }

    /**
     * Hides the tooltip without removing it.
     */
    private fun hideTooltip() {
        tooltipView?.visibility = View.GONE
    }

    /**
     * Creates the tooltip window once on initialization.
     */
    private fun createTooltipWindow(tapBehavior: String) {
        val density = context.resources.displayMetrics.density
        val paddingPx = (TOOLTIP_PADDING_DP * density).toInt()
        val lineSpacingPx = (TOOLTIP_LINE_SPACING_DP * density).toInt()
        val maxWidthPx = (TOOLTIP_MAX_WIDTH_DP * density).toInt()

        // Create tooltip content container
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

        tooltipContainer = container
        tooltipView = container
        currentTapBehavior = tapBehavior

        // Measure the view
        container.measure(
            View.MeasureSpec.makeMeasureSpec(maxWidthPx, View.MeasureSpec.AT_MOST),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        try {
            // Add tooltip as separate window with FLAG_NOT_TOUCHABLE
            val tooltipParams = android.view.WindowManager.LayoutParams(
                android.view.WindowManager.LayoutParams.WRAP_CONTENT,
                android.view.WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    android.view.WindowManager.LayoutParams.TYPE_PHONE
                },
                android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
            }

            windowManager.addView(container, tooltipParams)
            positionTooltip()

            // Tooltip has FLAG_NOT_TOUCHABLE, so touches pass through to button
            Log.d(TAG, "Tooltip window created")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create tooltip window", e)
            tooltipView = null
            tooltipContainer = null
            currentTapBehavior = null
        }
    }

    /**
     * Updates tooltip content when tap behavior changes.
     */
    private fun updateTooltipContent(tapBehavior: String) {
        val container = tooltipContainer ?: return
        val density = context.resources.displayMetrics.density
        val lineSpacingPx = (TOOLTIP_LINE_SPACING_DP * density).toInt()

        // Remove old action descriptions (keep title at index 0)
        while (container.childCount > 1) {
            container.removeViewAt(1)
        }

        // Add updated gesture descriptions
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

        currentTapBehavior = tapBehavior

        // Re-measure after content update
        val maxWidthPx = (TOOLTIP_MAX_WIDTH_DP * density).toInt()
        container.measure(
            View.MeasureSpec.makeMeasureSpec(maxWidthPx, View.MeasureSpec.AT_MOST),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        Log.d(TAG, "Tooltip content updated for tap behavior: $tapBehavior")
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

        // Update position using WindowManager.LayoutParams
        val params = tooltip.layoutParams as android.view.WindowManager.LayoutParams
        params.x = tooltipX
        params.y = tooltipY
        windowManager.updateViewLayout(tooltip, params)

        Log.d(TAG, "Tooltip positioned at ($tooltipX, $tooltipY) - below button, button at ($buttonLeft, $buttonTop)")
    }

    /**
     * Removes the tooltip from the screen (deprecated - use hideTooltip instead).
     * Kept for compatibility but now just hides the tooltip.
     */
    @Deprecated("Use hideTooltip instead", ReplaceWith("hideTooltip()"))
    fun removeTooltip() {
        hideTooltip()
        hideTooltipRunnable?.let { handler.removeCallbacks(it) }
        hideTooltipRunnable = null
    }

    /**
     * Cleanup method to be called when the service is destroyed.
     * Actually removes the tooltip window from WindowManager.
     */
    fun cleanup() {
        hideTooltipRunnable?.let { handler.removeCallbacks(it) }
        hideTooltipRunnable = null
        handler.removeCallbacksAndMessages(null)

        tooltipView?.let {
            try {
                windowManager.removeView(it)
                Log.d(TAG, "Tooltip window removed")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to remove tooltip window", e)
            }
        }
        tooltipView = null
        tooltipContainer = null
        currentTapBehavior = null
        isInitialized = false
    }
}
