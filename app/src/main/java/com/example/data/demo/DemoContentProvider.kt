package com.example.data.demo

import com.example.data.model.AccountType
import com.example.data.model.IptvAccount
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.data.model.SeriesEpisode

object DemoContentProvider {

    val DEMO_ACCOUNT = IptvAccount(
        id = "demo_account_default",
        name = "NOVA Showcase (Demo)",
        type = AccountType.DEMO,
        serverUrl = "https://demo.novaiptv.com",
        isActive = true,
        channelCount = 6,
        movieCount = 6,
        seriesCount = 3
    )

    fun getDemoMediaItems(accountId: String = DEMO_ACCOUNT.id): List<MediaItem> {
        return listOf(
            // LIVE CHANNELS
            MediaItem(
                id = "${accountId}_live_1",
                accountId = accountId,
                title = "NOVA Sports 1 HD",
                logoUrl = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=400&auto=format&fit=crop&q=80",
                category = "Sports",
                type = MediaType.LIVE,
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                quality = "FHD 1080p",
                overview = "Live global sports coverage, match previews, analysis, and tournament highlights.",
                isDemo = true
            ),
            MediaItem(
                id = "${accountId}_live_2",
                accountId = accountId,
                title = "NOVA News 24/7",
                logoUrl = "https://images.unsplash.com/photo-1585829365295-ab7cd400c167?w=400&auto=format&fit=crop&q=80",
                category = "News",
                type = MediaType.LIVE,
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                quality = "HD 720p",
                overview = "Breaking international headlines, financial markets, and around-the-clock live reporting.",
                isDemo = true
            ),
            MediaItem(
                id = "${accountId}_live_3",
                accountId = accountId,
                title = "NOVA Cinema 4K",
                logoUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=400&auto=format&fit=crop&q=80",
                category = "Movies",
                type = MediaType.LIVE,
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                quality = "4K 2160p",
                overview = "Non-stop blockbuster releases and award-winning international cinema in ultra-high resolution.",
                isDemo = true
            ),
            MediaItem(
                id = "${accountId}_live_4",
                accountId = accountId,
                title = "NOVA Discovery & Nature",
                logoUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=400&auto=format&fit=crop&q=80",
                category = "Documentary",
                type = MediaType.LIVE,
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                quality = "FHD 1080p",
                overview = "Exploring Earth's greatest wildlife, ocean depths, space exploration, and scientific marvels.",
                isDemo = true
            ),
            MediaItem(
                id = "${accountId}_live_5",
                accountId = accountId,
                title = "NOVA Kids Toons",
                logoUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=400&auto=format&fit=crop&q=80",
                category = "Kids",
                type = MediaType.LIVE,
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                quality = "HD 720p",
                overview = "Safe, colorful cartoon programming, educational adventures, and animated shows for kids.",
                isDemo = true
            ),
            MediaItem(
                id = "${accountId}_live_6",
                accountId = accountId,
                title = "NOVA Entertainment Prime",
                logoUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=400&auto=format&fit=crop&q=80",
                category = "Entertainment",
                type = MediaType.LIVE,
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
                quality = "FHD 1080p",
                overview = "Talk shows, reality competitions, music specials, and premier entertainment series.",
                isDemo = true
            ),

            // MOVIES (VOD)
            MediaItem(
                id = "${accountId}_vod_1",
                accountId = accountId,
                title = "Tears of Steel: Resurgence",
                logoUrl = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=600&auto=format&fit=crop&q=80",
                category = "Action",
                type = MediaType.MOVIE,
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                quality = "4K 2160p",
                duration = "1h 52m",
                releaseYear = "2024",
                rating = "8.9",
                overview = "In a dystopian cyberpunk Amsterdam, a rogue squad of cybernetic warriors must recapture an ancient beacon before rogue machines consume humanity.",
                isDemo = true
            ),
            MediaItem(
                id = "${accountId}_vod_2",
                accountId = accountId,
                title = "Sintel: The Dragon Quest",
                logoUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80",
                category = "Action",
                type = MediaType.MOVIE,
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                quality = "FHD 1080p",
                duration = "1h 38m",
                releaseYear = "2023",
                rating = "8.4",
                overview = "A determined lone wanderer traverses frozen mountain peaks and treacherous deserts to rescue her stolen companion from a ferocious dragon.",
                isDemo = true
            ),
            MediaItem(
                id = "${accountId}_vod_3",
                accountId = accountId,
                title = "Big Buck Bunny",
                logoUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600&auto=format&fit=crop&q=80",
                category = "Comedy",
                type = MediaType.MOVIE,
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                quality = "FHD 1080p",
                duration = "1h 24m",
                releaseYear = "2022",
                rating = "8.1",
                overview = "A gentle giant forest bunny seeks hilarious and creative revenge against three bullying forest rodents who ruined his peaceful day.",
                isDemo = true
            ),
            MediaItem(
                id = "${accountId}_vod_4",
                accountId = accountId,
                title = "Chronicles of Cosmos",
                logoUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=600&auto=format&fit=crop&q=80",
                category = "Documentary",
                type = MediaType.MOVIE,
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                quality = "4K 2160p",
                duration = "2h 05m",
                releaseYear = "2024",
                rating = "9.2",
                overview = "An immersive journey through supernovas, black holes, and the outer edges of the observable universe captured by modern orbital telescopes.",
                isDemo = true
            ),
            MediaItem(
                id = "${accountId}_vod_5",
                accountId = accountId,
                title = "Elephants Dream: Synthetic Reality",
                logoUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=600&auto=format&fit=crop&q=80",
                category = "Drama",
                type = MediaType.MOVIE,
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                quality = "HD 720p",
                duration = "1h 45m",
                releaseYear = "2023",
                rating = "7.8",
                overview = "Two contrasting explorers navigate an endless mechanical labyrinth whose twisted architecture materializes from their deepest subconscious thoughts.",
                isDemo = true
            ),
            MediaItem(
                id = "${accountId}_vod_6",
                accountId = accountId,
                title = "High Speed Horizon",
                logoUrl = "https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7?w=600&auto=format&fit=crop&q=80",
                category = "Sports",
                type = MediaType.MOVIE,
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
                quality = "FHD 1080p",
                duration = "1h 30m",
                releaseYear = "2024",
                rating = "8.6",
                overview = "Adrenaline-fueled cross-country road rally pushing experimental supercars and veteran drivers to their ultimate physical limits.",
                isDemo = true
            ),

            // SERIES
            MediaItem(
                id = "${accountId}_series_1",
                accountId = accountId,
                title = "Cyber Horizon: Neo Amsterdam",
                logoUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=600&auto=format&fit=crop&q=80",
                category = "Action",
                type = MediaType.SERIES,
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                quality = "4K 2160p",
                duration = "10 Episodes",
                releaseYear = "2024",
                rating = "9.1",
                overview = "In 2095, elite cyber-investigators unravel an undercover corporate syndicate engineering rogue artificial intelligences.",
                seriesId = "series_1",
                isDemo = true
            ),
            MediaItem(
                id = "${accountId}_series_2",
                accountId = accountId,
                title = "Kingdom of Dragons",
                logoUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80",
                category = "Drama",
                type = MediaType.SERIES,
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                quality = "FHD 1080p",
                duration = "8 Episodes",
                releaseYear = "2023",
                rating = "8.7",
                overview = "Ancient dynasties vie for dominion over untamed mystic territories protected by re-emerging ancestral beasts.",
                seriesId = "series_2",
                isDemo = true
            ),
            MediaItem(
                id = "${accountId}_series_3",
                accountId = accountId,
                title = "Planet Earth Unveiled",
                logoUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600&auto=format&fit=crop&q=80",
                category = "Documentary",
                type = MediaType.SERIES,
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                quality = "FHD 1080p",
                duration = "6 Episodes",
                releaseYear = "2024",
                rating = "9.5",
                overview = "Groundbreaking footage revealing unseen predator dynamics, deep sea bioluminescence, and high-altitude ecosystems.",
                seriesId = "series_3",
                isDemo = true
            )
        )
    }

