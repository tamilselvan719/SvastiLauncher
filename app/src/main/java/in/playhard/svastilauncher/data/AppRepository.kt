package `in`.playhard.svastilauncher.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import `in`.playhard.svastilauncher.model.AppInfo

class AppRepository(private val context: Context) {

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
}