package ch.heuscher.back_home_dot

import android.app.ActivityManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * Utility class for switching between apps
 * Uses different methods based on Android version and available permissions
 */
class AppSwitcherUtil(private val context: Context) {

    private val permissionManager = PermissionManager(context)

    /**
     * Switch to the previous app using the best available method
     */
    fun switchToPreviousApp(): Boolean {
        // Try ActivityManager first (works on newer Android versions)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (tryActivityManagerSwitch()) {
                return true
            }
        }

        // Fallback to UsageStatsManager if permission is granted
        if (permissionManager.hasUsageStatsPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (tryUsageStatsSwitch()) {
                    return true
                }
            }
        }

        // All methods failed
        return false
    }

    /**
     * Try to switch using ActivityManager (preferred method for newer Android)
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun tryActivityManagerSwitch(): Boolean {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
                ?: return false

            // Get list of recent tasks
            val recentTasks = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activityManager.appTasks
            } else {
                @Suppress("DEPRECATION")
                val tasks = activityManager.getRunningTasks(10)
                null // Fall through to UsageStats method
            }

            if (recentTasks != null && recentTasks.size >= 2) {
                // Move to front the second task (previous app)
                val previousTask = recentTasks[1]
                previousTask.moveToFront()
                return true
            }

            false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Try to switch using UsageStatsManager
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun tryUsageStatsSwitch(): Boolean {
        val previousPackage = getPreviousAppFromUsageStats() ?: return false

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

    /**
     * Get the most recently used app from UsageStats (excluding our own app)
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getPreviousAppFromUsageStats(): String? {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return null

        val currentTime = System.currentTimeMillis()
        val queryTime = currentTime - 10000 // Last 10 seconds

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
        val ourPackage = context.packageName

        // Find the most recent app that's not us
        for (stat in sortedStats) {
            if (stat.packageName != ourPackage && stat.lastTimeUsed > 0) {
                return stat.packageName
            }
        }

        return null
    }
}
