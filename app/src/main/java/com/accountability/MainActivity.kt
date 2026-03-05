package com.accountability

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.navArgument
import com.accountability.ui.MainViewModel
import com.accountability.ui.screens.*
import com.accountability.ui.theme.AccountabilityTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AccountabilityTheme {
                AccountabilityApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountabilityApp() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel()
    
    val items = listOf("dashboard" to "Home", "keys" to "Keys", "info" to "Info")
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route?.split("/")?.first()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (currentRoute in listOf("dashboard", "keys", "info")) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.background,
                    tonalElevation = 0.dp,
                ) {
                    NavigationBarItem(
                        icon = {
                            Box(
                                modifier = if (currentRoute == "dashboard") 
                                    Modifier.border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium)
                                            .padding(horizontal = 20.dp, vertical = 4.dp)
                                else 
                                    Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.Home,
                                    null,
                                    tint = if (currentRoute == "dashboard")
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        label = {
                            Text(
                                "Home",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (currentRoute == "dashboard")
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        selected = currentRoute == "dashboard",
                        onClick = {
                            navController.navigate("dashboard") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.background,
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    NavigationBarItem(
                        icon = {
                            Box(
                                modifier = if (currentRoute == "keys") 
                                    Modifier.border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium)
                                            .padding(horizontal = 20.dp, vertical = 4.dp)
                                else 
                                    Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.VpnKey,
                                    null,
                                    tint = if (currentRoute == "keys")
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        label = {
                            Text(
                                "Keys",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (currentRoute == "keys")
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        selected = currentRoute == "keys",
                        onClick = {
                            navController.navigate("keys") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.background,
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    NavigationBarItem(
                        icon = {
                            Box(
                                modifier = if (currentRoute == "info") 
                                    Modifier.border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium)
                                            .padding(horizontal = 20.dp, vertical = 4.dp)
                                else 
                                    Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.Info,
                                    null,
                                    tint = if (currentRoute == "info")
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        label = {
                            Text(
                                "Info",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (currentRoute == "info")
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        selected = currentRoute == "info",
                        onClick = {
                            navController.navigate("info") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.background,
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(navController, startDestination = "loader", modifier = Modifier.padding(padding)) {
            composable("loader") {
                LoaderScreen(onFinished = {
                    navController.navigate("dashboard") {
                        popUpTo("loader") { inclusive = true }
                    }
                })
            }
            composable("dashboard") {
                ExitBackHandler()
                DashboardScreen(
                    viewModel = viewModel,
                    onAddAppClick = { navController.navigate("app_picker") }
                )
            }
            composable("keys") {
                ExitBackHandler()
                KeysScreen(viewModel = viewModel)
            }
            composable("info") {
                ExitBackHandler()
                InfoScreen()
            }
            composable("app_picker") {
                AppPickerScreen(
                    viewModel = viewModel,
                    onAppSelected = { pkg ->
                        navController.navigate("configure_block/$pkg")
                    }
                )
            }
            dialog(
                "configure_block/{packageName}",
                arguments = listOf(navArgument("packageName") { type = NavType.StringType })
            ) { entry ->
                val packageName = entry.arguments?.getString("packageName") ?: ""
                ConfigureBlockDialog(
                    packageName = packageName,
                    viewModel = viewModel,
                    onDismiss = { navController.popBackStack() },
                    onConfirm = { limit, keys ->
                        viewModel.addBlockedApp(packageName, limit, keys)
                        navController.popBackStack("dashboard", inclusive = false)
                    }
                )
            }
        }
    }
}

@Composable
fun ExitBackHandler() {
    val context = LocalContext.current
    var backPressedTime by remember { mutableStateOf(0L) }
    BackHandler {
        if (System.currentTimeMillis() - backPressedTime < 2000) {
            (context as? android.app.Activity)?.finish()
        } else {
            backPressedTime = System.currentTimeMillis()
            Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun LoaderScreen(onFinished: () -> Unit) {
    val scale = remember { androidx.compose.animation.core.Animatable(1f) }
    val alpha = remember { androidx.compose.animation.core.Animatable(0f) }

    LaunchedEffect(Unit) {
        // Run animations concurrently
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = androidx.compose.animation.core.tween(durationMillis = 1000)
            )
        }
        launch {
            scale.animateTo(
                targetValue = 1.1f,
                animationSpec = androidx.compose.animation.core.tween(durationMillis = 2000)
            )
        }
        
        delay(2000) // 2 second pause
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.horyuji_pagoda_ai_transparent),
            contentDescription = "Loading...",
            modifier = Modifier
                .size(192.dp) // Match launcher icon bounds
                .scale(scale.value)
                .alpha(alpha.value),
            contentScale = ContentScale.Fit,
        )
    }
}
