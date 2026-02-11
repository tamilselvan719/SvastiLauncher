package `in`.playhard.svastilauncher.ui.drawer

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import `in`.playhard.svastilauncher.model.AppInfo
import `in`.playhard.svastilauncher.ui.components.AppItem

@Composable
fun AppDrawer(apps: List<AppInfo>,
              onAppClick: (AppInfo) -> Unit,
              onAppLongClick: (AppInfo) -> Unit,
              modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier) {
        items(apps) { app ->
            AppItem(app = app,
                onClick = { onAppClick(app) },
                onLongClick = { onAppLongClick(app) })
        }
    }
}