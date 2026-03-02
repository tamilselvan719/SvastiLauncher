package `in`.playhard.svastilauncher.ui.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import `in`.playhard.svastilauncher.model.AppInfo
import `in`.playhard.svastilauncher.ui.components.AppItem

import androidx.compose.ui.unit.Velocity

@Composable
fun AppDrawer(apps: List<AppInfo>,
              onAppClick: (AppInfo) -> Unit,
              onAppLongClick: (AppInfo) -> Unit,
              onClose: () -> Unit,
              onFractionUpdate: (Float) -> Unit,
              onDragStopped: (Float) -> Unit,
              isInteractive: Boolean = true,
              modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // If swiping down while at the top of the list, update fraction
                if (available.y > 0 &&
                    listState.firstVisibleItemIndex == 0 &&
                    listState.firstVisibleItemScrollOffset == 0) {
                    onFractionUpdate(available.y)
                    return available // Consume the scroll so list doesn't move
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                // When finger is released, trigger momentum snapping
                onDragStopped(-available.y)
                return super.onPostFling(consumed, available)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
    ) {
        // Layer 1: Background Panel with Gradient and Blur
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xCC0D0F18),
                            Color(0xAA1A1B26)
                        )
                    )
                )
                .blur(30.dp)
        )

        // Layer 2: Foreground (App Items)
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = isInteractive // CRITICAL: Disable scroll while moving
        ) {
            items(apps) { app ->
                AppItem(app = app,
                    onClick = { onAppClick(app) },
                    onLongClick = { onAppLongClick(app) })
            }
        }
    }
}