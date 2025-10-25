package ch.heuscher.back_home_dot

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null
    private var params: WindowManager.LayoutParams? = null

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

    private val longPressRunnable = Runnable {
        isLongPress = true
        performHapticFeedback()
        BackHomeAccessibilityService.instance?.performHomeAction()
    }

    private val clickTimeoutRunnable = Runnable {
        handleClicks()
    }

    companion object {
        private const val LONG_PRESS_TIMEOUT = 500L
        private const val MOVE_THRESHOLD = 10
        private const val DOUBLE_CLICK_TIMEOUT = 300L
    }

    private fun performHapticFeedback() {
        floatingView?.performHapticFeedback(
            android.view.HapticFeedbackConstants.VIRTUAL_KEY,
            android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        )
    }

    private fun handleClicks() {
        when (clickCount) {
            1 -> {
                // Single click - do nothing (we only respond to double and triple clicks now)
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
        }
        clickCount = 0
    }

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Inflate the floating view layout
        floatingView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)

        // Set up layout parameters
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 100
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

                        // Start long press timer
                        longPressHandler.postDelayed(longPressRunnable, LONG_PRESS_TIMEOUT)
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = event.rawX - initialTouchX
                        val deltaY = event.rawY - initialTouchY

                        // Check if the user has moved significantly
                        if (Math.abs(deltaX) > MOVE_THRESHOLD || Math.abs(deltaY) > MOVE_THRESHOLD) {
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

                        if (!hasMoved && !isLongPress) {
                            // Tap detected - count clicks
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastClickTime < DOUBLE_CLICK_TIMEOUT) {
                                // Within double-click timeout
                                clickCount++
                                clickHandler.removeCallbacks(clickTimeoutRunnable)
                            } else {
                                // New click sequence
                                clickCount = 1
                            }
                            lastClickTime = currentTime

                            // Wait for potential additional clicks
                            clickHandler.postDelayed(clickTimeoutRunnable, DOUBLE_CLICK_TIMEOUT)
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
