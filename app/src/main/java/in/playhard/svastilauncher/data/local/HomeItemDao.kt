package `in`.playhard.svastilauncher.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HomeItemDao {
    @Query("SELECT * FROM home_items")
    fun getAllHomeItems(): Flow<List<HomeItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItem(item: HomeItemEntity)

    @Query("UPDATE home_items SET x = :newX, y = :newY WHERE packageName = :packageName")
    fun updateItemPosition(packageName: String, newX: Int, newY: Int)

    @Delete
    fun deleteItem(item: HomeItemEntity)

    @Query("DELETE FROM home_items WHERE packageName = :packageName AND x = :x AND y = :y")
    fun deleteItemByPosition(packageName: String, x: Int, y: Int)
}