package com.accountability.core

object BlockingManager {
    private val unlockedApps = mutableMapOf<String, Long>() // Package -> Expiry Timestamp

    fun isUnlocked(packageName: String): Boolean {
        val expiry = unlockedApps[packageName] ?: return false
        if (System.currentTimeMillis() > expiry) {
            unlockedApps.remove(packageName)
            return false
        }
        return true
    }

    fun unlock(packageName: String, durationMinutes: Int) {
        unlockedApps[packageName] = System.currentTimeMillis() + (durationMinutes * 60 * 1000)
    }
}
