package ch.heuscher.back_home_dot

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi

class AppSwitcherUtil(private val context: Context) {

    /**
     * Get the most recently used app (excluding our own app)
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun getPreviousApp(): String? {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return null

        val currentTime = System.currentTimeMillis()
        // Query for apps used in the last 10 seconds
        val queryTime = currentTime - 10000

        val usageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            queryTime,
            currentTime
        )

        if (usageStats.isNullOrEmpty()) {
            return null
        }

        // Sort by last time used
        val sortedStats = usageStats.sortedByDescending { it.lastTimeUsed }

        // Get our own package name
        val ourPackage = context.packageName

        // Find the most recent app that's not us
        for (stat in sortedStats) {
            if (stat.packageName != ourPackage && stat.lastTimeUsed > 0) {
                return stat.packageName
            }
        }

        return null
    }

    /**
     * Switch to the previous app
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun switchToPreviousApp(): Boolean {
        val previousPackage = getPreviousApp() ?: return false

        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(previousPackage)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
