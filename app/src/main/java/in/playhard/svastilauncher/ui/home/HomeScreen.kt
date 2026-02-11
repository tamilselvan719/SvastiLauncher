package `in`.playhard.svastilauncher.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import `in`.playhard.svastilauncher.model.HomeItem

@Composable
fun HomeScreen(
    items: List<HomeItem>,
    onItemClick: (HomeItem.AppShortcut) -> Unit,
    modifier: Modifier = Modifier
) {
    val columns = 5
    val rows = 6

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        // We use 'this' to explicitly use the scope variables
        val cellWidth = this.maxWidth / columns
        val cellHeight = this.maxHeight / rows

        Box(modifier = Modifier.fillMaxSize()) {
            items.forEach { item ->
                if (item is HomeItem.AppShortcut) {
                    val xOffset = cellWidth * item.x
                    val yOffset = cellHeight * item.y

                    HomeItemView(
                        item = item,
                        onClick = { onItemClick(item) },
                        modifier = Modifier
                            .offset(x = xOffset, y = yOffset)
                            .size(cellWidth, cellHeight)
                    )
                }
            }
        }
    }
}