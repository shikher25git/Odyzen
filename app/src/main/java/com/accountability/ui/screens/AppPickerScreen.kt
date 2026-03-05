package com.accountability.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.accountability.core.AppInfo
import com.accountability.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPickerScreen(viewModel: MainViewModel, onAppSelected: (String) -> Unit) {
    val installedApps by viewModel.installedApps.collectAsState()

    // Reactively track whether this screen is the foreground screen
    val lifecycleOwner = LocalLifecycleOwner.current
    var isScreenActive by remember { mutableStateOf(
        lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
    ) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, _ ->
            isScreenActive = lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val dividerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Select App",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + 16.dp,
                start = 0.dp,
                end = 0.dp
            )
        ) {
            items(
                items = installedApps,
                key = { it.packageName },
                contentType = { "app_row" }
            ) { app ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = isScreenActive) {
                            onAppSelected(app.packageName)
                        }
                        .drawBehind {
                            drawLine(
                                color = dividerColor,
                                start = Offset(64f, size.height),
                                end = Offset(size.width - 64f, size.height),
                                strokeWidth = 0.5.dp.toPx()
                            )
                        }
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (app.icon != null) {
                        Image(
                            bitmap = app.icon,
                            contentDescription = app.label,
                            modifier = Modifier.size(44.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.size(44.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        app.label,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

