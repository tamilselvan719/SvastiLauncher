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

    @Delete
    fun deleteItem(item: HomeItemEntity)
}