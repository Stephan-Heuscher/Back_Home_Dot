package ch.heuscher.back_home_dot

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ImpressumActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_impressum)

        supportActionBar?.hide()

        // Setup back button
        findViewById<Button>(R.id.back_button).setOnClickListener {
            finish()
        }

        // Set version info
        val versionText = findViewById<TextView>(R.id.version_text)
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        versionText.text = "Version ${packageInfo.versionName} (Build ${packageInfo.longVersionCode})"

        // Setup Privacy Policy button
        findViewById<Button>(R.id.privacy_policy_button).setOnClickListener {
            openPrivacyPolicy()
        }

        // Setup Feedback button
        findViewById<Button>(R.id.feedback_button).setOnClickListener {
            sendFeedback()
        }
    }

    private fun openPrivacyPolicy() {
        val privacyPolicyUrl = getString(R.string.privacy_policy_url)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl))
        startActivity(intent)
    }

    private fun sendFeedback() {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}"
        val androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
        val appVersion = "${packageInfo.versionName} (Build ${packageInfo.longVersionCode})"

        val emailBody = getString(R.string.feedback_email_body, deviceModel, androidVersion, appVersion)
        val emailSubject = getString(R.string.feedback_email_subject)

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("s.heuscher@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, emailSubject)
            putExtra(Intent.EXTRA_TEXT, emailBody)
        }

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }
}