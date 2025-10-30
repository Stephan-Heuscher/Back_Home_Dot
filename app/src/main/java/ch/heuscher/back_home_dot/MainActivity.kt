package ch.heuscher.back_home_dot

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

class MainActivity : AppCompatActivity() {

    private lateinit var permissionsSection: LinearLayout
    private lateinit var overlayPermissionCard: CardView
    private lateinit var overlayStatusIcon: TextView
    private lateinit var overlayStatusText: TextView
    private lateinit var overlayPermissionButton: Button
    private lateinit var accessibilityPermissionCard: CardView
    private lateinit var accessibilityStatusIcon: TextView
    private lateinit var accessibilityStatusText: TextView
    private lateinit var accessibilityButton: Button
    private lateinit var overlaySwitch: SwitchCompat
    private lateinit var settingsButton: Button
    private lateinit var stopServiceButton: Button
    private lateinit var adView: AdView

    private lateinit var settings: OverlaySettings
    private lateinit var permissionManager: PermissionManager

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        settings = OverlaySettings(this)
        permissionManager = PermissionManager(this)

        // Initialize Mobile Ads SDK
        MobileAds.initialize(this) {}

        initializeViews()
        setupClickListeners()
        updateUI()
        loadBannerAd()
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
        permissionsSection = findViewById(R.id.permissions_section)
        overlayPermissionCard = findViewById(R.id.overlay_permission_card)
        overlayStatusIcon = findViewById(R.id.overlay_status_icon)
        overlayStatusText = findViewById(R.id.overlay_status_text)
        overlayPermissionButton = findViewById(R.id.overlay_permission_button)
        accessibilityPermissionCard = findViewById(R.id.accessibility_permission_card)
        accessibilityStatusIcon = findViewById(R.id.accessibility_status_icon)
        accessibilityStatusText = findViewById(R.id.accessibility_status_text)
        accessibilityButton = findViewById(R.id.accessibility_button)
        overlaySwitch = findViewById(R.id.overlay_switch)
        settingsButton = findViewById(R.id.settings_button)
        stopServiceButton = findViewById(R.id.stop_service_button)
        adView = findViewById(R.id.adView)
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

        // Show/hide individual permission cards
        overlayPermissionCard.visibility = if (hasOverlay) View.GONE else View.VISIBLE
        accessibilityPermissionCard.visibility = if (hasAccessibility) View.GONE else View.VISIBLE

        // Show/hide entire permissions section
        permissionsSection.visibility = if (hasOverlay && hasAccessibility) View.GONE else View.VISIBLE

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
                    .setTitle(getString(R.string.allow_assistipoint_display))
                    .setMessage(getString(R.string.allow_assistipoint_message))
                    .setPositiveButton(getString(R.string.open)) { _, _ ->
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:$packageName")
                        )
                        startActivity(intent)
                    }
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show()
            }
        }
    }

    private fun openAccessibilitySettings() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.allow_navigation_title))
            .setMessage(getString(R.string.allow_navigation_message))
            .setPositiveButton(getString(R.string.open)) { _, _ ->
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun openSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun showStopServiceDialog() {
        // Stoppe Services, aber ändere den gespeicherten Status nicht
        // So bleibt beim nächsten App-Start der Switch-Status erhalten
        stopOverlayService()
        closeApp()
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

    private fun loadBannerAd() {
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }
}
