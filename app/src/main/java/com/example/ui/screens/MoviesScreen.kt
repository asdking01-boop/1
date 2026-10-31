package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import com.example.ui.components.CategoryChips
import com.example.ui.components.MediaCard
import com.example.ui.theme.NovaBlack
import com.example.ui.theme.NovaCardBg
import com.example.ui.theme.NovaCardBorder
import com.example.ui.theme.NovaRed
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import com.example.ui.viewmodel.IptvViewModel

@Composable
fun MoviesScreen(
    viewModel: IptvViewModel,
    modifier: Modifier = Modifier
) {
    val movies by viewModel.filteredMovies.collectAsState()
    val categories by viewModel.movieCategories.collectAsState()
    val selectedCategory by viewModel.movieCategoryFilter.collectAsState()
    val searchQuery by viewModel.movieSearchQuery.collectAsState()
    val favorites by viewModel.allFavorites.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NovaBlack)
            .testTag("movies_screen")
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setMovieSearchQuery(it) },
            placeholder = { Text(stringResource(R.string.search_movies), color = NovaTextSecondary, fontSize = 13.sp) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = NovaTextSecondary)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setMovieSearchQuery("") }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = NovaTextSecondary)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NovaRed,
                unfocusedBorderColor = NovaCardBorder,
                focusedContainerColor = NovaCardBg,
                unfocusedContainerColor = NovaCardBg,
                focusedTextColor = NovaTextPrimary,
                unfocusedTextColor = NovaTextPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("movie_search_input")
        )

        // Category filter chips
        CategoryChips(
            categories = categories,
            selectedCategory = selectedCategory,
            onCategorySelected = { viewModel.setMovieCategoryFilter(it) }
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Movies Grid (3 columns)
        if (movies.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_movies_found),
                    color = NovaTextSecondary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp, top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(movies) { movie ->
                    val isFav = favorites.any { it.mediaId == movie.id }
                    MediaCard(
                        item = movie,
                        onClick = { viewModel.showDetail(movie) },
                        onFavoriteToggle = { viewModel.toggleFavorite(movie) },
                        isFavorite = isFav
                    )
                }
            }
        }
    }
}
