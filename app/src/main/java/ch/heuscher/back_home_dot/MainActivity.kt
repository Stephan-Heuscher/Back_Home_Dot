package ch.heuscher.back_home_dot

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat

class MainActivity : AppCompatActivity() {

    private lateinit var overlayStatusIcon: TextView
    private lateinit var overlayStatusText: TextView
    private lateinit var overlayPermissionButton: Button
    private lateinit var accessibilityStatusIcon: TextView
    private lateinit var accessibilityStatusText: TextView
    private lateinit var accessibilityButton: Button
    private lateinit var overlaySwitch: SwitchCompat
    private lateinit var settingsButton: Button
    private lateinit var stopServiceButton: Button

    private lateinit var settings: OverlaySettings
    private lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        settings = OverlaySettings(this)
        permissionManager = PermissionManager(this)

        initializeViews()
        setupClickListeners()
        updateUI()
    }

    override fun onResume() {
        super.onResume()
        updateUI()

        // Start service if enabled and permissions are granted
        if (settings.isEnabled && permissionManager.hasAllRequiredPermissions()) {
            startOverlayService()
        }
    }

    private fun initializeViews() {
        overlayStatusIcon = findViewById(R.id.overlay_status_icon)
        overlayStatusText = findViewById(R.id.overlay_status_text)
        overlayPermissionButton = findViewById(R.id.overlay_permission_button)
        accessibilityStatusIcon = findViewById(R.id.accessibility_status_icon)
        accessibilityStatusText = findViewById(R.id.accessibility_status_text)
        accessibilityButton = findViewById(R.id.accessibility_button)
        overlaySwitch = findViewById(R.id.overlay_switch)
        settingsButton = findViewById(R.id.settings_button)
        stopServiceButton = findViewById(R.id.stop_service_button)
    }

    private fun setupClickListeners() {
        overlayPermissionButton.setOnClickListener { requestOverlayPermission() }
        accessibilityButton.setOnClickListener { openAccessibilitySettings() }
        stopServiceButton.setOnClickListener { showStopServiceDialog() }
        settingsButton.setOnClickListener { openSettings() }

        overlaySwitch.setOnCheckedChangeListener { _, isChecked ->
            settings.isEnabled = isChecked
            if (permissionManager.hasAllRequiredPermissions()) {
                if (isChecked) {
                    startOverlayService()
                } else {
                    stopOverlayService()
                }
            }
            updateUI()
        }
    }

    private fun updateUI() {
        val hasOverlay = permissionManager.hasOverlayPermission()
        val hasAccessibility = permissionManager.hasAccessibilityPermission()

        // Update overlay permission status
        if (hasOverlay) {
            overlayStatusIcon.text = "✓"
            overlayStatusIcon.setTextColor(0xFF4CAF50.toInt()) // Green
            overlayStatusText.text = "Aktiviert"
            overlayPermissionButton.visibility = android.view.View.GONE
        } else {
            overlayStatusIcon.text = "✗"
            overlayStatusIcon.setTextColor(0xFFD32F2F.toInt()) // Red
            overlayStatusText.text = "Nicht aktiviert"
            overlayPermissionButton.visibility = android.view.View.VISIBLE
        }

        // Update accessibility permission status
        if (hasAccessibility) {
            accessibilityStatusIcon.text = "✓"
            accessibilityStatusIcon.setTextColor(0xFF4CAF50.toInt()) // Green
            accessibilityStatusText.text = "Aktiviert"
            accessibilityButton.visibility = android.view.View.GONE
        } else {
            accessibilityStatusIcon.text = "✗"
            accessibilityStatusIcon.setTextColor(0xFFD32F2F.toInt()) // Red
            accessibilityStatusText.text = "Nicht aktiviert"
            accessibilityButton.visibility = android.view.View.VISIBLE
        }

        // Update switch
        overlaySwitch.isChecked = settings.isEnabled
        overlaySwitch.isEnabled = hasOverlay && hasAccessibility

        // Update settings button
        settingsButton.isEnabled = hasOverlay && hasAccessibility
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                AlertDialog.Builder(this)
                    .setTitle("Overlay-Berechtigung erforderlich")
                    .setMessage("Diese Berechtigung erlaubt es der App, den Navigationspunkt über anderen Apps anzuzeigen.\n\nDies ist für die Hauptfunktion der App notwendig.")
                    .setPositiveButton("Einstellungen öffnen") { _, _ ->
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:$packageName")
                        )
                        startActivity(intent)
                    }
                    .setNegativeButton("Abbrechen", null)
                    .show()
            }
        }
    }

    private fun openAccessibilitySettings() {
        AlertDialog.Builder(this)
            .setTitle("Bedienungshilfe-Dienst erforderlich")
            .setMessage("Der Bedienungshilfe-Dienst ermöglicht der App, Systemnavigation durchzuführen (Zurück, Home, App-Wechsel).\n\nBitte aktivieren Sie \"Back Home Dot\" in den Bedienungshilfe-Einstellungen.")
            .setPositiveButton("Einstellungen öffnen") { _, _ ->
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    private fun openSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun showStopServiceDialog() {
        AlertDialog.Builder(this)
            .setTitle("Service beenden")
            .setMessage("Möchten Sie den Service wirklich beenden?\n\nDer Overlay-Service wird gestoppt. Der Bedienungshilfe-Dienst muss manuell in den System-Einstellungen deaktiviert werden.")
            .setPositiveButton("Beenden") { _, _ ->
                // Disable overlay
                settings.isEnabled = false
                overlaySwitch.isChecked = false
                stopOverlayService()
                updateUI()

                // Show info about accessibility service
                AlertDialog.Builder(this)
                    .setTitle("Service beendet")
                    .setMessage("Der Overlay-Service wurde beendet.\n\nUm die App vollständig zu deaktivieren, deaktivieren Sie bitte auch den Bedienungshilfe-Dienst in den System-Einstellungen.")
                    .setPositiveButton("Einstellungen öffnen") { _, _ ->
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        startActivity(intent)
                        closeApp()
                    }
                    .setNegativeButton("App schließen") { _, _ ->
                        closeApp()
                    }
                    .setOnDismissListener {
                        closeApp()
                    }
                    .show()
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    private fun closeApp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask()
        } else {
            finishAffinity()
        }
    }

    private fun startOverlayService() {
        val serviceIntent = Intent(this, OverlayService::class.java)
        startService(serviceIntent)
    }

    private fun stopOverlayService() {
        val serviceIntent = Intent(this, OverlayService::class.java)
        stopService(serviceIntent)
    }
}
