package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.demo.DemoContentProvider
import com.example.data.local.IptvDatabase
import com.example.data.model.FavoriteItem
import com.example.data.model.IptvAccount
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.data.model.SeriesEpisode
import com.example.data.model.WatchHistory
import com.example.data.preferences.PreferencesManager
import com.example.data.repository.IptvRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class IptvViewModel(application: Application) : AndroidViewModel(application) {

    private val db = IptvDatabase.getInstance(application)
    private val repository = IptvRepository(db.iptvDao())
    private val prefs = PreferencesManager.getInstance(application)

    val appLanguage: StateFlow<String> = prefs.appLanguage
    val defaultAspectRatio: StateFlow<String> = prefs.defaultAspectRatio
    val autoPlayNextEpisode: StateFlow<Boolean> = prefs.autoPlayNextEpisode

    val allAccounts: StateFlow<List<IptvAccount>> = repository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeAccount: StateFlow<IptvAccount?> = repository.activeAccount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val watchHistory: StateFlow<List<WatchHistory>> = repository.watchHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFavorites: StateFlow<List<FavoriteItem>> = repository.allFavorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All channels for active account
    val liveChannels: StateFlow<List<MediaItem>> = activeAccount.flatMapLatest { account ->
        account?.let { repository.getMediaItems(it.id, MediaType.LIVE) } ?: flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All movies for active account
    val movies: StateFlow<List<MediaItem>> = activeAccount.flatMapLatest { account ->
        account?.let { repository.getMediaItems(it.id, MediaType.MOVIE) } ?: flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All series for active account
    val seriesList: StateFlow<List<MediaItem>> = activeAccount.flatMapLatest { account ->
        account?.let { repository.getMediaItems(it.id, MediaType.SERIES) } ?: flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Dialog & Navigation States
    private val _showSetupWizard = MutableStateFlow(false)
    val showSetupWizard: StateFlow<Boolean> = _showSetupWizard.asStateFlow()

    private val _showAccountManager = MutableStateFlow(false)
    val showAccountManager: StateFlow<Boolean> = _showAccountManager.asStateFlow()

    private val _selectedDetailItem = MutableStateFlow<MediaItem?>(null)
    val selectedDetailItem: StateFlow<MediaItem?> = _selectedDetailItem.asStateFlow()

    // Active Playback State
    private val _currentPlaybackItem = MutableStateFlow<MediaItem?>(null)
    val currentPlaybackItem: StateFlow<MediaItem?> = _currentPlaybackItem.asStateFlow()

    private val _currentEpisode = MutableStateFlow<SeriesEpisode?>(null)
    val currentEpisode: StateFlow<SeriesEpisode?> = _currentEpisode.asStateFlow()

    // Syncing status
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError.asStateFlow()

    // Search and filters
    private val _channelSearchQuery = MutableStateFlow("")
    val channelSearchQuery: StateFlow<String> = _channelSearchQuery.asStateFlow()

    private val _channelCategoryFilter = MutableStateFlow("All")
    val channelCategoryFilter: StateFlow<String> = _channelCategoryFilter.asStateFlow()

    private val _movieSearchQuery = MutableStateFlow("")
    val movieSearchQuery: StateFlow<String> = _movieSearchQuery.asStateFlow()

    private val _movieCategoryFilter = MutableStateFlow("All")
    val movieCategoryFilter: StateFlow<String> = _movieCategoryFilter.asStateFlow()

    private val _seriesSearchQuery = MutableStateFlow("")
    val seriesSearchQuery: StateFlow<String> = _seriesSearchQuery.asStateFlow()

    private val _seriesCategoryFilter = MutableStateFlow("All")
    val seriesCategoryFilter: StateFlow<String> = _seriesCategoryFilter.asStateFlow()

    init {
        // Automatically check if an account exists; if none exists, show Setup Wizard
        viewModelScope.launch {
            repository.activeAccount.collect { account ->
                if (account == null) {
                    _showSetupWizard.value = true
                }
            }
        }
    }

    // Filtered lists
    val filteredLiveChannels: StateFlow<List<MediaItem>> = combine(
        liveChannels, channelSearchQuery, channelCategoryFilter
    ) { channels, query, category ->
        channels.filter { item ->
            val matchesQuery = query.isEmpty() || item.title.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" || category == "الكل" || item.category.equals(category, ignoreCase = true)
            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredMovies: StateFlow<List<MediaItem>> = combine(
        movies, movieSearchQuery, movieCategoryFilter
    ) { movieList, query, category ->
        movieList.filter { item ->
            val matchesQuery = query.isEmpty() || item.title.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" || category == "الكل" || item.category.equals(category, ignoreCase = true)
            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredSeries: StateFlow<List<MediaItem>> = combine(
        seriesList, seriesSearchQuery, seriesCategoryFilter
    ) { series, query, category ->
        series.filter { item ->
            val matchesQuery = query.isEmpty() || item.title.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" || category == "الكل" || item.category.equals(category, ignoreCase = true)
            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Category lists
    val channelCategories: StateFlow<List<String>> = liveChannels.flatMapLatest { channels ->
        val cats = listOf("All") + channels.map { it.category }.distinct().sorted()
        flowOf(cats)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("All"))

    val movieCategories: StateFlow<List<String>> = movies.flatMapLatest { movieList ->
        val cats = listOf("All") + movieList.map { it.category }.distinct().sorted()
        flowOf(cats)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("All"))

    val seriesCategories: StateFlow<List<String>> = seriesList.flatMapLatest { list ->
        val cats = listOf("All") + list.map { it.category }.distinct().sorted()
        flowOf(cats)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("All"))

    // Actions
    fun setChannelSearchQuery(q: String) { _channelSearchQuery.value = q }
    fun setChannelCategoryFilter(c: String) { _channelCategoryFilter.value = c }
    fun setMovieSearchQuery(q: String) { _movieSearchQuery.value = q }
    fun setMovieCategoryFilter(c: String) { _movieCategoryFilter.value = c }
    fun setSeriesSearchQuery(q: String) { _seriesSearchQuery.value = q }
    fun setSeriesCategoryFilter(c: String) { _seriesCategoryFilter.value = c }

    fun openSetupWizard() { _showSetupWizard.value = true }
    fun closeSetupWizard() { _showSetupWizard.value = false }
    fun openAccountManager() { _showAccountManager.value = true }
    fun closeAccountManager() { _showAccountManager.value = false }

    fun showDetail(item: MediaItem) { _selectedDetailItem.value = item }
    fun closeDetail() { _selectedDetailItem.value = null }

    fun playMedia(item: MediaItem, episode: SeriesEpisode? = null) {
        _currentPlaybackItem.value = item
        _currentEpisode.value = episode
    }

    fun closePlayer() {
        _currentPlaybackItem.value = null
        _currentEpisode.value = null
    }

    fun saveProgress(positionMs: Long, durationMs: Long) {
        val item = _currentPlaybackItem.value ?: return
        viewModelScope.launch {
            val ep = _currentEpisode.value
            repository.saveWatchProgress(
                streamId = ep?.id ?: item.id,
                title = ep?.title ?: item.title,
                posterUrl = ep?.thumbnailUrl?.ifEmpty { item.logoUrl } ?: item.logoUrl,
                mediaType = item.type,
                positionMs = positionMs,
                durationMs = durationMs,
                seasonNumber = ep?.seasonNumber ?: 1,
                episodeNumber = ep?.episodeNumber ?: 1
            )
        }
    }

    fun toggleFavorite(item: MediaItem) {
        viewModelScope.launch {
            repository.toggleFavorite(item)
        }
    }

    fun switchAccount(accountId: String) {
        viewModelScope.launch {
            repository.switchAccount(accountId)
        }
    }

    fun deleteAccount(accountId: String) {
        viewModelScope.launch {
            repository.deleteAccount(accountId)
        }
    }

    fun loadDemoShowcase() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncMessage.value = "Loading NOVA Showcase..."
            _syncError.value = null

            val result = repository.loadDemoShowcase()
            result.fold(
                onSuccess = {
                    _isSyncing.value = false
                    _syncMessage.value = "Showcase loaded successfully!"
                    _showSetupWizard.value = false
                },
                onFailure = { err ->
                    _isSyncing.value = false
                    _syncError.value = err.localizedMessage
                }
            )
        }
    }

    fun connectXtream(name: String, server: String, user: String, pass: String) {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncMessage.value = "Authenticating with Xtream Codes..."
            _syncError.value = null

            val result = repository.addXtreamAccount(name, server, user, pass)
            result.fold(
                onSuccess = {
                    _isSyncing.value = false
                    _syncMessage.value = "Synchronized ${it.channelCount} channels, ${it.movieCount} movies"
                    _showSetupWizard.value = false
                },
                onFailure = { err ->
                    _isSyncing.value = false
                    _syncError.value = err.localizedMessage ?: "Failed to connect to Xtream Codes server"
                }
            )
        }
    }

    fun connectM3u(name: String, url: String) {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncMessage.value = "Downloading and parsing M3U playlist..."
            _syncError.value = null

            val result = repository.addM3uAccount(name, url)
            result.fold(
                onSuccess = {
                    _isSyncing.value = false
                    _syncMessage.value = "Synchronized ${it.channelCount} channels, ${it.movieCount} movies"
                    _showSetupWizard.value = false
                },
                onFailure = { err ->
                    _isSyncing.value = false
                    _syncError.value = err.localizedMessage ?: "Failed to load M3U playlist"
                }
            )
        }
    }

    fun setLanguage(lang: String) {
        prefs.setAppLanguage(lang)
    }

    fun setDefaultAspectRatio(ratio: String) {
        prefs.setDefaultAspectRatio(ratio)
    }

    fun setAutoPlayNextEpisode(enabled: Boolean) {
        prefs.setAutoPlayNextEpisode(enabled)
    }

    fun getEpisodesForSeries(seriesId: String): List<SeriesEpisode> {
        return DemoContentProvider.getEpisodesForSeries(seriesId)
    }

    fun playNextEpisode() {
        val currentEp = _currentEpisode.value ?: return
        val currentMedia = _currentPlaybackItem.value ?: return
        val seriesId = currentMedia.seriesId ?: return
        val episodes = getEpisodesForSeries(seriesId)
        val currentIndex = episodes.indexOfFirst { it.id == currentEp.id }
        if (currentIndex != -1 && currentIndex + 1 < episodes.size) {
            val nextEp = episodes[currentIndex + 1]
            playMedia(currentMedia, nextEp)
        }
    }
}
