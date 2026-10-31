package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.model.FavoriteItem
import com.example.data.model.IptvAccount
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.data.model.WatchHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface IptvDao {
    // Accounts
    @Query("SELECT * FROM iptv_accounts ORDER BY createdAt DESC")
    fun getAllAccounts(): Flow<List<IptvAccount>>

    @Query("SELECT * FROM iptv_accounts WHERE isActive = 1 LIMIT 1")
    fun getActiveAccount(): Flow<IptvAccount?>

    @Query("SELECT * FROM iptv_accounts WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveAccountOnce(): IptvAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: IptvAccount)

    @Query("UPDATE iptv_accounts SET isActive = 0")
    suspend fun deactivateAllAccounts()

    @Query("UPDATE iptv_accounts SET isActive = 1 WHERE id = :accountId")
    suspend fun activateAccount(accountId: String)

    @Transaction
    suspend fun switchActiveAccount(accountId: String) {
        deactivateAllAccounts()
        activateAccount(accountId)
    }

    @Query("DELETE FROM iptv_accounts WHERE id = :accountId")
    suspend fun deleteAccount(accountId: String)

    // Media Items
    @Query("SELECT * FROM media_items WHERE accountId = :accountId AND type = :type ORDER BY category, title")
    fun getMediaItems(accountId: String, type: MediaType): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE accountId = :accountId ORDER BY title")
    fun getAllMediaItems(accountId: String): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE id = :id LIMIT 1")
    suspend fun getMediaItemById(id: String): MediaItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaItems(items: List<MediaItem>)

    @Query("DELETE FROM media_items WHERE accountId = :accountId")
    suspend fun deleteMediaItemsByAccount(accountId: String)

    // Watch History
    @Query("SELECT * FROM watch_history ORDER BY timestamp DESC LIMIT 20")
    fun getWatchHistory(): Flow<List<WatchHistory>>

    @Query("SELECT * FROM watch_history WHERE streamId = :streamId LIMIT 1")
    suspend fun getWatchHistoryItem(streamId: String): WatchHistory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchHistory(history: WatchHistory)

    @Query("DELETE FROM watch_history WHERE streamId = :streamId")
    suspend fun deleteWatchHistory(streamId: String)

    @Query("DELETE FROM watch_history")
    suspend fun clearWatchHistory()

    // Favorites
    @Query("SELECT * FROM favorites ORDER BY timestamp DESC")
    fun getAllFavorites(): Flow<List<FavoriteItem>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE mediaId = :mediaId)")
    fun isFavorite(mediaId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE mediaId = :mediaId)")
    suspend fun isFavoriteOnce(mediaId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteItem)

    @Query("DELETE FROM favorites WHERE mediaId = :mediaId")
    suspend fun deleteFavorite(mediaId: String)
}
