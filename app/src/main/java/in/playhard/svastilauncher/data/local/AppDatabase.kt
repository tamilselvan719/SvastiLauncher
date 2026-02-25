package `in`.playhard.svastilauncher.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [HomeItemEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // This tells Room to provide the implementation of your DAO
    abstract fun homeItemDao(): HomeItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // If the instance is not null, return it, otherwise create a new database instance.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "svasti_launcher_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}