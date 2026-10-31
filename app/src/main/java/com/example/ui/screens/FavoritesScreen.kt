package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.MediaType
import com.example.ui.theme.NovaBlack
import com.example.ui.theme.NovaCardBg
import com.example.ui.theme.NovaRed
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import com.example.ui.viewmodel.IptvViewModel

@Composable
fun FavoritesScreen(
    viewModel: IptvViewModel,
    modifier: Modifier = Modifier
) {
    val favorites by viewModel.allFavorites.collectAsState()
    val liveChannels by viewModel.liveChannels.collectAsState()
    val movies by viewModel.movies.collectAsState()
    val seriesList by viewModel.seriesList.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }

    val filteredFavorites = favorites.filter { item ->
        when (selectedTab) {
            1 -> item.type == MediaType.LIVE
            2 -> item.type == MediaType.MOVIE
            3 -> item.type == MediaType.SERIES
            else -> true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NovaBlack)
            .testTag("favorites_screen")
    ) {
        Text(
            text = stringResource(R.string.favorites_title),
            color = NovaTextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        )

        // Filter Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = NovaCardBg,
            contentColor = NovaRed,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = NovaRed
                )
            },
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(10.dp))
        ) {
            listOf(
                stringResource(R.string.filter_all),
                stringResource(R.string.filter_channels),
                stringResource(R.string.filter_movies),
                stringResource(R.string.filter_series)
            ).forEachIndexed { idx, label ->
                Tab(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    text = {
                        Text(
                            text = label,
                            color = if (selectedTab == idx) NovaRed else NovaTextSecondary,
                            fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredFavorites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = NovaTextSecondary.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.no_favorites_title),
                        color = NovaTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.no_favorites_desc),
                        color = NovaTextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 24.dp),
                        lineHeight = 18.sp
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredFavorites) { item ->
                    val fullMedia = liveChannels.find { it.id == item.mediaId }
                        ?: movies.find { it.id == item.mediaId }
                        ?: seriesList.find { it.id == item.mediaId }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(NovaCardBg)
                            .clickable {
                                if (fullMedia != null) {
                                    if (fullMedia.type == MediaType.LIVE) {
                                        viewModel.playMedia(fullMedia)
                                    } else {
                                        viewModel.showDetail(fullMedia)
                                    }
                                }
                            }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 70.dp, height = 50.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF14141A))
                        ) {
                            AsyncImage(
                                model = item.logoUrl.ifEmpty { "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=400&auto=format&fit=crop&q=80" },
                                contentDescription = item.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                color = NovaTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${item.type.name} • ${item.category}",
                                color = NovaTextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        // Play Button
                        if (fullMedia != null) {
                            IconButton(
                                onClick = {
                                    if (fullMedia.type == MediaType.LIVE) {
                                        viewModel.playMedia(fullMedia)
                                    } else {
                                        viewModel.showDetail(fullMedia)
                                    }
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(NovaRed)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Remove from favorites
                        if (fullMedia != null) {
                            IconButton(
                                onClick = { viewModel.toggleFavorite(fullMedia) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Remove Favorite",
                                    tint = NovaRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
