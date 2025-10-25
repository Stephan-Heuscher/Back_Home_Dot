package ch.heuscher.back_home_dot

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import android.view.View

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var overlayPermissionButton: Button
    private lateinit var accessibilityButton: Button
    private lateinit var usageStatsButton: Button
    private lateinit var overlaySwitch: SwitchCompat
    private lateinit var alphaSeekBar: SeekBar
    private lateinit var alphaValueText: TextView
    private lateinit var previewDot: View

    private lateinit var settings: OverlaySettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        settings = OverlaySettings(this)

        statusText = findViewById(R.id.status_text)
        overlayPermissionButton = findViewById(R.id.overlay_permission_button)
        accessibilityButton = findViewById(R.id.accessibility_button)
        usageStatsButton = findViewById(R.id.usage_stats_button)
        overlaySwitch = findViewById(R.id.overlay_switch)
        alphaSeekBar = findViewById(R.id.alpha_seekbar)
        alphaValueText = findViewById(R.id.alpha_value_text)
        previewDot = findViewById(R.id.preview_dot)

        overlayPermissionButton.setOnClickListener {
            requestOverlayPermission()
        }

        accessibilityButton.setOnClickListener {
            openAccessibilitySettings()
        }

        usageStatsButton.setOnClickListener {
            requestUsageStatsPermission()
        }

        setupOverlaySwitch()
        setupAlphaSeekBar()
        setupColorButtons()

        checkPermissionsAndStartService()
    }

    override fun onResume() {
        super.onResume()
        checkPermissionsAndStartService()
    }

    private fun checkPermissionsAndStartService() {
        val hasOverlayPermission = checkOverlayPermission()
        val hasAccessibilityService = BackHomeAccessibilityService.isServiceEnabled()
        val hasUsageStatsPermission = checkUsageStatsPermission()

        updateUI(hasOverlayPermission, hasAccessibilityService, hasUsageStatsPermission)

        if (hasOverlayPermission && hasAccessibilityService) {
            startOverlayService()
        }
    }

    private fun updateUI(hasOverlayPermission: Boolean, hasAccessibilityService: Boolean, hasUsageStatsPermission: Boolean) {
        val status = StringBuilder()
        status.append("Status:\n\n")

        if (hasOverlayPermission) {
            status.append("✓ Overlay-Berechtigung aktiviert\n")
            overlayPermissionButton.isEnabled = false
        } else {
            status.append("✗ Overlay-Berechtigung nicht aktiviert\n")
            overlayPermissionButton.isEnabled = true
        }

        if (hasAccessibilityService) {
            status.append("✓ Accessibility Service aktiviert\n")
            accessibilityButton.isEnabled = false
        } else {
            status.append("✗ Accessibility Service nicht aktiviert\n")
            accessibilityButton.isEnabled = true
        }

        if (hasUsageStatsPermission) {
            status.append("✓ App-Nutzungsstatistik aktiviert\n")
            usageStatsButton.isEnabled = false
        } else {
            status.append("✗ App-Nutzungsstatistik nicht aktiviert\n")
            usageStatsButton.isEnabled = true
        }

        if (hasOverlayPermission && hasAccessibilityService) {
            status.append("\n\nDer verschiebbare Punkt ist aktiv!\n\n")
            status.append("• Doppelklick = Zur vorherigen App")
            if (hasUsageStatsPermission) {
                status.append(" (direkt)\n")
            } else {
                status.append(" (via Recents)\n")
            }
            status.append("• Dreifachklick = App-Übersicht\n")
            status.append("• Langes Drücken = Home")
        }

        statusText.text = status.toString()
    }

    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                AlertDialog.Builder(this)
                    .setTitle(R.string.overlay_permission_needed)
                    .setMessage(R.string.please_enable_overlay)
                    .setPositiveButton(R.string.open_settings) { _, _ ->
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:$packageName")
                        )
                        startActivity(intent)
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }
        }
    }

    private fun openAccessibilitySettings() {
        AlertDialog.Builder(this)
            .setTitle(R.string.accessibility_service_needed)
            .setMessage(R.string.please_enable_accessibility)
            .setPositiveButton(R.string.open_settings) { _, _ ->
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun checkUsageStatsPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return false
        }

        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun requestUsageStatsPermission() {
        AlertDialog.Builder(this)
            .setTitle("App-Nutzungsstatistik Berechtigung")
            .setMessage("Für den direkten App-Wechsel (ohne Flackern) benötigt die App Zugriff auf die Nutzungsstatistik.\n\nBitte aktivieren Sie \"Back_Home_Dot\" in den Einstellungen.")
            .setPositiveButton(R.string.open_settings) { _, _ ->
                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun startOverlayService() {
        if (settings.isEnabled) {
            val serviceIntent = Intent(this, OverlayService::class.java)
            startService(serviceIntent)
        } else {
            stopOverlayService()
        }
    }

    private fun stopOverlayService() {
        val serviceIntent = Intent(this, OverlayService::class.java)
        stopService(serviceIntent)
    }

    private fun setupOverlaySwitch() {
        overlaySwitch.isChecked = settings.isEnabled
        overlaySwitch.setOnCheckedChangeListener { _, isChecked ->
            settings.isEnabled = isChecked
            if (checkOverlayPermission() && BackHomeAccessibilityService.isServiceEnabled()) {
                startOverlayService()
            }
        }
    }

    private fun setupAlphaSeekBar() {
        alphaSeekBar.progress = settings.alpha
        updateAlphaText(settings.alpha)

        alphaSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                settings.alpha = progress
                updateAlphaText(progress)
                updatePreview()
                notifyOverlayService()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        updatePreview()
    }

    private fun updateAlphaText(alpha: Int) {
        val percentage = (alpha * 100) / 255
        alphaValueText.text = "$percentage%"
    }

    private fun setupColorButtons() {
        // Get theme colors
        val typedValue = android.util.TypedValue()
        val theme = theme

        // Primary color button
        val primaryButton = findViewById<Button>(R.id.color_theme_primary)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)) {
                val primaryColor = typedValue.data
                primaryButton.backgroundTintList = android.content.res.ColorStateList.valueOf(primaryColor)
                primaryButton.setOnClickListener { setColor(primaryColor) }
            }
        } else {
            // Fallback for older versions
            primaryButton.setOnClickListener { setColor(0xFF2196F3.toInt()) }
        }

        // Secondary color button
        val secondaryButton = findViewById<Button>(R.id.color_theme_secondary)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (theme.resolveAttribute(android.R.attr.colorAccent, typedValue, true)) {
                val secondaryColor = typedValue.data
                secondaryButton.backgroundTintList = android.content.res.ColorStateList.valueOf(secondaryColor)
                secondaryButton.setOnClickListener { setColor(secondaryColor) }
            }
        } else {
            // Fallback for older versions
            secondaryButton.setOnClickListener { setColor(0xFF03DAC5.toInt()) }
        }

        // Preset colors
        findViewById<Button>(R.id.color_blue).setOnClickListener { setColor(0xFF2196F3.toInt()) }
        findViewById<Button>(R.id.color_red).setOnClickListener { setColor(0xFFF44336.toInt()) }
        findViewById<Button>(R.id.color_green).setOnClickListener { setColor(0xFF4CAF50.toInt()) }
        findViewById<Button>(R.id.color_orange).setOnClickListener { setColor(0xFFFF9800.toInt()) }
        findViewById<Button>(R.id.color_purple).setOnClickListener { setColor(0xFF9C27B0.toInt()) }
        findViewById<Button>(R.id.color_cyan).setOnClickListener { setColor(0xFF00BCD4.toInt()) }
        findViewById<Button>(R.id.color_yellow).setOnClickListener { setColor(0xFFFFEB3B.toInt()) }
        findViewById<Button>(R.id.color_gray).setOnClickListener { setColor(0xFF607D8B.toInt()) }

        // Custom color picker
        findViewById<Button>(R.id.color_custom).setOnClickListener { showColorPickerDialog() }
    }

    private fun setColor(color: Int) {
        settings.color = color
        updatePreview()
        notifyOverlayService()
    }

    private fun updatePreview() {
        val drawable = previewDot.background as? GradientDrawable
        if (drawable != null) {
            drawable.setColor(settings.getColorWithAlpha())
        } else {
            val newDrawable = GradientDrawable()
            newDrawable.shape = GradientDrawable.OVAL
            newDrawable.setColor(settings.getColorWithAlpha())
            newDrawable.setStroke(2, Color.WHITE)
            previewDot.background = newDrawable
        }
    }

    private fun notifyOverlayService() {
        if (settings.isEnabled && checkOverlayPermission() && BackHomeAccessibilityService.isServiceEnabled()) {
            // Restart service to apply changes
            stopOverlayService()
            val serviceIntent = Intent(this, OverlayService::class.java)
            startService(serviceIntent)
        }
    }

    private fun showColorPickerDialog() {
        val dialogView = layoutInflater.inflate(R.layout.color_picker_dialog, null)

        val colorPreview = dialogView.findViewById<View>(R.id.color_preview)
        val redSeekBar = dialogView.findViewById<SeekBar>(R.id.red_seekbar)
        val greenSeekBar = dialogView.findViewById<SeekBar>(R.id.green_seekbar)
        val blueSeekBar = dialogView.findViewById<SeekBar>(R.id.blue_seekbar)
        val redValue = dialogView.findViewById<TextView>(R.id.red_value)
        val greenValue = dialogView.findViewById<TextView>(R.id.green_value)
        val blueValue = dialogView.findViewById<TextView>(R.id.blue_value)

        // Initialize with current color
        val currentColor = settings.color
        val currentRed = Color.red(currentColor)
        val currentGreen = Color.green(currentColor)
        val currentBlue = Color.blue(currentColor)

        redSeekBar.progress = currentRed
        greenSeekBar.progress = currentGreen
        blueSeekBar.progress = currentBlue
        redValue.text = currentRed.toString()
        greenValue.text = currentGreen.toString()
        blueValue.text = currentBlue.toString()

        fun updateColorPreview() {
            val color = Color.rgb(redSeekBar.progress, greenSeekBar.progress, blueSeekBar.progress)
            colorPreview.setBackgroundColor(color)
        }

        updateColorPreview()

        redSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                redValue.text = progress.toString()
                updateColorPreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        greenSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                greenValue.text = progress.toString()
                updateColorPreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        blueSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                blueValue.text = progress.toString()
                updateColorPreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        AlertDialog.Builder(this)
            .setTitle("Eigene Farbe wählen")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                val selectedColor = Color.rgb(redSeekBar.progress, greenSeekBar.progress, blueSeekBar.progress)
                // Convert to ARGB format with full alpha
                setColor(0xFF000000.toInt() or selectedColor)
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }
}