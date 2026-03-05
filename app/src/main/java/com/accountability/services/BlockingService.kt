package com.accountability.services

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.accountability.core.BlockingManager
import com.accountability.data.AppDatabase
import com.accountability.ui.UnlockActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.isActive

class BlockingService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var database: AppDatabase
    private var monitoringJob: kotlinx.coroutines.Job? = null
    private var currentPackage: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        database = AppDatabase.getDatabase(applicationContext)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return
        
        // Skip self
        if (packageName == applicationContext.packageName) return
        
        // If switching to a new app, cancel old monitor
        if (packageName != currentPackage) {
            monitoringJob?.cancel()
            currentPackage = packageName
            startMonitoring(packageName)
        }
    }

    private fun startMonitoring(packageName: String) {
        monitoringJob = serviceScope.launch {
            // Observe the Rule (Limit) for changes
            // distinctUntilChanged ensures we don't restart the loop if only 'usedMinutes' changes
            database.appDao().getBlockedAppFlow(packageName)
                .distinctUntilChanged { old, new -> 
                    old?.limitMinutes == new?.limitMinutes && old?.packageName == new?.packageName 
                }
                .collectLatest { blockedApp ->
                    if (blockedApp == null) {
                        // Rule deleted or doesn't exist
                        return@collectLatest
                    }

                    // Loop for Usage Checking (with the valid limit)
                    while (kotlinx.coroutines.currentCoroutineContext().isActive) {
                        if (BlockingManager.isUnlocked(packageName)) {
                            kotlinx.coroutines.delay(30000) 
                            continue
                        }

                        val usageMillis = com.accountability.core.SystemUtils.getUsageForApp(applicationContext, packageName)
                        val usedMinutes = (usageMillis / 60000).toInt()

                        // Update DB if usage changed (this won't trigger re-collection due to distinctUntilChanged)
                        if (usedMinutes != blockedApp.usedMinutes) {
                             database.appDao().insertBlockedApp(blockedApp.copy(usedMinutes = usedMinutes))
                        }

                        if (usedMinutes >= blockedApp.limitMinutes) {
                            // Hard block: kick to home screen FIRST so the blocked
                            // app is no longer visible or in the foreground task
                            performGlobalAction(GLOBAL_ACTION_HOME)
                            kotlinx.coroutines.delay(300) // Let home screen render

                            val intent = Intent(applicationContext, UnlockActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            intent.putExtra("PACKAGE_NAME", packageName)
                            startActivity(intent)
                            
                            kotlinx.coroutines.delay(2000) 
                        } else {
                            // Check every 10 seconds
                            kotlinx.coroutines.delay(10000)
                        }
                    }
                }
        }
    }

    override fun onInterrupt() {}
}
