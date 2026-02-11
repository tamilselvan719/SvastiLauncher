package `in`.playhard.svastilauncher.model

sealed class HomeItem {
    abstract val x: Int
    abstract val y: Int
    abstract val width: Int // For widgets later (spanX)
    abstract val height: Int // For widgets later (spanY)

    data class AppShortcut(
        override val x: Int,
        override val y: Int,
        val app: AppInfo,
        override val width: Int = 1,
        override val height: Int = 1
    ) : HomeItem()
}