package ch.heuscher.back_home_dot

import android.app.AppOpsManager
import android.content.Context
import android.os.Build
import android.provider.Settings

/**
 * Centralized permission management
 */
class PermissionManager(private val context: Context) {

    /**
     * Check if overlay permission is granted
     */
    fun hasOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    /**
     * Check if accessibility service is enabled
     */
    fun hasAccessibilityPermission(): Boolean {
        return BackHomeAccessibilityService.isServiceEnabled()
    }

    /**
     * Check if usage stats permission is granted
     */
    fun hasUsageStatsPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return false
        }

        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Check if all required permissions are granted
     */
    fun hasAllRequiredPermissions(): Boolean {
        return hasOverlayPermission() && hasAccessibilityPermission()
    }

    /**
     * Check if all permissions including optional ones are granted
     */
    fun hasAllPermissions(): Boolean {
        return hasOverlayPermission() &&
               hasAccessibilityPermission() &&
               hasUsageStatsPermission()
    }
}
