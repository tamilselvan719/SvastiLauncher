package `in`.playhard.svastilauncher

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import `in`.playhard.svastilauncher.ui.drawer.AppDrawer
import `in`.playhard.svastilauncher.ui.home.HomeScreen
import `in`.playhard.svastilauncher.ui.theme.SvastiLauncherTheme
import `in`.playhard.svastilauncher.viewmodel.LauncherScreen
import `in`.playhard.svastilauncher.viewmodel.LauncherViewModel

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

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

                val scope = rememberCoroutineScope()
                val configuration = LocalConfiguration.current
                val density = LocalDensity.current
                
                // Screen height in pixels for the drag calculation
                val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
                
                // The "Slider" state: 0f is Home, 1f is Drawer
                val drawerFraction = remember { Animatable(0f) }

                // Sync the ViewModel state with our interactive fraction
                LaunchedEffect(currentScreen) {
                    val target = if (currentScreen == LauncherScreen.DRAWER) 1f else 0f
                    drawerFraction.animateTo(target, tween(300))
                }

                // Background blur also follows the fraction live
                LaunchedEffect(drawerFraction.value) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val blurRadius = (drawerFraction.value * 80).toInt()
                        // Ensure radius is at least 1 when drawer is slightly open to avoid artifacts
                        window.setBackgroundBlurRadius(if (blurRadius > 0) blurRadius else 0)
                    }
                }

                if (currentScreen == LauncherScreen.DRAWER) {
                    BackHandler { viewModel.setScreen(LauncherScreen.HOME) }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .draggable(
                                orientation = Orientation.Vertical,
                                state = rememberDraggableState { delta ->
                                    // Use 20% of screen height as the "full travel" distance for the slider
                                    // This makes the drawer feel 5x lighter and more responsive
                                    val sensitivityFactor = 0.2f
                                    val newFraction = drawerFraction.value - (delta / (screenHeightPx * sensitivityFactor))
                                    scope.launch {
                                        drawerFraction.snapTo(newFraction.coerceIn(0f, 1f))
                                    }
                                },
                                onDragStopped = { velocity ->
                                    // Dead-zone: Ignore tiny velocities (jitters) under 200px/s
                                    val absVelocity = kotlin.math.abs(velocity)
                                    val isFlick = absVelocity > 600f
                                    
                                    val velocityFactor = (absVelocity / 4000f).coerceIn(0f, 1f)
                                    val dynamicThreshold = 0.5f - (velocityFactor * 0.4f)
                                    
                                    val isOpening = if (isFlick) {
                                        // On a fast flick, we trust the velocity direction
                                        velocity < 0 
                                    } else {
                                        // On a slow drag, we trust the distance traveled
                                        drawerFraction.value > dynamicThreshold
                                    }

                                    val target = if (isOpening) LauncherScreen.DRAWER else LauncherScreen.HOME
                                    viewModel.setScreen(target)
                                    
                                    scope.launch {
                                        drawerFraction.animateTo(
                                            targetValue = if (target == LauncherScreen.DRAWER) 1f else 0f,
                                            animationSpec = tween(300)
                                        )
                                    }
                                }
                            )
                    ) {
                        // Home Screen with interactive Alpha and Scale
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    alpha = 1f - (drawerFraction.value * 0.5f)
                                    val scale = 1f - (drawerFraction.value * 0.05f)
                                    scaleX = scale
                                    scaleY = scale
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

                        // Drawer with interactive Translation
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
                            onFractionUpdate = { delta ->
                                scope.launch {
                                    val sensitivityFactor = 0.2f
                                    val newFraction = drawerFraction.value - (delta / (screenHeightPx * sensitivityFactor))
                                    drawerFraction.snapTo(newFraction.coerceIn(0f, 1f))
                                }
                            },
                            onDragStopped = { velocity ->
                                val absVelocity = kotlin.math.abs(velocity)
                                val isFlick = absVelocity > 600f
                                val velocityFactor = (absVelocity / 4000f).coerceIn(0f, 1f)
                                val dynamicThreshold = 0.5f - (velocityFactor * 0.4f)
                                
                                val isOpening = if (isFlick) {
                                    velocity < 0 
                                } else {
                                    drawerFraction.value > dynamicThreshold
                                }

                                val target = if (isOpening) LauncherScreen.DRAWER else LauncherScreen.HOME
                                viewModel.setScreen(target)
                                
                                scope.launch {
                                    drawerFraction.animateTo(if (target == LauncherScreen.DRAWER) 1f else 0f, tween(300))
                                }
                            },
                            // CRITICAL: Keep drawer interactive as long as the state is DRAWER
                            // This prevents mid-gesture disruption while pulling down
                            isInteractive = currentScreen == LauncherScreen.DRAWER,
                            modifier = Modifier.offset {
                                IntOffset(0, ((1f - drawerFraction.value) * screenHeightPx).toInt())
                            }
                        )
                    }
                }
            }
        }
    }
}