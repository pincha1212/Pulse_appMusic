package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.data.CancionEntity
import com.example.data.PlaylistEntity
import com.example.ui.components.ExpandedPlayerSheet
import com.example.ui.components.MiniPlayerBar
import com.example.ui.components.SettingsBottomSheet
import com.example.ui.components.SongItemRow
import com.example.ui.theme.AccentGradientEnd
import com.example.ui.theme.AmoledBackground
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.PulseTheme
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.util.HapticFeedbackHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PulseApp(viewModel: MusicViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val settingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val expandedPlayerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val addToPlaylistSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // BackHandler for sub-views
    BackHandler(enabled = uiState.selectedPlaylist != null || uiState.selectedAlbum != null || uiState.selectedTab != LibraryTab.ALL) {
        if (uiState.selectedPlaylist != null) {
            viewModel.selectPlaylist(null)
        } else if (uiState.selectedAlbum != null) {
            viewModel.selectAlbum(null)
        } else {
            viewModel.setLibraryTab(LibraryTab.ALL)
        }
    }

    // GPU-accelerated ambient background color transitions (tween 800ms)
    val animatedBgPrimary by animateColorAsState(
        targetValue = if (uiState.isAmoledMode) AmoledBackground else uiState.ambientPrimaryColor,
        animationSpec = tween(800),
        label = "bgPrimary"
    )
    val animatedBgSecondary by animateColorAsState(
        targetValue = if (uiState.isAmoledMode) AmoledBackground else uiState.ambientSecondaryColor.copy(alpha = 0.15f),
        animationSpec = tween(800),
        label = "bgSecondary"
    )

    // Audio storage permission handling
    val requiredPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onPermissionResult(isGranted)
    }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            requiredPermission
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.onPermissionResult(granted)
        if (!granted) {
            permissionLauncher.launch(requiredPermission)
        }
    }

    LaunchedEffect(uiState.trimSuccessMessage) {
        uiState.trimSuccessMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearTrimMessage()
        }
    }

    PulseTheme(isAmoledMode = uiState.isAmoledMode) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (uiState.isAmoledMode) {
                        Brush.verticalGradient(listOf(AmoledBackground, AmoledBackground))
                    } else {
                        Brush.verticalGradient(
                            listOf(
                                animatedBgPrimary,
                                animatedBgSecondary,
                                DarkBackground
                            )
                        )
                    }
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Top Header Zone with Settings Gear Button
                TopHeaderSection(
                    searchQuery = uiState.searchQuery,
                    onSearchQueryChange = viewModel::setSearchQuery,
                    onOpenSettings = {
                        HapticFeedbackHelper.click(context)
                        viewModel.setSettingsSheetOpen(true)
                    },
                    onClearSearch = {
                        HapticFeedbackHelper.click(context)
                        viewModel.setSearchQuery("")
                        keyboardController?.hide()
                    }
                )

                // Library Navigation Filter Chips (Todas, Favoritos, Playlists, Álbumes)
                LibraryFilterChips(
                    selectedTab = uiState.selectedTab,
                    onTabSelected = { tab ->
                        HapticFeedbackHelper.click(context)
                        viewModel.setLibraryTab(tab)
                    }
                )

                // Sub-header with Track count / Section label & Micro memory badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val countText = when (uiState.selectedTab) {
                        LibraryTab.PLAYLISTS -> if (uiState.selectedPlaylist != null) "${uiState.canciones.size} pistas" else "${uiState.playlists.size} listas"
                        LibraryTab.ALBUMS -> if (uiState.selectedAlbum != null) "${uiState.canciones.size} pistas" else "${uiState.albums.size} álbumes"
                        LibraryTab.FAVORITES -> "${uiState.canciones.size} favoritos"
                        else -> stringResource(R.string.tracks_count, uiState.canciones.size)
                    }

                    Text(
                        text = countText,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(ElectricCyan)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.memory_usage),
                            color = ElectricCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Storage Permission banner if not granted
                if (!uiState.hasStoragePermission) {
                    StoragePermissionBanner(
                        onRequestPermission = { permissionLauncher.launch(requiredPermission) }
                    )
                }

                // Center Content Area: Switched by Tab and Drill-down
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when {
                        // Playlists Root View (List of playlists + Create New button)
                        uiState.selectedTab == LibraryTab.PLAYLISTS && uiState.selectedPlaylist == null -> {
                            PlaylistsListView(
                                playlists = uiState.playlists,
                                onCreatePlaylistClick = {
                                    HapticFeedbackHelper.click(context)
                                    viewModel.setShowCreatePlaylistDialog(true)
                                },
                                onPlaylistClick = { playlist ->
                                    HapticFeedbackHelper.click(context)
                                    viewModel.selectPlaylist(playlist)
                                },
                                onDeletePlaylist = { playlist ->
                                    HapticFeedbackHelper.click(context)
                                    viewModel.deletePlaylist(playlist)
                                }
                            )
                        }

                        // Albums Root View (List of albums)
                        uiState.selectedTab == LibraryTab.ALBUMS && uiState.selectedAlbum == null -> {
                            AlbumsListView(
                                albums = uiState.albums,
                                onAlbumClick = { album ->
                                    HapticFeedbackHelper.click(context)
                                    viewModel.selectAlbum(album)
                                }
                            )
                        }

                        // Song Lists (ALL, FAVORITES, or inside a selected Playlist/Album)
                        else -> {
                            val listState = rememberLazyListState()
                            Column(modifier = Modifier.fillMaxSize()) {
                                // Sub-view back navigation header if viewing a specific playlist or album
                                if (uiState.selectedPlaylist != null || uiState.selectedAlbum != null) {
                                    SubViewHeader(
                                        title = uiState.selectedPlaylist?.nombre ?: uiState.selectedAlbum?.nombre ?: "",
                                        subtitle = if (uiState.selectedPlaylist != null) "Lista de reproducción" else uiState.selectedAlbum?.artista ?: "",
                                        onBack = {
                                            HapticFeedbackHelper.click(context)
                                            if (uiState.selectedPlaylist != null) viewModel.selectPlaylist(null)
                                            else viewModel.selectAlbum(null)
                                        }
                                    )
                                }

                                if (uiState.canciones.isEmpty() && !uiState.isScanning) {
                                    EmptyTracksView(
                                        isSearching = uiState.searchQuery.isNotEmpty(),
                                        onScanClick = viewModel::scanStorage
                                    )
                                } else {
                                    LazyColumn(
                                        state = listState,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                            .testTag("tracks_list"),
                                        contentPadding = PaddingValues(top = 4.dp, bottom = 120.dp)
                                    ) {
                                        items(
                                            items = uiState.canciones,
                                            key = { it.id }
                                        ) { song ->
                                            SongItemRow(
                                                cancion = song,
                                                isPlaying = uiState.isPlaying && uiState.cancionActual?.id == song.id,
                                                isCurrent = uiState.cancionActual?.id == song.id,
                                                onSongClick = { viewModel.play(song) },
                                                onToggleFavorite = { viewModel.toggleFavorite(it) },
                                                onAddToPlaylist = { viewModel.showAddToPlaylist(it) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Zone: Compact Mini Player Bar
            AnimatedVisibility(
                visible = uiState.cancionActual != null && !uiState.isPlayerExpanded,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
            ) {
                uiState.cancionActual?.let { currentSong ->
                    MiniPlayerBar(
                        cancion = currentSong,
                        isPlaying = uiState.isPlaying,
                        progressFraction = uiState.progressFraction,
                        waveformArray = currentSong.parsedWaveform(),
                        onTogglePlayPause = viewModel::togglePlayPause,
                        onExpandPlayer = { viewModel.setPlayerExpanded(true) },
                        onSeek = viewModel::seekToFraction
                    )
                }
            }

            // Settings Modal Bottom Sheet
            if (uiState.isSettingsSheetOpen) {
                SettingsBottomSheet(
                    sheetState = settingsSheetState,
                    isAmoledMode = uiState.isAmoledMode,
                    onToggleAmoled = viewModel::toggleAmoledMode,
                    eqPreset = uiState.eqPreset,
                    onEqPresetSelected = viewModel::setEqPreset,
                    speed = uiState.playbackSpeed,
                    pitch = uiState.playbackPitch,
                    onSpeedPitchChange = viewModel::setPlaybackSpeedAndPitch,
                    loudnessGainMb = uiState.loudnessGainMb,
                    onLoudnessGainChange = viewModel::setLoudnessBoost,
                    bassBoostStrength = uiState.bassBoostStrength,
                    onBassBoostChange = viewModel::setBassBoostStrength,
                    virtualizerStrength = uiState.virtualizerStrength,
                    onVirtualizerChange = viewModel::setVirtualizerStrength,
                    crossfadeDurationSec = uiState.crossfadeDurationSec,
                    onCrossfadeDurationChange = viewModel::setCrossfadeDuration,
                    isScanning = uiState.isScanning,
                    onScanStorage = viewModel::scanStorage,
                    githubRepoPath = uiState.githubRepoPath,
                    onGithubRepoPathChange = viewModel::setGithubRepoPath,
                    isCheckingOtaUpdate = uiState.isCheckingOtaUpdate,
                    otaUpdateInfo = uiState.otaUpdateInfo,
                    otaStatusMessage = uiState.otaStatusMessage,
                    onCheckOtaUpdate = viewModel::checkForOtaUpdates,
                    onTriggerOtaInstall = { viewModel.triggerOtaInstall(context) },
                    onDismissRequest = { viewModel.setSettingsSheetOpen(false) }
                )
            }

            // Full Expanded Player Sheet (with Waveform, Lyrics, Repeat, Shuffle, Timer, Favorite)
            if (uiState.isPlayerExpanded && uiState.cancionActual != null) {
                ExpandedPlayerSheet(
                    sheetState = expandedPlayerSheetState,
                    cancion = uiState.cancionActual!!,
                    isPlaying = uiState.isPlaying,
                    progressFraction = uiState.progressFraction,
                    currentPositionMs = uiState.currentPositionMs,
                    durationMs = uiState.durationMs,
                    isShuffleEnabled = uiState.isShuffleEnabled,
                    onToggleShuffle = viewModel::toggleShuffle,
                    repeatMode = uiState.repeatMode,
                    onToggleRepeatMode = viewModel::toggleRepeatMode,
                    sleepTimerOption = uiState.sleepTimerOption,
                    sleepTimerRemainingSec = uiState.sleepTimerRemainingSec,
                    onSetSleepTimer = viewModel::setSleepTimer,
                    isTrimModeActive = uiState.isTrimModeActive,
                    onToggleTrimMode = viewModel::toggleTrimMode,
                    trimStartFraction = uiState.trimStartFraction,
                    trimEndFraction = uiState.trimEndFraction,
                    onTrimRangeChange = viewModel::setTrimRange,
                    isTrimming = uiState.isTrimming,
                    onTrimConfirm = viewModel::trimSelectedTrack,
                    onTogglePlayPause = viewModel::togglePlayPause,
                    onSkipNext = viewModel::skipNext,
                    onSkipPrevious = viewModel::skipPrevious,
                    onSeek = viewModel::seekToFraction,
                    isFavorite = uiState.cancionActual?.isFavorite == true,
                    onToggleFavorite = { uiState.cancionActual?.let { viewModel.toggleFavorite(it) } },
                    showLyricsView = uiState.showLyricsView,
                    onToggleLyricsView = viewModel::toggleLyricsView,
                    visualizerMode = uiState.visualizerMode,
                    onToggleVisualizerMode = { viewModel.setVisualizerMode((uiState.visualizerMode + 1) % 3) },
                    onDismissRequest = { viewModel.setPlayerExpanded(false) }
                )
            }

            // Add To Playlist Modal Sheet
            if (uiState.addToPlaylistSong != null) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.showAddToPlaylist(null) },
                    sheetState = addToPlaylistSheetState,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = TextPrimary,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = "Añadir a lista de reproducción",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = uiState.addToPlaylistSong?.titulo ?: "",
                            fontSize = 12.sp,
                            color = ElectricCyan,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        if (uiState.playlists.isEmpty()) {
                            Text(
                                text = "No tienes listas creadas aún.",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            uiState.playlists.forEach { playlist ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            HapticFeedbackHelper.click(context)
                                            uiState.addToPlaylistSong?.let {
                                                viewModel.addSongToPlaylist(playlist, it)
                                            }
                                        }
                                        .padding(vertical = 10.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QueueMusic,
                                        contentDescription = null,
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = playlist.nombre,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Button to create new playlist right from here
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ElectricCyan.copy(alpha = 0.15f))
                                .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .clickable {
                                HapticFeedbackHelper.click(context)
                                viewModel.setShowCreatePlaylistDialog(true)
                            }
                            .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Crear nueva lista",
                                    color = ElectricCyan,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }

            // Create Playlist Dialog
            if (uiState.showCreatePlaylistDialog) {
                var newPlaylistName by remember { mutableStateOf("") }
                AlertDialog(
                    onDismissRequest = { viewModel.setShowCreatePlaylistDialog(false) },
                    title = {
                        Text(
                            text = "Nueva Lista de Reproducción",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                    },
                    text = {
                        Column {
                            Text(
                                text = "Introduce el nombre para tu nueva colección:",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newPlaylistName,
                                onValueChange = { newPlaylistName = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                placeholder = { Text("Ej: Favoritas 2026", color = TextSecondary) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElectricCyan,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (newPlaylistName.isNotBlank()) {
                                    HapticFeedbackHelper.click(context)
                                    viewModel.createPlaylist(newPlaylistName)
                                }
                            }
                        ) {
                            Text("Crear", color = ElectricCyan, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.setShowCreatePlaylistDialog(false) }) {
                            Text("Cancelar", color = TextSecondary)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(20.dp)
                )
            }

            // Edit Song Metadata Dialog (v4.0.0 Tag Editor)
            uiState.editingSong?.let { song ->
                var editTitle by remember { mutableStateOf(song.titulo) }
                var editArtist by remember { mutableStateOf(song.artista) }
                var editAlbum by remember { mutableStateOf(song.album) }

                AlertDialog(
                    onDismissRequest = { viewModel.setEditingSong(null) },
                    title = {
                        Text(
                            text = "Editar Metadatos",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = editTitle,
                                onValueChange = { editTitle = it },
                                label = { Text("Título de la canción", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElectricCyan,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            OutlinedTextField(
                                value = editArtist,
                                onValueChange = { editArtist = it },
                                label = { Text("Artista", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElectricCyan,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            OutlinedTextField(
                                value = editAlbum,
                                onValueChange = { editAlbum = it },
                                label = { Text("Álbum", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElectricCyan,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                HapticFeedbackHelper.click(context)
                                viewModel.saveEditedSongMetadata(song.id, editTitle, editArtist, editAlbum)
                            }
                        ) {
                            Text("Guardar", color = ElectricCyan, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.setEditingSong(null) }) {
                            Text("Cancelar", color = TextSecondary)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }
    }
}

/**
 * Category Navigation Filter Chips for v2.0.0 (Todas, Favoritos, Listas, Álbumes)
 */
@Composable
private fun LibraryFilterChips(
    selectedTab: LibraryTab,
    onTabSelected: (LibraryTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val tabs = listOf(
        LibraryTab.ALL to "Todas",
        LibraryTab.FAVORITES to "Favoritos ❤️",
        LibraryTab.PLAYLISTS to "Listas 📁",
        LibraryTab.ALBUMS to "Álbumes 💿"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEach { (tab, label) ->
            val isSelected = selectedTab == tab
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) ElectricCyan.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                    .border(
                        1.dp,
                        if (isSelected) ElectricCyan else Color.Transparent,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onTabSelected(tab) }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
                    .testTag("tab_${tab.name.lowercase()}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) ElectricCyan else TextSecondary
                )
            }
        }
    }
}

/**
 * Sub-view header when navigating inside a playlist or album
 */
@Composable
private fun SubViewHeader(
    title: String,
    subtitle: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = ElectricCyan,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Playlists List View
 */
@Composable
private fun PlaylistsListView(
    playlists: List<PlaylistEntity>,
    onCreatePlaylistClick: () -> Unit,
    onPlaylistClick: (PlaylistEntity) -> Unit,
    onDeletePlaylist: (PlaylistEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("playlists_list"),
        contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp)
    ) {
        // Create Playlist Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .clickable { onCreatePlaylistClick() }
                    .testTag("create_playlist_button"),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ElectricCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Crear nueva lista",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                        Text(
                            text = "Organiza tus canciones favoritas",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        items(playlists, key = { it.id }) { playlist ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 5.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .clickable { onPlaylistClick(playlist) },
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(NeonViolet.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QueueMusic,
                                contentDescription = null,
                                tint = NeonViolet,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = playlist.nombre,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Lista de reproducción",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = { onDeletePlaylist(playlist) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar lista",
                            tint = TextSecondary.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Albums List View
 */
@Composable
private fun AlbumsListView(
    albums: List<AlbumItem>,
    onAlbumClick: (AlbumItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("albums_list"),
        contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp)
    ) {
        items(albums, key = { it.nombre }) { album ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 5.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .clickable { onAlbumClick(album) },
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!album.albumArtUri.isNullOrEmpty()) {
                            AsyncImage(
                                model = album.albumArtUri,
                                contentDescription = "Album art",
                                modifier = Modifier.size(50.dp),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Album,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = album.nombre,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${album.artista} • ${album.canciones.size} pistas",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * Minimalist Top Header with brand logo, realtime search pill and Settings Gear Button (v2.0.0)
 */
@Composable
private fun TopHeaderSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onClearSearch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(listOf(ElectricCyan, AccentGradientEnd))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Pulse Logo",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "PULSE",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.width(6.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ElectricCyan.copy(alpha = 0.15f))
                        .border(1.dp, ElectricCyan.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "v4.1.0",
                        color = ElectricCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Clean Minimalist Settings Gear Button
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("settings_gear_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Ajustes",
                    tint = TextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Real-time Search Input Field
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    1.dp,
                    if (searchQuery.isNotEmpty()) ElectricCyan.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    RoundedCornerShape(16.dp)
                ),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = if (searchQuery.isNotEmpty()) ElectricCyan else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_input"),
                    textStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(ElectricCyan),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {}),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = stringResource(R.string.search_placeholder),
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
                )

                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = onClearSearch,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StoragePermissionBanner(
    onRequestPermission: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, NeonViolet.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.permission_required),
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.permission_description),
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(NeonViolet)
                    .clickable { onRequestPermission() }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("grant_permission_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.grant_permission),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EmptyTracksView(
    isSearching: Boolean,
    onScanClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp, start = 32.dp, end = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.LibraryMusic,
                contentDescription = null,
                tint = ElectricCyan.copy(alpha = 0.8f),
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isSearching) stringResource(R.string.no_tracks_found) else "Biblioteca vacía",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isSearching) {
                "Intenta buscar con otros términos"
            } else {
                stringResource(R.string.no_tracks_sub)
            },
            color = TextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )

        if (!isSearching) {
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.horizontalGradient(listOf(ElectricCyan, AccentGradientEnd))
                    )
                    .clickable { onScanClick() }
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .testTag("empty_scan_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.scan_storage),
                    color = Color.Black,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
