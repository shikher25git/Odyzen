package com.accountability.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.accountability.R
import com.accountability.data.BlockedAppEntity
import com.accountability.ui.MainViewModel
import com.accountability.core.SystemUtils
import com.accountability.ui.theme.*

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onAddAppClick: () -> Unit
) {
    val apps by viewModel.blockedApps.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    var hasAccessPermission by remember { mutableStateOf(isAccessibilityServiceEnabled(context)) }
    var hasUsagePermission by remember { mutableStateOf(SystemUtils.hasUsageStatsPermission(context)) }
    
    // Check permission on resume
    DisposableEffect(androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
             if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                 hasAccessPermission = isAccessibilityServiceEnabled(context)
                 hasUsagePermission = SystemUtils.hasUsageStatsPermission(context)
             }
        }
        val lifecycle = androidx.lifecycle.ProcessLifecycleOwner.get().lifecycle
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    // ─── Permission dialogs (minimal Zen styling) ─────
    if (!hasAccessPermission) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(
                    "Accessibility Required",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    "To block apps, enable the 'Accountability' accessibility service.\n\nTap Enable → Installed Apps → Accountability → On.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { openAccessibilitySettings(context) }) {
                    Text("Enable")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
        )
    } else if (!hasUsagePermission) {
         AlertDialog(
            onDismissRequest = { },
            title = {
                Text(
                    "Usage Access Required",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    "To track app limits accurately, 'Usage Access' is required.\n\nTap Grant → Find Accountability → Switch On.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { 
                    context.startActivity(android.content.Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS))
                }) { Text("Grant") }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAppClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 1.dp,
                    pressedElevation = 2.dp
                )
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Block App")
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // ─── Zen header ─────
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "庭 · Dashboard",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Divider(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f)
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (apps.isEmpty()) {
                    // ─── Empty state ─────
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(48.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "一期一会",
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No apps blocked yet.\nTap + to begin.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(apps) { app ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(animationSpec = tween(400)),
                                exit = fadeOut(animationSpec = tween(300))
                            ) {
                                BlockedAppItem(
                                    app = app,
                                    onDelete = { viewModel.deleteBlockedApp(app.packageName) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun isAccessibilityServiceEnabled(context: android.content.Context): Boolean {
    val enabledServices = android.provider.Settings.Secure.getString(
        context.contentResolver,
        android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    )
    val colonSplitter = android.text.TextUtils.SimpleStringSplitter(':')
    colonSplitter.setString(enabledServices ?: "")
    val myService = "${context.packageName}/com.accountability.services.BlockingService"
    while (colonSplitter.hasNext()) {
        val componentName = colonSplitter.next()
        if (componentName.equals(myService, ignoreCase = true)) {
            return true
        }
    }
    return false
}

fun openAccessibilitySettings(context: android.content.Context) {
    val intent = android.content.Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
    context.startActivity(intent)
}

@Composable
fun BlockedAppItem(app: BlockedAppEntity, onDelete: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val appInfo = remember(app.packageName) {
        SystemUtils.getAppMetadata(context, app.packageName)
    }
    val label = appInfo?.label ?: app.packageName
    val iconBmp = appInfo?.icon

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (iconBmp != null) {
                androidx.compose.foundation.Image(
                    bitmap = iconBmp,
                    contentDescription = label,
                    modifier = Modifier.size(40.dp).padding(end = 12.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Limit: ${app.limitMinutes}m  ·  Used: ${app.usedMinutes}m",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun ConfigureBlockDialog(
    packageName: String,
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    onConfirm: (Int, List<String>) -> Unit
) {
    val secrets by viewModel.secrets.collectAsState()
    var limit by remember { mutableStateOf("15") }
    val selectedKeys = remember { mutableStateListOf<String>() }

    val context = androidx.compose.ui.platform.LocalContext.current
    val appLabel = remember(packageName) {
        SystemUtils.getAppMetadata(context, packageName)?.label ?: packageName
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Block $appLabel",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = limit,
                    onValueChange = { limit = it },
                    label = { Text("Daily Limit (mins)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    )
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    "Select keys to unlock:",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.height(150.dp)) {
                    items(secrets) { secret ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (selectedKeys.contains(secret.id)) {
                                        selectedKeys.remove(secret.id)
                                    } else {
                                        selectedKeys.add(secret.id)
                                    }
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = selectedKeys.contains(secret.id),
                                onCheckedChange = { chk ->
                                    if (chk) selectedKeys.add(secret.id) else selectedKeys.remove(secret.id)
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary,
                                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                secret.label,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val l = limit.toIntOrNull()
                if (l != null && selectedKeys.isNotEmpty()) {
                    onConfirm(l, selectedKeys.toList())
                }
            }) {
                Text("Block")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
    )
}
