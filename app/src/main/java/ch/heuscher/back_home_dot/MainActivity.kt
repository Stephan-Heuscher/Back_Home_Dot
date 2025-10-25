package ch.heuscher.back_home_dot

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

        updateUI(hasOverlayPermission, hasAccessibilityService)

        if (hasOverlayPermission && hasAccessibilityService) {
            startOverlayService()
        }
    }

    private fun updateUI(hasOverlayPermission: Boolean, hasAccessibilityService: Boolean) {
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

        if (hasOverlayPermission && hasAccessibilityService) {
            status.append("\n\nDer verschiebbare Punkt ist aktiv!\n\n")
            status.append("• Doppelklick = Zur vorherigen App\n")
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
        findViewById<Button>(R.id.color_blue).setOnClickListener { setColor(0xFF2196F3.toInt()) }
        findViewById<Button>(R.id.color_red).setOnClickListener { setColor(0xFFF44336.toInt()) }
        findViewById<Button>(R.id.color_green).setOnClickListener { setColor(0xFF4CAF50.toInt()) }
        findViewById<Button>(R.id.color_orange).setOnClickListener { setColor(0xFFFF9800.toInt()) }
        findViewById<Button>(R.id.color_purple).setOnClickListener { setColor(0xFF9C27B0.toInt()) }
        findViewById<Button>(R.id.color_cyan).setOnClickListener { setColor(0xFF00BCD4.toInt()) }
        findViewById<Button>(R.id.color_yellow).setOnClickListener { setColor(0xFFFFEB3B.toInt()) }
        findViewById<Button>(R.id.color_gray).setOnClickListener { setColor(0xFF607D8B.toInt()) }
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
}