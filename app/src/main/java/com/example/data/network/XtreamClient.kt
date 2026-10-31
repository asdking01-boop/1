package com.example.data.network

import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class XtreamClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()
) {

    suspend fun authenticateAndSync(
        serverUrl: String,
        username: String,
        password: String,
        accountId: String
    ): Result<List<MediaItem>> = withContext(Dispatchers.IO) {
        try {
            val base = serverUrl.trimEnd('/')
            val authUrl = "$base/player_api.php?username=$username&password=$password"

            val authReq = Request.Builder().url(authUrl).build()
            val authResp = client.newCall(authReq).execute()
            if (!authResp.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP error ${authResp.code}"))
            }

            val authBody = authResp.body?.string().orEmpty()
            val authJson = JSONObject(authBody)
            val userInfo = authJson.optJSONObject("user_info")
            val authStatus = userInfo?.optInt("auth", 0) ?: 0
            if (authStatus != 1) {
                val statusText = userInfo?.optString("status", "Unauthorized") ?: "Invalid credentials"
                return@withContext Result.failure(Exception("Authentication failed: $statusText"))
            }

            // Sync Live, VOD, and Series
            val mediaItems = mutableListOf<MediaItem>()

            // 1. Fetch live categories
            val liveCatMap = fetchCategories("$base/player_api.php?username=$username&password=$password&action=get_live_categories")
            // 2. Fetch live streams
            val liveReq = Request.Builder().url("$base/player_api.php?username=$username&password=$password&action=get_live_streams").build()
            val liveResp = client.newCall(liveReq).execute()
            if (liveResp.isSuccessful) {
                val liveBody = liveResp.body?.string().orEmpty()
                val liveArray = JSONArray(liveBody)
                for (i in 0 until minOf(liveArray.length(), 250)) {
                    val obj = liveArray.getJSONObject(i)
                    val streamId = obj.optString("stream_id")
                    val name = obj.optString("name", "Channel $streamId")
                    val icon = obj.optString("stream_icon")
                    val catId = obj.optString("category_id")
                    val categoryName = liveCatMap[catId] ?: "General"
                    val streamUrl = "$base/live/$username/$password/$streamId.m3u8"

                    mediaItems.add(
                        MediaItem(
                            id = "${accountId}_live_$streamId",
                            accountId = accountId,
                            title = name,
                            logoUrl = icon,
                            category = categoryName,
                            type = MediaType.LIVE,
                            streamUrl = streamUrl,
                            quality = if (name.contains("4K", true)) "4K 2160p" else if (name.contains("FHD", true) || name.contains("1080", true)) "FHD 1080p" else "HD 720p",
                            isDemo = false
                        )
                    )
                }
            }

            // 3. Fetch VOD categories & streams
            val vodCatMap = fetchCategories("$base/player_api.php?username=$username&password=$password&action=get_vod_categories")
            val vodReq = Request.Builder().url("$base/player_api.php?username=$username&password=$password&action=get_vod_streams").build()
            val vodResp = client.newCall(vodReq).execute()
            if (vodResp.isSuccessful) {
                val vodBody = vodResp.body?.string().orEmpty()
                val vodArray = JSONArray(vodBody)
                for (i in 0 until minOf(vodArray.length(), 200)) {
                    val obj = vodArray.getJSONObject(i)
                    val streamId = obj.optString("stream_id")
                    val name = obj.optString("name", "Movie $streamId")
                    val icon = obj.optString("stream_icon")
                    val ext = obj.optString("container_extension", "mp4")
                    val catId = obj.optString("category_id")
                    val categoryName = vodCatMap[catId] ?: "Movies"
                    val rating = obj.optString("rating", "7.5")
                    val streamUrl = "$base/movie/$username/$password/$streamId.$ext"

                    mediaItems.add(
                        MediaItem(
                            id = "${accountId}_vod_$streamId",
                            accountId = accountId,
                            title = name,
                            logoUrl = icon,
                            category = categoryName,
                            type = MediaType.MOVIE,
                            streamUrl = streamUrl,
                            quality = if (name.contains("4K", true)) "4K 2160p" else "FHD 1080p",
                            rating = rating,
                            isDemo = false
                        )
                    )
                }
            }

            // 4. Fetch Series
            val seriesCatMap = fetchCategories("$base/player_api.php?username=$username&password=$password&action=get_series_categories")
            val seriesReq = Request.Builder().url("$base/player_api.php?username=$username&password=$password&action=get_series").build()
            val seriesResp = client.newCall(seriesReq).execute()
            if (seriesResp.isSuccessful) {
                val seriesBody = seriesResp.body?.string().orEmpty()
                val seriesArray = JSONArray(seriesBody)
                for (i in 0 until minOf(seriesArray.length(), 150)) {
                    val obj = seriesArray.getJSONObject(i)
                    val seriesId = obj.optString("series_id")
                    val name = obj.optString("name", "Series $seriesId")
                    val cover = obj.optString("cover")
                    val catId = obj.optString("category_id")
                    val categoryName = seriesCatMap[catId] ?: "Series"
                    val rating = obj.optString("rating", "8.0")
                    val plot = obj.optString("plot", "")
                    val releaseDate = obj.optString("releaseDate", "")

                    mediaItems.add(
                        MediaItem(
                            id = "${accountId}_series_$seriesId",
                            accountId = accountId,
                            title = name,
                            logoUrl = cover,
                            category = categoryName,
                            type = MediaType.SERIES,
                            streamUrl = "", // loaded per episode
                            quality = "FHD 1080p",
                            releaseYear = releaseDate.take(4),
                            rating = rating,
                            overview = plot,
                            seriesId = seriesId,
                            isDemo = false
                        )
                    )
                }
            }

            Result.success(mediaItems)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun fetchCategories(url: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        try {
            val req = Request.Builder().url(url).build()
            val resp = client.newCall(req).execute()
            if (resp.isSuccessful) {
                val array = JSONArray(resp.body?.string().orEmpty())
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val id = obj.optString("category_id")
                    val name = obj.optString("category_name")
                    if (id.isNotEmpty() && name.isNotEmpty()) {
                        map[id] = name
                    }
                }
            }
        } catch (_: Exception) { }
        return map
    }
}
