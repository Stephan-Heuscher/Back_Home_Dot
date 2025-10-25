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

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var overlayPermissionButton: Button
    private lateinit var accessibilityButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.status_text)
        overlayPermissionButton = findViewById(R.id.overlay_permission_button)
        accessibilityButton = findViewById(R.id.accessibility_button)

        overlayPermissionButton.setOnClickListener {
            requestOverlayPermission()
        }

        accessibilityButton.setOnClickListener {
            openAccessibilitySettings()
        }

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
            status.append("• Kurzes Tippen = Zurück\n")
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
        val serviceIntent = Intent(this, OverlayService::class.java)
        startService(serviceIntent)
    }
}