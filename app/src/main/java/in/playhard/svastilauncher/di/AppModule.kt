package `in`.playhard.svastilauncher.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import `in`.playhard.svastilauncher.data.AppRepository
import `in`.playhard.svastilauncher.data.local.AppDatabase
import `in`.playhard.svastilauncher.data.local.HomeItemDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideHomeItemDao(database: AppDatabase): HomeItemDao {
        return database.homeItemDao()
    }

    @Provides
    @Singleton
    fun provideAppRepository(
        @ApplicationContext context: Context,
        homeItemDao: HomeItemDao
    ): AppRepository {
        return AppRepository(context, homeItemDao)
    }
}
