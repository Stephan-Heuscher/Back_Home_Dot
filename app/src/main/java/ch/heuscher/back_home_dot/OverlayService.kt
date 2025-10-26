package ch.heuscher.back_home_dot

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null
    private var params: WindowManager.LayoutParams? = null
    private lateinit var settings: OverlaySettings

    private var initialX: Int = 0
    private var initialY: Int = 0
    private var initialTouchX: Float = 0f
    private var initialTouchY: Float = 0f

    private var isLongPress = false
    private var hasMoved = false
    private var clickCount = 0
    private var lastClickTime = 0L

    private val longPressHandler = Handler(Looper.getMainLooper())
    private val clickHandler = Handler(Looper.getMainLooper())

    // Use system timeouts
    private var longPressTimeout: Long = 500L
    private var doubleTapTimeout: Long = 300L
    private var touchSlop: Int = 10

    private val longPressRunnable = Runnable {
        isLongPress = true
        performHapticFeedback()
        BackHomeAccessibilityService.instance?.performHomeAction()
    }

    private val clickTimeoutRunnable = Runnable {
        handleClicks()
    }

    companion object {
        // Fallback values if system config unavailable
        private const val DEFAULT_LONG_PRESS_TIMEOUT = 500L
        private const val DEFAULT_DOUBLE_TAP_TIMEOUT = 300L
    }

    private fun performHapticFeedback() {
        floatingView?.performHapticFeedback(
            android.view.HapticFeedbackConstants.VIRTUAL_KEY,
            android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        )
    }

    private fun applyColorSettings() {
        val dotView = floatingView?.findViewById<View>(R.id.floating_dot)
        dotView?.let {
            val drawable = GradientDrawable()
            drawable.shape = GradientDrawable.OVAL
            drawable.setColor(settings.getColorWithAlpha())
            drawable.setStroke(2, android.graphics.Color.WHITE)
            it.background = drawable
        }
    }

    private fun handleClicks() {
        when (clickCount) {
            1 -> {
                // Single click - back action
                performHapticFeedback()
                BackHomeAccessibilityService.instance?.performBackAction()
            }
            2 -> {
                // Double click - switch to previous app
                performHapticFeedback()
                BackHomeAccessibilityService.instance?.performRecentsAction()
            }
            3 -> {
                // Triple click - open recent apps overview
                performHapticFeedback()
                BackHomeAccessibilityService.instance?.performRecentsOverviewAction()
            }
            else -> {
                // 4+ clicks - open main app
                if (clickCount >= 4) {
                    performHapticFeedback()
                    openMainActivity()
                }
            }
        }
        clickCount = 0
    }

    private fun openMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
    }

    override fun onCreate() {
        super.onCreate()

        settings = OverlaySettings(this)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Load system gesture timeouts
        val viewConfig = ViewConfiguration.get(this)
        longPressTimeout = ViewConfiguration.getLongPressTimeout().toLong()
        doubleTapTimeout = ViewConfiguration.getDoubleTapTimeout().toLong()
        touchSlop = viewConfig.scaledTouchSlop

        // Inflate the floating view layout
        floatingView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)

        // Apply color and alpha settings to the floating dot
        applyColorSettings()

        // Set up layout parameters
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        // Get current screen dimensions
        val displayMetrics = resources.displayMetrics
        val currentWidth = displayMetrics.widthPixels
        val currentHeight = displayMetrics.heightPixels

        // Load saved position using percentages for rotation stability
        val savedX: Int
        val savedY: Int

        if (settings.screenWidth > 0 && settings.screenHeight > 0) {
            // Convert saved percentages to current screen coordinates
            savedX = (settings.positionXPercent * currentWidth).toInt()
            savedY = (settings.positionYPercent * currentHeight).toInt()
        } else {
            // First run - use default pixel position
            savedX = settings.positionX
            savedY = settings.positionY
        }

        // Save current screen dimensions
        settings.screenWidth = currentWidth
        settings.screenHeight = currentHeight

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = savedX
            y = savedY
        }

        // Add the view to the window
        windowManager.addView(floatingView, params)

        // Set up touch listener for the floating view
        floatingView?.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params?.x ?: 0
                        initialY = params?.y ?: 0
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isLongPress = false
                        hasMoved = false

                        // Start long press timer using system timeout
                        longPressHandler.postDelayed(longPressRunnable, longPressTimeout)
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = event.rawX - initialTouchX
                        val deltaY = event.rawY - initialTouchY

                        // Check if the user has moved significantly using system touch slop
                        if (Math.abs(deltaX) > touchSlop || Math.abs(deltaY) > touchSlop) {
                            hasMoved = true
                            longPressHandler.removeCallbacks(longPressRunnable)

                            params?.x = initialX + deltaX.toInt()
                            params?.y = initialY + deltaY.toInt()
                            windowManager.updateViewLayout(floatingView, params)
                        }
                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        longPressHandler.removeCallbacks(longPressRunnable)

                        // Save position if moved
                        if (hasMoved) {
                            params?.let {
                                // Save as both pixels and percentages
                                settings.positionX = it.x
                                settings.positionY = it.y

                                // Get current screen dimensions
                                val displayMetrics = resources.displayMetrics
                                val currentWidth = displayMetrics.widthPixels
                                val currentHeight = displayMetrics.heightPixels

                                // Save position as percentage for rotation stability
                                settings.positionXPercent = it.x.toFloat() / currentWidth
                                settings.positionYPercent = it.y.toFloat() / currentHeight
                                settings.screenWidth = currentWidth
                                settings.screenHeight = currentHeight
                            }
                        }

                        if (!hasMoved && !isLongPress) {
                            // Tap detected - count clicks
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastClickTime < doubleTapTimeout) {
                                // Within double-click timeout
                                clickCount++
                                clickHandler.removeCallbacks(clickTimeoutRunnable)
                            } else {
                                // New click sequence
                                clickCount = 1
                            }
                            lastClickTime = currentTime

                            // Wait for potential additional clicks using system timeout
                            clickHandler.postDelayed(clickTimeoutRunnable, doubleTapTimeout)
                        }

                        return true
                    }
                }
                return false
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        floatingView?.let {
            windowManager.removeView(it)
            floatingView = null
        }
        longPressHandler.removeCallbacks(longPressRunnable)
        clickHandler.removeCallbacks(clickTimeoutRunnable)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
