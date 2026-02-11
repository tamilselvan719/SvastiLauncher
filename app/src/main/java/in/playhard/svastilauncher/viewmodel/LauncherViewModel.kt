package `in`.playhard.svastilauncher.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import `in`.playhard.svastilauncher.data.AppRepository
import `in`.playhard.svastilauncher.model.AppInfo
import `in`.playhard.svastilauncher.model.HomeItem

enum class LauncherScreen {
    HOME,
    DRAWER
}

class LauncherViewModel(private val repository: AppRepository) : ViewModel() {

    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps

    init {
        loadApps()
    }

    private val _currentScreen = MutableStateFlow(LauncherScreen.HOME)
    val currentScreen: StateFlow<LauncherScreen> = _currentScreen
    private val _homeItems = MutableStateFlow<List<HomeItem>>(emptyList())
    val homeItems: StateFlow<List<HomeItem>> = _homeItems


    fun setScreen(screen: LauncherScreen) {
        _currentScreen.value = screen
    }

    fun loadApps() {
        viewModelScope.launch {
            _apps.value = repository.getInstalledApps()
        }
    }

    fun onAppClicked(app: AppInfo) {
        repository.launchApp(app.packageName)
    }

    fun addAppToHome(app: AppInfo, x: Int, y: Int) {
        val newItem = HomeItem.AppShortcut(x = x, y = y, app = app)
        // basic check to prevent overlapping not implemented yet
        _homeItems.value += newItem
    }

    fun removeHomeItem(item: HomeItem) {
        _homeItems.value -= item
    }

    fun onAppLongClicked(app: AppInfo) {
        val columns = 5
        val rows = 6

        // Iterate through every cell to find the first empty one
        for (y in 0 until rows) {
            for (x in 0 until columns) {
                val isOccupied = _homeItems.value.any { it.x == x && it.y == y }
                if (!isOccupied) {
                    addAppToHome(app, x, y)
                    return // Stop once we find a spot
                }
            }
        }
    }
}