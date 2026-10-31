package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AccountType {
    XTREAM,
    M3U,
    DEMO
}

@Entity(tableName = "iptv_accounts")
data class IptvAccount(
    @PrimaryKey val id: String,
    val name: String,
    val type: AccountType,
    val serverUrl: String = "",
    val username: String = "",
    val password: String = "",
    val m3uUrl: String = "",
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastSynced: Long = System.currentTimeMillis(),
    val channelCount: Int = 0,
    val movieCount: Int = 0,
    val seriesCount: Int = 0
)

@Entity(tableName = "watch_history")
data class WatchHistory(
    @PrimaryKey val streamId: String,
    val title: String,
    val posterUrl: String,
    val mediaType: MediaType,
    val positionMs: Long,
    val durationMs: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val seasonNumber: Int = 1,
    val episodeNumber: Int = 1
)

@Entity(tableName = "favorites")
data class FavoriteItem(
    @PrimaryKey val mediaId: String,
    val accountId: String,
    val title: String,
    val logoUrl: String,
    val type: MediaType,
    val category: String,
    val timestamp: Long = System.currentTimeMillis()
)
