package com.example.data.repository

import android.content.Context
import com.example.data.demo.DemoContentProvider
import com.example.data.local.IptvDao
import com.example.data.model.AccountType
import com.example.data.model.FavoriteItem
import com.example.data.model.IptvAccount
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.data.model.WatchHistory
import com.example.data.network.XtreamClient
import com.example.data.parser.M3uParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.UUID

class IptvRepository(
    private val dao: IptvDao,
    private val xtreamClient: XtreamClient = XtreamClient(),
    private val okHttpClient: OkHttpClient = OkHttpClient()
) {

    val allAccounts: Flow<List<IptvAccount>> = dao.getAllAccounts()
    val activeAccount: Flow<IptvAccount?> = dao.getActiveAccount()
    val watchHistory: Flow<List<WatchHistory>> = dao.getWatchHistory()
    val allFavorites: Flow<List<FavoriteItem>> = dao.getAllFavorites()

    fun getMediaItems(accountId: String, type: MediaType): Flow<List<MediaItem>> =
        dao.getMediaItems(accountId, type)

    fun getAllMediaItems(accountId: String): Flow<List<MediaItem>> =
        dao.getAllMediaItems(accountId)

    suspend fun initializeDemoAccountIfNeeded() = withContext(Dispatchers.IO) {
        val currentActive = dao.getActiveAccountOnce()
        if (currentActive == null) {
            val demoAccount = DemoContentProvider.DEMO_ACCOUNT
            dao.insertAccount(demoAccount)
            val demoItems = DemoContentProvider.getDemoMediaItems(demoAccount.id)
            dao.insertMediaItems(demoItems)
        }
    }

    suspend fun loadDemoShowcase(): Result<IptvAccount> = withContext(Dispatchers.IO) {
        try {
            val demoAccount = DemoContentProvider.DEMO_ACCOUNT
            dao.deactivateAllAccounts()
            dao.insertAccount(demoAccount)
            dao.deleteMediaItemsByAccount(demoAccount.id)
            val demoItems = DemoContentProvider.getDemoMediaItems(demoAccount.id)
            dao.insertMediaItems(demoItems)
            Result.success(demoAccount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addXtreamAccount(
        name: String,
        serverUrl: String,
        username: String,
        password: String
    ): Result<IptvAccount> = withContext(Dispatchers.IO) {
        val accountId = "xtream_${UUID.randomUUID().toString().take(8)}"
        val syncResult = xtreamClient.authenticateAndSync(
            serverUrl = serverUrl,
            username = username,
            password = password,
            accountId = accountId
        )

        syncResult.fold(
            onSuccess = { items ->
                val liveCount = items.count { it.type == MediaType.LIVE }
                val movieCount = items.count { it.type == MediaType.MOVIE }
                val seriesCount = items.count { it.type == MediaType.SERIES }

                val account = IptvAccount(
                    id = accountId,
                    name = name.ifEmpty { "Xtream ($username)" },
                    type = AccountType.XTREAM,
                    serverUrl = serverUrl,
                    username = username,
                    password = password,
                    isActive = true,
                    channelCount = liveCount,
                    movieCount = movieCount,
                    seriesCount = seriesCount
                )

                dao.deactivateAllAccounts()
                dao.insertAccount(account)
                dao.deleteMediaItemsByAccount(accountId)
                dao.insertMediaItems(items)
                Result.success(account)
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    suspend fun addM3uAccount(
        name: String,
        m3uUrl: String
    ): Result<IptvAccount> = withContext(Dispatchers.IO) {
        try {
            val accountId = "m3u_${UUID.randomUUID().toString().take(8)}"
            val request = Request.Builder().url(m3uUrl).build()
            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Failed to download playlist: HTTP ${response.code}"))
            }

            val inputStream = response.body?.byteStream()
                ?: return@withContext Result.failure(Exception("Empty playlist response"))

            val items = M3uParser.parse(inputStream, accountId)
            val liveCount = items.count { it.type == MediaType.LIVE }
            val movieCount = items.count { it.type == MediaType.MOVIE }
            val seriesCount = items.count { it.type == MediaType.SERIES }

            val account = IptvAccount(
                id = accountId,
                name = name.ifEmpty { "M3U Playlist" },
                type = AccountType.M3U,
                m3uUrl = m3uUrl,
                isActive = true,
                channelCount = liveCount,
                movieCount = movieCount,
                seriesCount = seriesCount
            )

            dao.deactivateAllAccounts()
            dao.insertAccount(account)
            dao.deleteMediaItemsByAccount(accountId)
            dao.insertMediaItems(items)
            Result.success(account)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun switchAccount(accountId: String) = withContext(Dispatchers.IO) {
        dao.switchActiveAccount(accountId)
    }

    suspend fun deleteAccount(accountId: String) = withContext(Dispatchers.IO) {
        dao.deleteAccount(accountId)
        dao.deleteMediaItemsByAccount(accountId)
    }

    suspend fun toggleFavorite(item: MediaItem) = withContext(Dispatchers.IO) {
        val isFav = dao.isFavoriteOnce(item.id)
        if (isFav) {
            dao.deleteFavorite(item.id)
        } else {
            dao.insertFavorite(
                FavoriteItem(
                    mediaId = item.id,
                    accountId = item.accountId,
                    title = item.title,
                    logoUrl = item.logoUrl,
                    type = item.type,
                    category = item.category
                )
            )
        }
    }

    fun isFavorite(mediaId: String): Flow<Boolean> = dao.isFavorite(mediaId)

    suspend fun saveWatchProgress(
        streamId: String,
        title: String,
        posterUrl: String,
        mediaType: MediaType,
        positionMs: Long,
        durationMs: Long,
        seasonNumber: Int = 1,
        episodeNumber: Int = 1
    ) = withContext(Dispatchers.IO) {
        if (durationMs > 0 && positionMs > 5000) { // Only save after 5 seconds of watching
            dao.insertWatchHistory(
                WatchHistory(
                    streamId = streamId,
                    title = title,
                    posterUrl = posterUrl,
                    mediaType = mediaType,
                    positionMs = positionMs,
                    durationMs = durationMs,
                    seasonNumber = seasonNumber,
                    episodeNumber = episodeNumber
                )
            )
        }
    }

    suspend fun getWatchHistoryItem(streamId: String): WatchHistory? = withContext(Dispatchers.IO) {
        dao.getWatchHistoryItem(streamId)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        dao.clearWatchHistory()
    }
}
