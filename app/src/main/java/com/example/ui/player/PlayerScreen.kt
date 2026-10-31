package com.example.ui.player

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build
import android.util.Rational
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.R
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.data.model.SeriesEpisode
import com.example.ui.theme.NovaBlack
import com.example.ui.theme.NovaCardBg
import com.example.ui.theme.NovaGreenLive
import com.example.ui.theme.NovaRed
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class VideoQualityOption(
    val label: String,
    val width: Int,
    val height: Int,
    val bitrate: Int,
    val trackGroupIndex: Int,
    val trackIndex: Int
)

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    mediaItem: MediaItem,
    episode: SeriesEpisode? = null,
    liveChannels: List<MediaItem> = emptyList(),
    onClose: () -> Unit,
    onSaveProgress: (positionMs: Long, durationMs: Long) -> Unit,
    onPlayNextEpisode: () -> Unit,
    onChannelSelect: (MediaItem) -> Unit,
    autoPlayNext: Boolean = true,
    initialAspectRatio: String = "FIT"
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()

    var isPlaying by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    var playbackError by remember { mutableStateOf<String?>(null) }
    var showControls by remember { mutableStateOf(true) }
    var isControlsLocked by remember { mutableStateOf(false) }
    var showChannelGuide by remember { mutableStateOf(false) }

    // Stream telemetry
    var detectedResolution by remember { mutableStateOf(mediaItem.quality) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var isLiveStream by remember { mutableStateOf(mediaItem.type == MediaType.LIVE) }

    // Menus
    var showQualityMenu by remember { mutableStateOf(false) }
    var showAudioMenu by remember { mutableStateOf(false) }
    var showSubtitleMenu by remember { mutableStateOf(false) }
    var showSpeedMenu by remember { mutableStateOf(false) }
    var currentSpeed by remember { mutableFloatStateOf(1.0f) }

    val availableQualities = remember { mutableStateListOf<VideoQualityOption>() }
    var selectedQualityLabel by remember { mutableStateOf("Auto") }

    val audioTracks = remember { mutableStateListOf<String>() }
    var selectedAudioTrack by remember { mutableIntStateOf(0) }

    val subtitleTracks = remember { mutableStateListOf<String>() }
    var selectedSubtitleTrack by remember { mutableIntStateOf(-1) } // -1 = off

    // Aspect ratios: 0=FIT, 1=FILL, 2=ZOOM, 3=FIXED_WIDTH, 4=FIXED_HEIGHT
    var resizeMode by remember {
        mutableIntStateOf(
            when (initialAspectRatio) {
                "FILL" -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                "ZOOM" -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                "STRETCH" -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
            }
        )
    }

    var resizeModeName by remember {
        mutableStateOf(
            when (initialAspectRatio) {
                "FILL" -> "Fill"
                "ZOOM" -> "Zoom"
                "STRETCH" -> "Stretch"
                else -> "Fit"
            }
        )
    }

    // Initialize ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    // Handle back button
    BackHandler {
        onSaveProgress(exoPlayer.currentPosition, exoPlayer.duration)
        onClose()
    }

    // Stream URL
    val streamUrl = episode?.streamUrl?.ifEmpty { mediaItem.streamUrl } ?: mediaItem.streamUrl

    LaunchedEffect(streamUrl) {
        if (streamUrl.isNotEmpty()) {
            isBuffering = true
            playbackError = null
            val exoItem = ExoMediaItem.fromUri(streamUrl)
            exoPlayer.setMediaItem(exoItem)
            exoPlayer.prepare()
            exoPlayer.play()
        }
    }

    // Auto-hide controls timer
    LaunchedEffect(showControls) {
        if (showControls && !isControlsLocked) {
            delay(5000)
            showControls = false
        }
    }

    // Progress poller
    LaunchedEffect(exoPlayer) {
        while (true) {
            if (exoPlayer.isPlaying) {
                currentPositionMs = exoPlayer.currentPosition
                durationMs = exoPlayer.duration.coerceAtLeast(0L)
            }
            delay(1000)
        }
    }

    // Player listener
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                isBuffering = state == Player.STATE_BUFFERING
                if (state == Player.STATE_READY) {
                    playbackError = null
                    durationMs = exoPlayer.duration.coerceAtLeast(0L)
                    isLiveStream = exoPlayer.isCurrentMediaItemLive || mediaItem.type == MediaType.LIVE
                } else if (state == Player.STATE_ENDED) {
                    if (autoPlayNext && episode != null) {
                        onPlayNextEpisode()
                    }
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlayerError(error: PlaybackException) {
                isBuffering = false
                playbackError = error.message ?: "Streaming error occurred"
            }

            override fun onTracksChanged(tracks: Tracks) {
                // Discover actual video tracks
                availableQualities.clear()
                var groupIdx = 0
                for (group in tracks.groups) {
                    if (group.type == C.TRACK_TYPE_VIDEO) {
                        for (i in 0 until group.length) {
                            val format = group.getTrackFormat(i)
                            val w = format.width
                            val h = format.height
                            if (w > 0 && h > 0) {
                                val label = when {
                                    h >= 2160 -> "4K 2160p"
                                    h >= 1440 -> "QHD 1440p"
                                    h >= 1080 -> "1080p FHD"
                                    h >= 720 -> "720p HD"
                                    h >= 480 -> "480p SD"
                                    else -> "${h}p"
                                }
                                availableQualities.add(
                                    VideoQualityOption(label, w, h, format.bitrate, groupIdx, i)
                                )
                            }
                        }
                    } else if (group.type == C.TRACK_TYPE_AUDIO) {
                        audioTracks.clear()
                        for (i in 0 until group.length) {
                            val format = group.getTrackFormat(i)
                            audioTracks.add(format.language ?: "Audio ${i + 1}")
                        }
                    } else if (group.type == C.TRACK_TYPE_TEXT) {
                        subtitleTracks.clear()
                        for (i in 0 until group.length) {
                            val format = group.getTrackFormat(i)
                            subtitleTracks.add(format.language ?: "Subtitle ${i + 1}")
                        }
                    }
                    groupIdx++
                }

                // Detect current real resolution
                val format = exoPlayer.videoFormat
                if (format != null && format.width > 0 && format.height > 0) {
                    detectedResolution = when {
                        format.height >= 2160 -> "4K 2160p"
                        format.height >= 1440 -> "QHD 1440p"
                        format.height >= 1080 -> "1080p FHD"
                        format.height >= 720 -> "720p HD"
                        else -> "${format.height}p"
                    }
                }
            }
        }

        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (!isControlsLocked) {
                    showControls = !showControls
                } else {
                    showControls = true
                }
            }
    ) {
        // AndroidView PlayerView
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    this.resizeMode = resizeMode
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { playerView ->
                playerView.resizeMode = resizeMode
            },
            modifier = Modifier.fillMaxSize()
        )

        // Buffering Spinner
        if (isBuffering && playbackError == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = NovaRed, strokeWidth = 3.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.buffering),
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Playback Error Banner
        if (playbackError != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = stringResource(R.string.playback_error),
                        color = NovaRed,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = playbackError ?: "",
                        color = NovaTextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    IconButton(
                        onClick = {
                            playbackError = null
                            isBuffering = true
                            exoPlayer.prepare()
                            exoPlayer.play()
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(NovaRed)
                            .size(44.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Retry", tint = Color.White)
                    }
                }
            }
        }

        // OSD Controls Overlay
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
            ) {
                // TOP BAR
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = {
                                onSaveProgress(exoPlayer.currentPosition, exoPlayer.duration)
                                onClose()
                            },
                            modifier = Modifier.testTag("player_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            Text(
                                text = episode?.title ?: mediaItem.title,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isLiveStream) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(NovaGreenLive)
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(text = "LIVE", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = detectedResolution,
                                    color = NovaRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Top Action Icons
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Lock Controls Toggle
                        IconButton(onClick = { isControlsLocked = !isControlsLocked }) {
                            Icon(
                                imageVector = if (isControlsLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = "Lock",
                                tint = if (isControlsLocked) NovaRed else Color.White
                            )
                        }

                        if (!isControlsLocked) {
                            // Picture-in-Picture
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                IconButton(
                                    onClick = {
                                        val params = PictureInPictureParams.Builder()
                                            .setAspectRatio(Rational(16, 9))
                                            .build()
                                        activity?.enterPictureInPictureMode(params)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PictureInPictureAlt,
                                        contentDescription = "PiP",
                                        tint = Color.White
                                    )
                                }
                            }

                            // Live TV Guide Drawer Toggle
                            if (isLiveStream && liveChannels.isNotEmpty()) {
                                IconButton(
                                    onClick = { showChannelGuide = !showChannelGuide },
                                    modifier = Modifier.testTag("player_channel_guide_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tv,
                                        contentDescription = "Channel Guide",
                                        tint = Color.White
                                    )
                                }
                            }

                            // Aspect Ratio Cycle Button
                            IconButton(
                                onClick = {
                                    when (resizeMode) {
                                        AspectRatioFrameLayout.RESIZE_MODE_FIT -> {
                                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                                            resizeModeName = "Fill"
                                        }
                                        AspectRatioFrameLayout.RESIZE_MODE_FILL -> {
                                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                            resizeModeName = "Zoom"
                                        }
                                        else -> {
                                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                                            resizeModeName = "Fit"
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AspectRatio,
                                    contentDescription = "Aspect Ratio: $resizeModeName",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }

                // CENTER PLAY/PAUSE/SEEK CONTROLS
                if (!isControlsLocked) {
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(28.dp)
                    ) {
                        if (!isLiveStream) {
                            IconButton(
                                onClick = {
                                    exoPlayer.seekTo((exoPlayer.currentPosition - 10000).coerceAtLeast(0L))
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Replay10,
                                    contentDescription = "Replay 10s",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        // Play / Pause main button
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(NovaRed)
                                .clickable {
                                    if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        if (!isLiveStream) {
                            IconButton(
                                onClick = {
                                    exoPlayer.seekTo((exoPlayer.currentPosition + 10000).coerceAtMost(durationMs))
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Forward10,
                                    contentDescription = "Forward 10s",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        // Next Episode Button for TV series
                        if (episode != null) {
                            IconButton(
                                onClick = onPlayNextEpisode,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = stringResource(R.string.next_episode),
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }

                // BOTTOM BAR (Scrub bar & Track settings)
                if (!isControlsLocked) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 20.dp)
                    ) {
                        // Scrubber for VOD/Movies
                        if (!isLiveStream && durationMs > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formatDuration(currentPositionMs),
                                    color = Color.White,
                                    fontSize = 12.sp
                                )

                                Slider(
                                    value = currentPositionMs.toFloat(),
                                    onValueChange = { targetPos ->
                                        currentPositionMs = targetPos.toLong()
                                        exoPlayer.seekTo(targetPos.toLong())
                                    },
                                    valueRange = 0f..durationMs.toFloat(),
                                    colors = SliderDefaults.colors(
                                        thumbColor = NovaRed,
                                        activeTrackColor = NovaRed,
                                        inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 10.dp)
                                )

                                Text(
                                    text = formatDuration(durationMs),
                                    color = NovaTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Bottom Toolbar: Quality, Audio, Subtitles, Speed
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Quality Menu Button
                                Box {
                                    IconButton(onClick = { showQualityMenu = true }) {
                                        Icon(
                                            imageVector = Icons.Default.HighQuality,
                                            contentDescription = "Quality",
                                            tint = Color.White
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = showQualityMenu,
                                        onDismissRequest = { showQualityMenu = false },
                                        modifier = Modifier.background(NovaCardBg)
                                    ) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    "Auto",
                                                    color = if (selectedQualityLabel == "Auto") NovaRed else Color.White
                                                )
                                            },
                                            onClick = {
                                                selectedQualityLabel = "Auto"
                                                exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                                                    .buildUpon()
                                                    .clearOverridesOfType(C.TRACK_TYPE_VIDEO)
                                                    .build()
                                                showQualityMenu = false
                                            }
                                        )
                                        availableQualities.forEach { q ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        q.label,
                                                        color = if (selectedQualityLabel == q.label) NovaRed else Color.White
                                                    )
                                                },
                                                onClick = {
                                                    selectedQualityLabel = q.label
                                                    val tracks = exoPlayer.currentTracks
                                                    val group = tracks.groups.getOrNull(q.trackGroupIndex)
                                                    if (group != null) {
                                                        exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                                                            .buildUpon()
                                                            .setOverrideForType(
                                                                TrackSelectionOverride(group.mediaTrackGroup, q.trackIndex)
                                                            )
                                                            .build()
                                                    }
                                                    showQualityMenu = false
                                                }
                                            )
                                        }
                                    }
                                }

                                // Audio Tracks
                                if (audioTracks.size > 1) {
                                    Box {
                                        IconButton(onClick = { showAudioMenu = true }) {
                                            Icon(
                                                imageVector = Icons.Default.Audiotrack,
                                                contentDescription = "Audio",
                                                tint = Color.White
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = showAudioMenu,
                                            onDismissRequest = { showAudioMenu = false },
                                            modifier = Modifier.background(NovaCardBg)
                                        ) {
                                            audioTracks.forEachIndexed { idx, track ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            track,
                                                            color = if (selectedAudioTrack == idx) NovaRed else Color.White
                                                        )
                                                    },
                                                    onClick = {
                                                        selectedAudioTrack = idx
                                                        showAudioMenu = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                // Speed Control (0.5x, 1x, 1.25x, 1.5x, 2x)
                                if (!isLiveStream) {
                                    Box {
                                        IconButton(onClick = { showSpeedMenu = true }) {
                                            Icon(
                                                imageVector = Icons.Default.Speed,
                                                contentDescription = "Speed",
                                                tint = Color.White
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = showSpeedMenu,
                                            onDismissRequest = { showSpeedMenu = false },
                                            modifier = Modifier.background(NovaCardBg)
                                        ) {
                                            listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { spd ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            "${spd}x",
                                                            color = if (currentSpeed == spd) NovaRed else Color.White
                                                        )
                                                    },
                                                    onClick = {
                                                        currentSpeed = spd
                                                        exoPlayer.playbackParameters = PlaybackParameters(spd)
                                                        showSpeedMenu = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Aspect Ratio Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = resizeModeName,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // SWIPEABLE CHANNEL GUIDE DRAWER (For Live TV)
        AnimatedVisibility(
            visible = showChannelGuide,
            enter = slideInHorizontally { it },
            exit = slideOutHorizontally { it },
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(280.dp)
                    .background(NovaBlack.copy(alpha = 0.95f))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.channel_guide),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showChannelGuide = false }) {
                            Icon(
                                imageVector = Icons.Default.FullscreenExit,
                                contentDescription = "Close Guide",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(liveChannels) { channel ->
                            val isCurrent = channel.id == mediaItem.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isCurrent) NovaRed.copy(alpha = 0.25f) else NovaCardBg)
                                    .clickable {
                                        onChannelSelect(channel)
                                        showChannelGuide = false
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = channel.title,
                                        color = if (isCurrent) NovaRed else Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = channel.category,
                                        color = NovaTextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (isCurrent) NovaRed else Color.White.copy(alpha = 0.15f))
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text(text = channel.quality, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
