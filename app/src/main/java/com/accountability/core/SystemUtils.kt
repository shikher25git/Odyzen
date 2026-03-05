package com.accountability.core

import android.content.Context
import android.content.pm.PackageManager

import android.graphics.drawable.Drawable
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

data class AppInfo(
    val label: String,
    val packageName: String,
    val icon: ImageBitmap? = null
)

object SystemUtils {
    // Target icon size in pixels — keeps textures small for smooth LazyColumn scroll
    private const val ICON_SIZE_PX = 96

    fun getInstalledApps(context: Context): List<AppInfo> {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        return apps.filter { app ->
            (pm.getLaunchIntentForPackage(app.packageName) != null)
        }.map { app ->
            val iconDrawable = try { pm.getApplicationIcon(app) } catch (e: Exception) { null }
            val bitmap = try {
                iconDrawable?.toBitmap(width = ICON_SIZE_PX, height = ICON_SIZE_PX)
            } catch (e: Exception) { null }
            val imageBitmap = try { bitmap?.asImageBitmap() } catch (e: Exception) { null }
            AppInfo(
                label = pm.getApplicationLabel(app).toString(),
                packageName = app.packageName,
                icon = imageBitmap
            )
        }.sortedBy { it.label }
    }

    fun getAppMetadata(context: Context, packageName: String): AppInfo? {
        val pm = context.packageManager
        return try {
            val appInfo = pm.getApplicationInfo(packageName, 0)
            val iconDrawable = try { pm.getApplicationIcon(appInfo) } catch (e: Exception) { null }
            val bitmap = try { iconDrawable?.toBitmap() } catch (e: Exception) { null }
            val imageBitmap = try { bitmap?.asImageBitmap() } catch (e: Exception) { null }
            AppInfo(
                label = pm.getApplicationLabel(appInfo).toString(),
                packageName = packageName,
                icon = imageBitmap
            )
        } catch (e: Exception) {
            null
        }
    }
    
    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
        val mode = appOps.checkOpNoThrow(
            android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == android.app.AppOpsManager.MODE_ALLOWED
    }

    fun getUsageForApp(context: Context, packageName: String): Long {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as android.app.usage.UsageStatsManager
        val calendar = java.util.Calendar.getInstance()
        val endTime = calendar.timeInMillis
        // Reset to start of day
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        val startTime = calendar.timeInMillis

        val stats = usageStatsManager.queryUsageStats(
            android.app.usage.UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )
        
        return stats.find { it.packageName == packageName }?.totalTimeInForeground ?: 0L
    }
}
