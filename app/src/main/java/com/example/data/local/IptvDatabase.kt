package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.FavoriteItem
import com.example.data.model.IptvAccount
import com.example.data.model.MediaItem
import com.example.data.model.WatchHistory

@Database(
    entities = [
        IptvAccount::class,
        MediaItem::class,
        WatchHistory::class,
        FavoriteItem::class
    ],
    version = 1,
    exportSchema = false
)
abstract class IptvDatabase : RoomDatabase() {
    abstract fun iptvDao(): IptvDao

    companion object {
        @Volatile
        private var INSTANCE: IptvDatabase? = null

        fun getInstance(context: Context): IptvDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    IptvDatabase::class.java,
                    "nova_iptv.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
