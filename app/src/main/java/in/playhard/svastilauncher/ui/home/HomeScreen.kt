package `in`.playhard.svastilauncher.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import `in`.playhard.svastilauncher.model.HomeItem
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.IntOffset
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlin.math.roundToInt
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset

@Composable
fun HomeScreen(
    items: List<HomeItem>,
    onItemClick: (HomeItem.AppShortcut) -> Unit,
    modifier: Modifier = Modifier,
    onItemMoved: (item: HomeItem.AppShortcut, newX: Int, newY: Int) -> Unit,
    onItemRemoved: (item: HomeItem.AppShortcut) -> Unit
) {
    val columns = 5
    val rows = 6
    var draggedItem by remember { mutableStateOf<HomeItem.AppShortcut?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var targetCell by remember { mutableStateOf<IntOffset?>(null) }
    var isHoveringRemove by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val cellWidth = this.maxWidth / columns
        val cellHeight = this.maxHeight / rows
        val cellWidthPx = constraints.maxWidth / columns.toFloat()
        val cellHeightPx = constraints.maxHeight / rows.toFloat()
        val density = LocalDensity.current

        // Draw the Remove Zone (appears when dragging)
        if (draggedItem != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .background(
                        if (isHoveringRemove) Color.Red.copy(alpha = 0.8f)
                        else Color.Black.copy(alpha = 0.5f)
                    )
                    .zIndex(2f),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                androidx.compose.material3.Text(
                    text = if (isHoveringRemove) "Release to Remove" else "Remove",
                    color = Color.White
                )
            }
        }

        // Draw the Highlight Box
        targetCell?.let { target ->
            // Convert our pixel dimensions to Dp for the modifiers
            val cellWidthDp = with(density) { cellWidthPx.toDp() }
            val cellHeightDp = with(density) { cellHeightPx.toDp() }
            val offsetX = with(density) { (target.x * cellWidthPx).toDp() }
            val offsetY = with(density) { (target.y * cellHeightPx).toDp() }

            Box(
                modifier = Modifier
                    .offset(x = offsetX, y = offsetY)
                    .size(width = cellWidthDp, height = cellHeightDp)
                    .background(
                        color = Color.White.copy(alpha = 0.2f), // A subtle semi-transparent white
                        shape = RoundedCornerShape(16.dp)       // Smooth rounded corners
                    )
            )
        }
        Box(modifier = Modifier.fillMaxSize()) {
            items.forEach { item ->
                if (item is HomeItem.AppShortcut) {
                    val isDraggingThis = draggedItem == item
                    val xOffset = cellWidth * item.x
                    val yOffset = cellHeight * item.y

                    HomeItemView(
                        item = item,
                        onClick = { onItemClick(item) },
                        modifier = Modifier
                            .offset(x = xOffset, y = yOffset)
                            .size(cellWidth, cellHeight)
                            .zIndex(if (isDraggingThis) 1f else 0f)
                            .offset {
                                if (isDraggingThis) {
                                    IntOffset(dragOffset.x.roundToInt(), dragOffset.y.roundToInt())
                                } else {
                                    IntOffset.Zero
                                }
                            }
                            .pointerInput(item) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        draggedItem = item
                                        dragOffset = Offset.Zero
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffset += dragAmount

                                        val absoluteY = (item.y * cellHeightPx) + dragOffset.y
                                        isHoveringRemove = absoluteY <= 10f && dragOffset.y < -10f
                                        if (!isHoveringRemove) {
                                            val xCellsMoved =
                                                (dragOffset.x / cellWidthPx).roundToInt()
                                            val yCellsMoved =
                                                (dragOffset.y / cellHeightPx).roundToInt()
                                            targetCell = IntOffset(
                                                (item.x + xCellsMoved).coerceIn(0, columns - 1),
                                                (item.y + yCellsMoved).coerceIn(0, rows - 1)
                                            )
                                        } else {
                                            targetCell = null
                                        }
                                    },
                                    onDragEnd = {
                                        // Decide whether to Remove or Move
                                        if (isHoveringRemove) {
                                            onItemRemoved(item)
                                        } else {
                                            targetCell?.let { target ->
                                                onItemMoved(item, target.x, target.y)
                                            }
                                        }

                                        draggedItem = null
                                        dragOffset = Offset.Zero
                                        targetCell = null
                                        isHoveringRemove = false
                                    },
                                    onDragCancel = {
                                        draggedItem = null
                                        dragOffset = Offset.Zero
                                        targetCell = null
                                        isHoveringRemove = false
                                    }
                                )
                            }
                    )
                }
            }
        }
    }
}