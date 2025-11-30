package ch.heuscher.back_home_dot

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Onboarding activity that guides new users through the app's features.
 * Shows a multi-step tutorial with skip option.
 */
class OnboardingActivity : AppCompatActivity() {

    companion object {
        private const val PREFS_NAME = "onboarding_prefs"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val TOTAL_PAGES = 4

        /**
         * Check if onboarding has been completed.
         */
        fun isOnboardingCompleted(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        }

        /**
         * Mark onboarding as completed.
         */
        fun markOnboardingCompleted(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, true).apply()
        }
    }

    private var currentPage = 0

    private lateinit var titleText: TextView
    private lateinit var messageText: TextView
    private lateinit var iconImage: ImageView
    private lateinit var pageIndicator: TextView
    private lateinit var nextButton: Button
    private lateinit var skipButton: Button
    private lateinit var dot1: View
    private lateinit var dot2: View
    private lateinit var dot3: View
    private lateinit var dot4: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        supportActionBar?.hide()

        initializeViews()
        setupClickListeners()
        updatePage()
    }

    private fun initializeViews() {
        titleText = findViewById(R.id.onboarding_title)
        messageText = findViewById(R.id.onboarding_message)
        iconImage = findViewById(R.id.onboarding_icon)
        pageIndicator = findViewById(R.id.page_indicator)
        nextButton = findViewById(R.id.next_button)
        skipButton = findViewById(R.id.skip_button)
        dot1 = findViewById(R.id.dot1)
        dot2 = findViewById(R.id.dot2)
        dot3 = findViewById(R.id.dot3)
        dot4 = findViewById(R.id.dot4)
    }

    private fun setupClickListeners() {
        nextButton.setOnClickListener {
            if (currentPage < TOTAL_PAGES - 1) {
                currentPage++
                updatePage()
            } else {
                finishOnboarding()
            }
        }

        skipButton.setOnClickListener {
            finishOnboarding()
        }
    }

    private fun updatePage() {
        // Update content based on current page
        when (currentPage) {
            0 -> {
                titleText.text = getString(R.string.onboarding_welcome_title)
                messageText.text = getString(R.string.onboarding_welcome_message)
                iconImage.setImageResource(R.mipmap.ic_launcher)
            }
            1 -> {
                titleText.text = getString(R.string.onboarding_step1_title)
                messageText.text = getString(R.string.onboarding_step1_message)
                iconImage.setImageResource(R.drawable.ic_permissions)
            }
            2 -> {
                titleText.text = getString(R.string.onboarding_step2_title)
                messageText.text = getString(R.string.onboarding_step2_message)
                iconImage.setImageResource(R.drawable.ic_switch)
            }
            3 -> {
                titleText.text = getString(R.string.onboarding_step3_title)
                messageText.text = getString(R.string.onboarding_step3_message)
                iconImage.setImageResource(R.drawable.ic_tap)
            }
        }

        // Update page indicator text
        pageIndicator.text = getString(R.string.onboarding_page_indicator, currentPage + 1, TOTAL_PAGES)

        // Update button text on last page
        nextButton.text = if (currentPage == TOTAL_PAGES - 1) {
            getString(R.string.onboarding_done)
        } else {
            getString(R.string.onboarding_next)
        }

        // Update dot indicators
        updateDots()
    }

    private fun updateDots() {
        val activeDot = R.drawable.onboarding_dot_active
        val inactiveDot = R.drawable.onboarding_dot_inactive

        dot1.setBackgroundResource(if (currentPage == 0) activeDot else inactiveDot)
        dot2.setBackgroundResource(if (currentPage == 1) activeDot else inactiveDot)
        dot3.setBackgroundResource(if (currentPage == 2) activeDot else inactiveDot)
        dot4.setBackgroundResource(if (currentPage == 3) activeDot else inactiveDot)
    }

    private fun finishOnboarding() {
        markOnboardingCompleted(this)
        
        // Navigate to MainActivity
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (currentPage > 0) {
            currentPage--
            updatePage()
        } else {
            super.onBackPressed()
        }
    }
}
