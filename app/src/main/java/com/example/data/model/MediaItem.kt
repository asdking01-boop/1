package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MediaType {
    LIVE,
    MOVIE,
    SERIES
}

enum class StreamQuality(val label: String) {
    AUTO("Auto"),
    SD("SD 480p"),
    HD("HD 720p"),
    FHD("FHD 1080p"),
    QHD("QHD 1440p"),
    UHD_4K("4K 2160p")
}

@Entity(tableName = "media_items")
data class MediaItem(
    @PrimaryKey val id: String,
    val accountId: String,
    val title: String,
    val logoUrl: String = "",
    val category: String = "General",
    val type: MediaType = MediaType.LIVE,
    val streamUrl: String = "",
    val quality: String = "HD",
    val duration: String = "",
    val releaseYear: String = "",
    val rating: String = "",
    val overview: String = "",
    val seriesId: String? = null,
    val seasonNumber: Int = 1,
    val episodeNumber: Int = 1,
    val isFavorite: Boolean = false,
    val isDemo: Boolean = false
)

data class SeriesEpisode(
    val id: String,
    val seriesId: String,
    val title: String,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val streamUrl: String,
    val duration: String = "45m",
    val overview: String = "",
    val thumbnailUrl: String = ""
)
