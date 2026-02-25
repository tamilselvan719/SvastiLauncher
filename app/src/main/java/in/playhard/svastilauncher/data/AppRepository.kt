package `in`.playhard.svastilauncher.data // Adjust to your actual package

import android.content.Context
import android.content.Intent
import `in`.playhard.svastilauncher.data.local.AppDatabase
import `in`.playhard.svastilauncher.data.local.HomeItemEntity
import `in`.playhard.svastilauncher.model.AppInfo
import `in`.playhard.svastilauncher.model.HomeItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(private val context: Context) {
    private val packageManager = context.packageManager

    // Initialize the Room DAO
    private val homeItemDao = AppDatabase.getDatabase(context).homeItemDao()

    fun getInstalledApps(): List<AppInfo> {
        val apps = mutableListOf<AppInfo>()
        val manager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = manager.queryIntentActivities(intent, 0)

        for (info in resolveInfos) {
            apps.add(
                AppInfo(
                    label = info.loadLabel(manager).toString(),
                    packageName = info.activityInfo.packageName,
                    icon = info.loadIcon(manager)
                )
            )
        }
        return apps.sortedBy { it.label.lowercase() }
    }

    fun launchApp(packageName: String) {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            context.startActivity(intent)
        }
    }

    /**
     * Reads the database and converts flat entities into UI-ready items with icons.
     */
    fun getHomeItemsFlow(): Flow<List<HomeItem>> {
        return homeItemDao.getAllHomeItems().map { entities ->
            entities.mapNotNull { entity ->
                // Try to find the app on the device
                val intent = packageManager.getLaunchIntentForPackage(entity.packageName)
                if (intent != null) {
                    val resolveInfo = packageManager.resolveActivity(intent, 0)
                    resolveInfo?.let {
                        val appInfo = AppInfo(
                            label = it.loadLabel(packageManager).toString(),
                            packageName = entity.packageName,
                            icon = it.loadIcon(packageManager)
                        )
                        HomeItem.AppShortcut(x = entity.x, y = entity.y, app = appInfo)
                    }
                } else {
                    // If intent is null, the app was likely uninstalled.
                    // mapNotNull automatically filters this out of the final list.
                    null
                }
            }
        }
    }

    /**
     * Inserts a new shortcut into the database.
     */
    suspend fun addAppToHome(packageName: String, x: Int, y: Int) {
        withContext(Dispatchers.IO) {
            val entity = HomeItemEntity(
                x = x,
                y = y,
                packageName = packageName
            )
            homeItemDao.insertItem(entity)
        }
    }
}