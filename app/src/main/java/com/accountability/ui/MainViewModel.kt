package com.accountability.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.accountability.core.TotpUtils
import com.accountability.data.AppDatabase
import com.accountability.data.BlockedAppEntity
import com.accountability.data.SecretKeyEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import java.util.UUID
import com.accountability.core.AppInfo
import com.accountability.core.SystemUtils

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()

    val secrets = dao.getAllSecrets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blockedApps = dao.getAllBlockedApps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> = _installedApps.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _installedApps.value = SystemUtils.getInstalledApps(application)
        }
    }

    fun addSecret(label: String, duration: Int) {
        val secret = TotpUtils.generateSecret()
        val entity = SecretKeyEntity(
            id = UUID.randomUUID().toString(),
            label = label,
            secret = secret,
            duration = duration
        )
        viewModelScope.launch {
            dao.insertSecret(entity)
        }
    }

    fun deleteSecret(entity: SecretKeyEntity) {
        viewModelScope.launch {
            dao.deleteSecret(entity)
        }
    }

    fun addBlockedApp(packageName: String, limit: Int, secretIds: List<String>) {
        val entity = BlockedAppEntity(
            packageName = packageName,
            limitMinutes = limit,
            usedMinutes = 0,
            secretIds = secretIds
        )
        viewModelScope.launch {
            dao.insertBlockedApp(entity)
        }
    }

    fun deleteBlockedApp(packageName: String) {
        viewModelScope.launch {
            dao.deleteBlockedApp(packageName)
        }
    }
}
