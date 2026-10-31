package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.MediaItem
import com.example.data.model.WatchHistory
import com.example.ui.components.CategoryChips
import com.example.ui.components.ContinueWatchingRail
import com.example.ui.components.HeroBanner
import com.example.ui.components.MediaCard
import com.example.ui.theme.NovaBlack
import com.example.ui.theme.NovaRed
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.viewmodel.IptvViewModel

@Composable
fun HomeScreen(
    viewModel: IptvViewModel,
    onNavigateToSection: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val movies by viewModel.movies.collectAsState()
    val seriesList by viewModel.seriesList.collectAsState()
    val liveChannels by viewModel.liveChannels.collectAsState()
    val watchHistory by viewModel.watchHistory.collectAsState()
    val favorites by viewModel.allFavorites.collectAsState()

    val movieCategories by viewModel.movieCategories.collectAsState()
    val selectedCategory by viewModel.movieCategoryFilter.collectAsState()

    // Featured hero item
    val heroItem = movies.firstOrNull() ?: liveChannels.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NovaBlack)
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Hero Banner
        if (heroItem != null) {
            item {
                HeroBanner(
                    item = heroItem,
                    onPlayClick = { viewModel.playMedia(heroItem) },
                    onDetailsClick = { viewModel.showDetail(heroItem) }
                )
            }
        }

        // Category Filter Chips
        item {
            Spacer(modifier = Modifier.height(12.dp))
            CategoryChips(
                categories = movieCategories,
                selectedCategory = selectedCategory,
                onCategorySelected = { viewModel.setMovieCategoryFilter(it) }
            )
        }

        // Continue Watching Section
        if (watchHistory.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                ContinueWatchingRail(
                    historyList = watchHistory,
                    onResumeClick = { history ->
                        val targetItem = movies.find { it.id == history.streamId }
                            ?: seriesList.find { it.id == history.streamId }
                            ?: liveChannels.find { it.id == history.streamId }

                        if (targetItem != null) {
                            viewModel.playMedia(targetItem)
                        }
                    }
                )
            }
        }

        // Trending Movies Section
        if (movies.isNotEmpty()) {
            item {
                SectionHeader(
                    title = stringResource(R.string.trending_movies),
                    onViewAllClick = { onNavigateToSection(2) } // Movies tab
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(movies.take(10)) { movie ->
                        val isFav = favorites.any { it.mediaId == movie.id }
                        MediaCard(
                            item = movie,
                            onClick = { viewModel.showDetail(movie) },
                            onFavoriteToggle = { viewModel.toggleFavorite(movie) },
                            isFavorite = isFav,
                            modifier = Modifier.width(135.dp)
                        )
                    }
                }
            }
        }

        // Live Channels Section
        if (liveChannels.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(
                    title = stringResource(R.string.recommended_channels),
                    onViewAllClick = { onNavigateToSection(1) } // Live TV tab
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(liveChannels.take(10)) { channel ->
                        val isFav = favorites.any { it.mediaId == channel.id }
                        MediaCard(
                            item = channel,
                            onClick = { viewModel.playMedia(channel) },
                            onFavoriteToggle = { viewModel.toggleFavorite(channel) },
                            isFavorite = isFav,
                            modifier = Modifier.width(170.dp)
                        )
                    }
                }
            }
        }

        // Popular Series Section
        if (seriesList.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(
                    title = stringResource(R.string.popular_series),
                    onViewAllClick = { onNavigateToSection(3) } // Series tab
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(seriesList.take(10)) { series ->
                        val isFav = favorites.any { it.mediaId == series.id }
                        MediaCard(
                            item = series,
                            onClick = { viewModel.showDetail(series) },
                            onFavoriteToggle = { viewModel.toggleFavorite(series) },
                            isFavorite = isFav,
                            modifier = Modifier.width(135.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    onViewAllClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            color = NovaTextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        TextButton(onClick = onViewAllClick) {
            Text(
                text = stringResource(R.string.view_all),
                color = NovaRed,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
