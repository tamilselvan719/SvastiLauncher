package `in`.playhard.svastilauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import `in`.playhard.svastilauncher.data.AppRepository
import `in`.playhard.svastilauncher.ui.drawer.AppDrawer
import `in`.playhard.svastilauncher.ui.home.HomeScreen
import `in`.playhard.svastilauncher.ui.theme.SvastiLauncherTheme
import `in`.playhard.svastilauncher.viewmodel.LauncherViewModel
import `in`.playhard.svastilauncher.viewmodel.LauncherScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Manual initialization for now
        val repository = AppRepository(this)
        val viewModel = LauncherViewModel(repository)

        setContent {
            SvastiLauncherTheme {
                val apps by viewModel.apps.collectAsState()
                val currentScreen by viewModel.currentScreen.collectAsState()

                val homeItems by viewModel.homeItems.collectAsState()

                if (currentScreen == LauncherScreen.DRAWER) {
                    BackHandler {
                        viewModel.setScreen(LauncherScreen.HOME)
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (currentScreen) {
                        LauncherScreen.HOME -> {
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
                                    onItemMoved = { item, newX, newY ->
                                        // This is the ONLY place the ViewModel should be called
                                        viewModel.moveItem(item, newX, newY)
                                    },
                                    onItemRemoved = { item ->
                                        viewModel.removeApp(item)
                                    }
                                )
                            }
                        }
                        LauncherScreen.DRAWER -> {
                            AppDrawer(
                                apps = apps,
                                onAppClick = { app ->
                                    viewModel.onAppClicked(app)
                                    viewModel.setScreen(LauncherScreen.HOME)
                                },
                                onAppLongClick = { app ->
                                    viewModel.onAppLongClicked(app)
                                    viewModel.setScreen(LauncherScreen.HOME) // Go to home to see the new icon
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}