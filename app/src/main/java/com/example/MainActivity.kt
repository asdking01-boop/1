package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AccountManagerDialog
import com.example.ui.components.IptvSetupDialog
import com.example.ui.components.MediaDetailModal
import com.example.ui.player.PlayerScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LiveTvScreen
import com.example.ui.screens.MoviesScreen
import com.example.ui.screens.SeriesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.NovaBlack
import com.example.ui.theme.NovaCardBg
import com.example.ui.theme.NovaIptvTheme
import com.example.ui.theme.NovaRed
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import com.example.ui.viewmodel.IptvViewModel
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val viewModel: IptvViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val appLanguage by viewModel.appLanguage.collectAsState()
            val layoutDirection = if (appLanguage == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr

            // Dynamic locale context configuration for instant translation update
            val baseContext = LocalContext.current
            val localizedContext = remember(appLanguage) {
                val locale = Locale(appLanguage)
                Locale.setDefault(locale)
                val config = baseContext.resources.configuration
                config.setLocale(locale)
                config.setLayoutDirection(locale)
                baseContext.createConfigurationContext(config)
            }

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalLayoutDirection provides layoutDirection
            ) {
                NovaIptvTheme {
                    MainAppContent(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: IptvViewModel) {
    var selectedNavIndex by rememberSaveable { mutableIntStateOf(0) }

    val currentPlaybackItem by viewModel.currentPlaybackItem.collectAsState()
    val currentEpisode by viewModel.currentEpisode.collectAsState()
    val liveChannels by viewModel.liveChannels.collectAsState()

    val showSetupWizard by viewModel.showSetupWizard.collectAsState()
    val showAccountManager by viewModel.showAccountManager.collectAsState()
    val selectedDetailItem by viewModel.selectedDetailItem.collectAsState()

    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncMessage by viewModel.syncMessage.collectAsState()
    val syncError by viewModel.syncError.collectAsState()
    val activeAccount by viewModel.activeAccount.collectAsState()
    val allAccounts by viewModel.allAccounts.collectAsState()
    val defaultAspect by viewModel.defaultAspectRatio.collectAsState()
    val autoPlayNext by viewModel.autoPlayNextEpisode.collectAsState()
    val allFavorites by viewModel.allFavorites.collectAsState()

    // BackHandler coordination
    BackHandler(enabled = currentPlaybackItem == null && (selectedDetailItem != null || showAccountManager || showSetupWizard || selectedNavIndex != 0)) {
        when {
            selectedDetailItem != null -> viewModel.closeDetail()
            showAccountManager -> viewModel.closeAccountManager()
            showSetupWizard && activeAccount != null -> viewModel.closeSetupWizard()
            selectedNavIndex != 0 -> selectedNavIndex = 0
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(NovaBlack)) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = NovaBlack,
            bottomBar = {
                if (currentPlaybackItem == null) {
                    NavigationBar(
                        containerColor = NovaBlack,
                        contentColor = NovaTextPrimary,
                        modifier = Modifier
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .testTag("bottom_nav_bar")
                    ) {
                        // 0: Home
                        NavigationBarItem(
                            selected = selectedNavIndex == 0,
                            onClick = { selectedNavIndex = 0 },
                            icon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.nav_home), modifier = Modifier.size(22.dp)) },
                            label = { Text(stringResource(R.string.nav_home), fontSize = 10.sp, fontWeight = if (selectedNavIndex == 0) FontWeight.Bold else FontWeight.Normal) },
                            colors = navigationBarItemColors(),
                            modifier = Modifier.testTag("nav_home")
                        )
                        // 1: Live TV
                        NavigationBarItem(
                            selected = selectedNavIndex == 1,
                            onClick = { selectedNavIndex = 1 },
                            icon = { Icon(Icons.Default.LiveTv, contentDescription = stringResource(R.string.nav_live_tv), modifier = Modifier.size(22.dp)) },
                            label = { Text(stringResource(R.string.nav_live_tv), fontSize = 10.sp, fontWeight = if (selectedNavIndex == 1) FontWeight.Bold else FontWeight.Normal) },
                            colors = navigationBarItemColors(),
                            modifier = Modifier.testTag("nav_live_tv")
                        )
                        // 2: Movies
                        NavigationBarItem(
                            selected = selectedNavIndex == 2,
                            onClick = { selectedNavIndex = 2 },
                            icon = { Icon(Icons.Default.Movie, contentDescription = stringResource(R.string.nav_movies), modifier = Modifier.size(22.dp)) },
                            label = { Text(stringResource(R.string.nav_movies), fontSize = 10.sp, fontWeight = if (selectedNavIndex == 2) FontWeight.Bold else FontWeight.Normal) },
                            colors = navigationBarItemColors(),
                            modifier = Modifier.testTag("nav_movies")
                        )
                        // 3: Series
                        NavigationBarItem(
                            selected = selectedNavIndex == 3,
                            onClick = { selectedNavIndex = 3 },
                            icon = { Icon(Icons.Default.VideoLibrary, contentDescription = stringResource(R.string.nav_series), modifier = Modifier.size(22.dp)) },
                            label = { Text(stringResource(R.string.nav_series), fontSize = 10.sp, fontWeight = if (selectedNavIndex == 3) FontWeight.Bold else FontWeight.Normal) },
                            colors = navigationBarItemColors(),
                            modifier = Modifier.testTag("nav_series")
                        )
                        // 4: Favorites
                        NavigationBarItem(
                            selected = selectedNavIndex == 4,
                            onClick = { selectedNavIndex = 4 },
                            icon = { Icon(Icons.Default.Favorite, contentDescription = stringResource(R.string.nav_favorites), modifier = Modifier.size(22.dp)) },
                            label = { Text(stringResource(R.string.nav_favorites), fontSize = 10.sp, fontWeight = if (selectedNavIndex == 4) FontWeight.Bold else FontWeight.Normal) },
                            colors = navigationBarItemColors(),
                            modifier = Modifier.testTag("nav_favorites")
                        )
                        // 5: Settings
                        NavigationBarItem(
                            selected = selectedNavIndex == 5,
                            onClick = { selectedNavIndex = 5 },
                            icon = { Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.nav_settings), modifier = Modifier.size(22.dp)) },
                            label = { Text(stringResource(R.string.nav_settings), fontSize = 10.sp, fontWeight = if (selectedNavIndex == 5) FontWeight.Bold else FontWeight.Normal) },
                            colors = navigationBarItemColors(),
                            modifier = Modifier.testTag("nav_settings")
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (selectedNavIndex) {
                    0 -> HomeScreen(viewModel = viewModel, onNavigateToSection = { selectedNavIndex = it })
                    1 -> LiveTvScreen(viewModel = viewModel)
                    2 -> MoviesScreen(viewModel = viewModel)
                    3 -> SeriesScreen(viewModel = viewModel)
                    4 -> FavoritesScreen(viewModel = viewModel)
                    5 -> SettingsScreen(viewModel = viewModel)
                }
            }
        }

        // Fullscreen Cinema Video Player
        if (currentPlaybackItem != null) {
            PlayerScreen(
                mediaItem = currentPlaybackItem!!,
                episode = currentEpisode,
                liveChannels = liveChannels,
                onClose = { viewModel.closePlayer() },
                onSaveProgress = { pos, dur -> viewModel.saveProgress(pos, dur) },
                onPlayNextEpisode = { viewModel.playNextEpisode() },
                onChannelSelect = { channel -> viewModel.playMedia(channel) },
                autoPlayNext = autoPlayNext,
                initialAspectRatio = defaultAspect
            )
        }

        // Media Detail Bottom Sheet
        if (selectedDetailItem != null) {
            val episodes = if (selectedDetailItem!!.seriesId != null) {
                viewModel.getEpisodesForSeries(selectedDetailItem!!.seriesId!!)
            } else emptyList()

            val isFav = allFavorites.any { it.mediaId == selectedDetailItem!!.id }

            MediaDetailModal(
                item = selectedDetailItem,
                episodes = episodes,
                onDismiss = { viewModel.closeDetail() },
                onPlayClick = {
                    val item = selectedDetailItem!!
                    viewModel.closeDetail()
                    viewModel.playMedia(item)
                },
                onPlayEpisode = { ep ->
                    val item = selectedDetailItem!!
                    viewModel.closeDetail()
                    viewModel.playMedia(item, ep)
                },
                onToggleFavorite = {
                    viewModel.toggleFavorite(selectedDetailItem!!)
                },
                isFavorite = isFav
            )
        }

        // Setup Wizard Dialog
        IptvSetupDialog(
            isOpen = showSetupWizard,
            onDismiss = { viewModel.closeSetupWizard() },
            onConnectXtream = { name, server, user, pass ->
                viewModel.connectXtream(name, server, user, pass)
            },
            onConnectM3u = { name, url ->
                viewModel.connectM3u(name, url)
            },
            onLoadDemo = {
                viewModel.loadDemoShowcase()
            },
            isSyncing = isSyncing,
            syncMessage = syncMessage,
            syncError = syncError,
            canDismiss = activeAccount != null
        )

        // Account Manager Dialog
        AccountManagerDialog(
            isOpen = showAccountManager,
            onDismiss = { viewModel.closeAccountManager() },
            accounts = allAccounts,
            onSwitchAccount = { accountId ->
                viewModel.switchAccount(accountId)
                viewModel.closeAccountManager()
            },
            onDeleteAccount = { accountId ->
                viewModel.deleteAccount(accountId)
            },
            onAddNewAccount = {
                viewModel.closeAccountManager()
                viewModel.openSetupWizard()
            }
        )
    }
}

@Composable
private fun navigationBarItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = Color.White,
    selectedTextColor = NovaRed,
    indicatorColor = NovaRed,
    unselectedIconColor = NovaTextSecondary,
    unselectedTextColor = NovaTextSecondary
)
