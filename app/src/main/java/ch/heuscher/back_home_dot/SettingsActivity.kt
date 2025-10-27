package ch.heuscher.back_home_dot

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.view.View

class SettingsActivity : AppCompatActivity() {

    private lateinit var alphaSeekBar: SeekBar
    private lateinit var alphaValueText: TextView
    private lateinit var timeoutSeekBar: SeekBar
    private lateinit var timeoutValueText: TextView
    private lateinit var advancedToggleCard: androidx.cardview.widget.CardView
    private lateinit var advancedContent: android.widget.LinearLayout
    private lateinit var advancedArrow: TextView
    private var isAdvancedExpanded = false

    private lateinit var settings: OverlaySettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Hide action bar since we have custom back button
        supportActionBar?.hide()

        settings = OverlaySettings(this)

        initializeViews()
        setupBackButton()
        setupAdvancedToggle()
        setupAlphaSeekBar()
        setupTimeoutSeekBar()
        setupColorButtons()
    }

    private fun initializeViews() {
        alphaSeekBar = findViewById(R.id.alpha_seekbar)
        alphaValueText = findViewById(R.id.alpha_value_text)
        timeoutSeekBar = findViewById(R.id.timeout_seekbar)
        timeoutValueText = findViewById(R.id.timeout_value_text)
        advancedToggleCard = findViewById(R.id.advanced_toggle_card)
        advancedContent = findViewById(R.id.advanced_content)
        advancedArrow = findViewById(R.id.advanced_arrow)
    }

    private fun setupBackButton() {
        findViewById<Button>(R.id.back_button).setOnClickListener {
            finish()
        }
    }

    private fun setupAdvancedToggle() {
        advancedToggleCard.setOnClickListener {
            toggleAdvanced()
        }
    }

    private fun toggleAdvanced() {
        isAdvancedExpanded = !isAdvancedExpanded
        advancedContent.visibility = if (isAdvancedExpanded) View.VISIBLE else View.GONE
        advancedArrow.text = if (isAdvancedExpanded) "▲" else "▼"
    }

    private fun setupAlphaSeekBar() {
        alphaSeekBar.progress = settings.alpha
        updateAlphaText(settings.alpha)

        alphaSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                settings.alpha = progress
                updateAlphaText(progress)
                notifyOverlayService()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun updateAlphaText(alpha: Int) {
        val percentage = (alpha * 100) / 255
        alphaValueText.text = "$percentage%"
    }

    private fun setupTimeoutSeekBar() {
        timeoutSeekBar.progress = settings.recentsTimeout.toInt()
        updateTimeoutText(settings.recentsTimeout.toInt())

        timeoutSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                settings.recentsTimeout = progress.toLong()
                updateTimeoutText(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun updateTimeoutText(timeout: Int) {
        timeoutValueText.text = "$timeout ms"
    }

    private fun setupColorButtons() {
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
        notifyOverlayService()
    }

    private fun notifyOverlayService() {
        if (settings.isEnabled) {
            // Restart service to apply changes
            val serviceIntent = Intent(this, OverlayService::class.java)
            stopService(serviceIntent)
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
            .setTitle(getString(R.string.choose_custom_color))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                val selectedColor = Color.rgb(redSeekBar.progress, greenSeekBar.progress, blueSeekBar.progress)
                setColor(0xFF000000.toInt() or selectedColor)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
}
