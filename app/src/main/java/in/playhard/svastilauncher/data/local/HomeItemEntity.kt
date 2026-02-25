package `in`.playhard.svastilauncher.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "home_items")
data class HomeItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val x: Int,
    val y: Int,
    val width: Int = 1,
    val height: Int = 1,
    val packageName: String,
    val itemType: String = "APP_SHORTCUT" // Helps us distinguish widgets later
)