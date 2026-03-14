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
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first

class BlockingService : AccessibilityService() {

    private val supervisorJob = SupervisorJob()
    private val serviceScope = CoroutineScope(supervisorJob + Dispatchers.IO)
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
            // Quick check: does this app even have a blocking rule?
            // If not, exit immediately — no polling, no battery drain
            val rule = database.appDao().getBlockedAppFlow(packageName).first()
            if (rule == null) return@launch

            // Observe the Rule for changes
            database.appDao().getBlockedAppFlow(packageName)
                .distinctUntilChanged { old, new -> 
                    old?.limitMinutes == new?.limitMinutes && old?.packageName == new?.packageName 
                }
                .collectLatest { blockedApp ->
                    if (blockedApp == null) {
                        // Rule deleted — stop monitoring entirely
                        return@collectLatest
                    }

                    // Polling loop — only runs while this app is in the foreground
                    while (kotlinx.coroutines.currentCoroutineContext()[kotlinx.coroutines.Job]?.isActive == true) {
                        if (BlockingManager.isUnlocked(packageName)) {
                            kotlinx.coroutines.delay(30000) 
                            continue
                        }

                        val usageMillis = com.accountability.core.SystemUtils.getUsageForApp(applicationContext, packageName)
                        val usedMinutes = (usageMillis / 60000).toInt()

                        // Update DB if usage changed
                        if (usedMinutes != blockedApp.usedMinutes) {
                             database.appDao().insertBlockedApp(blockedApp.copy(usedMinutes = usedMinutes))
                        }

                        if (usedMinutes >= blockedApp.limitMinutes) {
                            // Hard block: kick to home screen FIRST
                            performGlobalAction(GLOBAL_ACTION_HOME)
                            kotlinx.coroutines.delay(300)

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

    override fun onDestroy() {
        super.onDestroy()
        // Clean up all coroutines to prevent leaks
        supervisorJob.cancel()
    }
}
