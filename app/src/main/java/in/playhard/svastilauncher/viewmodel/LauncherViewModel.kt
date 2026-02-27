package `in`.playhard.svastilauncher.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import `in`.playhard.svastilauncher.data.AppRepository
import `in`.playhard.svastilauncher.model.AppInfo
import `in`.playhard.svastilauncher.model.HomeItem

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

enum class LauncherScreen {
    HOME,
    DRAWER
}

@HiltViewModel
class LauncherViewModel @Inject constructor(private val repository: AppRepository) : ViewModel() {

    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps

    init {
        loadApps()
    }

    private val _currentScreen = MutableStateFlow(LauncherScreen.HOME)
    val currentScreen: StateFlow<LauncherScreen> = _currentScreen
    private val _homeItems = MutableStateFlow<List<HomeItem>>(emptyList())
    // ... (Keep your existing _apps and currentScreen logic) ...

    // 1. Convert the Room Flow into a StateFlow for Compose
    val homeItems: StateFlow<List<HomeItem>> = repository.getHomeItemsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onAppLongClicked(app: AppInfo) {
        val columns = 5
        val rows = 6

        // Grab the current snapshot of the grid
        val currentItems = homeItems.value

        for (y in 0 until rows) {
            for (x in 0 until columns) {
                // Check if the slot is occupied
                val isOccupied = currentItems.filterIsInstance<HomeItem.AppShortcut>()
                    .any { it.x == x && it.y == y }

                if (!isOccupied) {
                    // 2. Launch a coroutine to save to the database
                    viewModelScope.launch {
                        repository.addAppToHome(
                            packageName = app.packageName,
                            x = x,
                            y = y
                        )
                    }
                    return // Stop once we find the first empty spot
                }
            }
        }
    }


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

    fun moveItem(item: HomeItem.AppShortcut, newX: Int, newY: Int) {
        val currentItems = homeItems.value

        // Check if another app is already sitting in the target cell
        val isOccupied = currentItems.filterIsInstance<HomeItem.AppShortcut>()
            .any { it.x == newX && it.y == newY && it.app.packageName != item.app.packageName }

        if (!isOccupied) {
            viewModelScope.launch {
                repository.updateAppPosition(item.app.packageName, newX, newY)
            }
        }
    }

    fun removeApp(item: HomeItem.AppShortcut) {
        viewModelScope.launch {
            repository.removeAppFromHome(item.app.packageName, item.x, item.y)
        }
    }
}