    fun getEpisodesForSeries(seriesId: String): List<SeriesEpisode> {
        val streamUrl = when (seriesId) {
            "series_1" -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
            "series_2" -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"
            else -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        }
        return listOf(
            SeriesEpisode(
                id = "${seriesId}_s1_e1",
                seriesId = seriesId,
                title = "Episode 1: The Awakening",
                seasonNumber = 1,
                episodeNumber = 1,
                streamUrl = streamUrl,
                duration = "52m",
                overview = "The inciting event triggers alarms across the territory, forcing hidden factions out into the open.",
                thumbnailUrl = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=400&auto=format&fit=crop&q=80"
            ),
            SeriesEpisode(
                id = "${seriesId}_s1_e2",
                seriesId = seriesId,
                title = "Episode 2: Dark Waters",
                seasonNumber = 1,
                episodeNumber = 2,
                streamUrl = streamUrl,
                duration = "48m",
                overview = "Alliances fracture as secret transmissions reveal an infiltration within the highest command rank.",
                thumbnailUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=400&auto=format&fit=crop&q=80"
            ),
            SeriesEpisode(
                id = "${seriesId}_s1_e3",
                seriesId = seriesId,
                title = "Episode 3: Iron Skies",
                seasonNumber = 1,
                episodeNumber = 3,
                streamUrl = streamUrl,
                duration = "55m",
                overview = "A daring airborne raid over the northern outpost puts all contingencies to the test.",
                thumbnailUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=400&auto=format&fit=crop&q=80"
            ),
            SeriesEpisode(
                id = "${seriesId}_s1_e4",
                seriesId = seriesId,
                title = "Episode 4: The Reckoning",
                seasonNumber = 1,
                episodeNumber = 4,
                streamUrl = streamUrl,
                duration = "58m",
                overview = "Final preparations are set in motion for the ultimate showdown at the citadel.",
                thumbnailUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=400&auto=format&fit=crop&q=80"
            )
        )
    }
}
