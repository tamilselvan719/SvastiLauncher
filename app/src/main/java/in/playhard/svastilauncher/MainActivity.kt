package `in`.playhard.svastilauncher

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import `in`.playhard.svastilauncher.ui.drawer.AppDrawer
import `in`.playhard.svastilauncher.ui.home.HomeScreen
import `in`.playhard.svastilauncher.ui.theme.SvastiLauncherTheme
import `in`.playhard.svastilauncher.viewmodel.LauncherScreen
import `in`.playhard.svastilauncher.viewmodel.LauncherViewModel

import androidx.compose.animation.*
import androidx.compose.animation.core.tween

import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        }

        setContent {
            SvastiLauncherTheme {
                val viewModel: LauncherViewModel = hiltViewModel()
                val apps by viewModel.apps.collectAsState()
                val currentScreen by viewModel.currentScreen.collectAsState()
                val homeItems by viewModel.homeItems.collectAsState()

                val configuration = LocalConfiguration.current
                val screenHeight = configuration.screenHeightDp.dp
                
                // Animate the vertical offset based on the current screen state
                val drawerOffset by animateIntOffsetAsState(
                    targetValue = if (currentScreen == LauncherScreen.DRAWER) IntOffset.Zero 
                                 else IntOffset(0, 2000), // Push it way down when closed
                    animationSpec = tween(durationMillis = 300),
                    label = "DrawerSlide"
                )

                LaunchedEffect(currentScreen) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        window.setBackgroundBlurRadius(if (currentScreen == LauncherScreen.DRAWER) 80 else 0)
                    }
                }

                if (currentScreen == LauncherScreen.DRAWER) {
                    BackHandler { viewModel.setScreen(LauncherScreen.HOME) }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Home Screen is always at the back
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectVerticalDragGestures { _, dragAmount ->
                                        if (dragAmount < -50) {
                                            viewModel.setScreen(LauncherScreen.DRAWER)
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            HomeScreen(
                                items = homeItems,
                                onItemClick = { item -> viewModel.onAppClicked(item.app) },
                                onItemMoved = { item, newX, newY -> viewModel.moveItem(item, newX, newY) },
                                onItemRemoved = { item -> viewModel.removeApp(item) }
                            )
                        }

                        // Drawer slides over the Home Screen
                        AppDrawer(
                            apps = apps,
                            onAppClick = { app ->
                                viewModel.onAppClicked(app)
                                viewModel.setScreen(LauncherScreen.HOME)
                            },
                            onAppLongClick = { app ->
                                viewModel.onAppLongClicked(app)
                                viewModel.setScreen(LauncherScreen.HOME)
                            },
                            onClose = { viewModel.setScreen(LauncherScreen.HOME) },
                            modifier = Modifier.offset { drawerOffset }
                        )
                    }
                }
            }
        }
    }
}