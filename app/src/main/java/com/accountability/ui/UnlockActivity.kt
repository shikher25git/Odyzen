package com.accountability.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.accountability.core.BlockingManager
import com.accountability.core.SystemUtils
import com.accountability.core.TotpUtils
import com.accountability.data.AppDatabase
import kotlinx.coroutines.launch

import com.accountability.ui.theme.AccountabilityTheme

class UnlockActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val packageName = intent.getStringExtra("PACKAGE_NAME") ?: return finish()
        
        setContent {
            AccountabilityTheme {
                UnlockScreen(packageName) { finish() }
            }
        }
    }

    @Composable
    fun UnlockScreen(packageName: String, onUnlock: () -> Unit) {
        var code by remember { mutableStateOf("") }
        var error by remember { mutableStateOf("") }
        
        val dao = AppDatabase.getDatabase(application).appDao()
        val scope = rememberCoroutineScope()
        
        val context = LocalContext.current
        val appInfo = remember(packageName) { SystemUtils.getAppMetadata(context, packageName) }
        val label = appInfo?.label ?: packageName
        val iconBmp = appInfo?.icon

        Column(
            modifier = Modifier.fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(40.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Zen lock icon — kanji instead of emoji
            Text(
                "封",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            if (iconBmp != null) {
                androidx.compose.foundation.Image(
                    bitmap = iconBmp,
                    contentDescription = label,
                    modifier = Modifier.size(64.dp).padding(bottom = 16.dp)
                )
            }
            
            Text(
                text = "Access Blocked",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(40.dp))
            
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("Enter TOTP Code") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            if (error.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Minimal outlined button — Zen style
            OutlinedButton(
                onClick = {
                    scope.launch {
                        val blockedApp = dao.getBlockedApp(packageName)
                        if (blockedApp != null) {
                            var isValid = false
                            for (secretId in blockedApp.secretIds) {
                                val secret = dao.getSecretById(secretId)
                                if (secret != null && TotpUtils.validate(secret.secret, code)) {
                                    BlockingManager.unlock(packageName, secret.duration)
                                    isValid = true
                                    break
                                }
                            }
                            
                            if (isValid) {
                                onUnlock()
                            } else {
                                error = "Invalid Code"
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Unlock", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
