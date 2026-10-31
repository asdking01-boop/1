package com.example.data.parser

import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.UUID

object M3uParser {

    private val TVG_NAME_REGEX = Regex("""tvg-name="([^"]*)"""", RegexOption.IGNORE_CASE)
    private val TVG_LOGO_REGEX = Regex("""tvg-logo="([^"]*)"""", RegexOption.IGNORE_CASE)
    private val GROUP_TITLE_REGEX = Regex("""group-title="([^"]*)"""", RegexOption.IGNORE_CASE)
    private val TVG_ID_REGEX = Regex("""tvg-id="([^"]*)"""", RegexOption.IGNORE_CASE)

    fun parse(inputStream: InputStream, accountId: String): List<MediaItem> {
        val items = mutableListOf<MediaItem>()
        val reader = BufferedReader(InputStreamReader(inputStream))
        var line: String?
        var currentTitle = ""
        var currentLogo = ""
        var currentCategory = "General"
        var currentTvgName = ""

        while (reader.readLine().also { line = it } != null) {
            val trimmed = line!!.trim()
            if (trimmed.isEmpty()) continue

            if (trimmed.startsWith("#EXTINF:", ignoreCase = true)) {
                currentTvgName = TVG_NAME_REGEX.find(trimmed)?.groupValues?.get(1).orEmpty()
                currentLogo = TVG_LOGO_REGEX.find(trimmed)?.groupValues?.get(1).orEmpty()
                currentCategory = GROUP_TITLE_REGEX.find(trimmed)?.groupValues?.get(1) ?: "General"

                // Title is usually after the last comma
                val commaIndex = trimmed.lastIndexOf(',')
                currentTitle = if (commaIndex != -1 && commaIndex < trimmed.length - 1) {
                    trimmed.substring(commaIndex + 1).trim()
                } else if (currentTvgName.isNotEmpty()) {
                    currentTvgName
                } else {
                    "Channel ${items.size + 1}"
                }
            } else if (!trimmed.startsWith("#") && (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true))) {
                val streamUrl = trimmed
                val type = detectMediaType(streamUrl, currentCategory, currentTitle)
                val quality = detectQuality(currentTitle, currentCategory)

                items.add(
                    MediaItem(
                        id = "${accountId}_${UUID.randomUUID().toString().take(8)}",
                        accountId = accountId,
                        title = currentTitle.ifEmpty { "Stream ${items.size + 1}" },
                        logoUrl = currentLogo,
                        category = cleanCategory(currentCategory),
                        type = type,
                        streamUrl = streamUrl,
                        quality = quality,
                        isDemo = false
                    )
                )

                // Reset per-stream temp state
                currentTitle = ""
                currentLogo = ""
                currentCategory = "General"
                currentTvgName = ""
            }
        }

        return items
    }

    private fun detectMediaType(url: String, category: String, title: String): MediaType {
        val combined = "$url $category $title".lowercase()
        return when {
            combined.contains("/series/") || combined.contains(" s0") || combined.contains(" s1") || combined.contains(" e0") || combined.contains(" e1") || combined.contains("مسلسل") -> MediaType.SERIES
            combined.contains("/movie/") || combined.contains("vod") || combined.contains(".mp4") || combined.contains(".mkv") || combined.contains("فيلم") || combined.contains("movies") || combined.contains("cinema") -> MediaType.MOVIE
            else -> MediaType.LIVE
        }
    }

    private fun detectQuality(title: String, category: String): String {
        val combined = "$title $category".uppercase()
        return when {
            combined.contains("4K") || combined.contains("UHD") || combined.contains("2160") -> "4K 2160p"
            combined.contains("FHD") || combined.contains("1080") -> "FHD 1080p"
            combined.contains("HD") || combined.contains("720") -> "HD 720p"
            combined.contains("SD") || combined.contains("480") || combined.contains("360") -> "SD 480p"
            else -> "HD"
        }
    }

    private fun cleanCategory(category: String): String {
        return category.replace(Regex("""^[\|\-\s]+|[\|\-\s]+$"""), "").trim().ifEmpty { "General" }
    }
}
