package com.example.ui

import android.app.Application
import android.content.ComponentName
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionToken
import androidx.palette.graphics.Palette
import com.example.PulseApplication
import com.example.data.CancionEntity
import com.example.data.CancionRepository
import com.example.data.PlaylistEntity
import com.example.service.PlaybackService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

import android.content.Context
import com.example.BuildConfig
import com.example.ui.util.OtaUpdateInfo
import com.example.ui.util.OtaUpdateManager

enum class LibraryTab {
    ALL, FAVORITES, MOST_PLAYED, RECENTLY_PLAYED, PLAYLISTS, ALBUMS
}

data class AlbumItem(
    val nombre: String,
    val artista: String,
    val canciones: List<CancionEntity>,
    val albumArtUri: String?
)

data class UIState(
    val canciones: List<CancionEntity> = emptyList(),
    val cancionActual: CancionEntity? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val searchQuery: String = "",
    val isAmoledMode: Boolean = false,
    val isScanning: Boolean = false,
    val hasStoragePermission: Boolean = false,
    val eqPreset: String = "Flat",
    val playbackSpeed: Float = 1.0f,
    val playbackPitch: Float = 1.0f,
    val ambientPrimaryColor: Color = Color(0xFF0F0F11),
    val ambientSecondaryColor: Color = Color(0xFF00E5FF),
    val trimStartFraction: Float = 0.0f,
    val trimEndFraction: Float = 1.0f,
    val isTrimModeActive: Boolean = false,
    val isTrimming: Boolean = false,
    val trimSuccessMessage: String? = null,
    val isSettingsSheetOpen: Boolean = false,
    val isPlayerExpanded: Boolean = false,
    val isShuffleEnabled: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val sleepTimerOption: Int = 0, // 0: Off, 15: 15min, 30: 30min, 60: 60min, -1: End of Track
    val sleepTimerRemainingSec: Long = 0L,
    // v2.0.0 features:
    val selectedTab: LibraryTab = LibraryTab.ALL,
    val playlists: List<PlaylistEntity> = emptyList(),
    val selectedPlaylist: PlaylistEntity? = null,
    val playlistSongs: List<CancionEntity> = emptyList(),
    val albums: List<AlbumItem> = emptyList(),
    val selectedAlbum: AlbumItem? = null,
    val loudnessGainMb: Int = 0, // 0 to 1000 mB
    val crossfadeDurationSec: Int = 0, // 0 to 8 sec
    val showLyricsView: Boolean = false,
    val addToPlaylistSong: CancionEntity? = null,
    val showCreatePlaylistDialog: Boolean = false,
    // v3.0.0 features:
    val bassBoostStrength: Int = 0, // 0 to 1000
    val virtualizerStrength: Int = 0, // 0 to 1000
    val playbackQueue: List<CancionEntity> = emptyList(),
    val isQueueSheetOpen: Boolean = false,
    val visualizerMode: Int = 0, // 0: Waveform, 1: Frequency Bars
    // v4.0.0 features:
    val isAutoGenreEqEnabled: Boolean = true,
    val editingSong: CancionEntity? = null,
    // OTA Auto-Update features:
    val githubRepoPath: String = "pincha1212/Pulse_appMusic",
    val isCheckingOtaUpdate: Boolean = false,
    val otaUpdateInfo: OtaUpdateInfo? = null,
    val otaStatusMessage: String? = null
) {
    val progressFraction: Float
        get() {
            val total = if (durationMs > 0) durationMs else (cancionActual?.duracion ?: 0L)
            return if (total > 0) (currentPositionMs.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f
        }
}

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CancionRepository = (application as PulseApplication).repository
    private val _uiState = MutableStateFlow(UIState())
    val uiState: StateFlow<UIState> = _uiState.asStateFlow()

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    private var progressPollingJob: Job? = null
    private var sleepTimerJob: Job? = null
    private var playlistSongsJob: Job? = null
    private var allSongsCache: List<CancionEntity> = emptyList()

    init {
        initMediaController()
        observeDatabase()
        scanStorage()
    }

    private fun initMediaController() {
        val context = getApplication<Application>()
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture = future

        future.addListener(
            {
                try {
                    val controller = future.get()
                    mediaController = controller
                    setupPlayerListener(controller)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            MoreExecutors.directExecutor()
        )
    }

    private fun setupPlayerListener(player: Player) {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) {
                    startProgressPolling()
                } else {
                    stopProgressPolling()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    val dur = player.duration.coerceAtLeast(0L)
                    _uiState.update { it.copy(durationMs = dur) }
                } else if (playbackState == Player.STATE_ENDED) {
                    if (_uiState.value.sleepTimerOption == -1) {
                        setSleepTimer(0)
                        player.pause()
                    } else if (_uiState.value.repeatMode == Player.REPEAT_MODE_ONE) {
                        player.seekTo(0)
                        player.play()
                    } else {
                        skipNext()
                    }
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val mediaId = mediaItem?.mediaId?.toLongOrNull()
                val current = allSongsCache.find { it.id == mediaId }
                _uiState.update {
                    it.copy(
                        cancionActual = current ?: it.cancionActual,
                        durationMs = current?.duracion ?: 0L,
                        currentPositionMs = 0L
                    )
                }
                current?.let { extractAmbientColors(it) }
            }
        })

        _uiState.update {
            it.copy(
                isPlaying = player.isPlaying,
                currentPositionMs = player.currentPosition.coerceAtLeast(0L),
                durationMs = player.duration.coerceAtLeast(0L),
                isShuffleEnabled = player.shuffleModeEnabled,
                repeatMode = player.repeatMode
            )
        }
        if (player.isPlaying) {
            startProgressPolling()
        }
    }

    private fun observeDatabase() {
        viewModelScope.launch {
            repository.getAllCanciones().collectLatest { list ->
                allSongsCache = list
                updateAlbums(list)
                filterCurrentTabList()
                if (_uiState.value.cancionActual == null && list.isNotEmpty()) {
                    val first = list[0]
                    _uiState.update {
                        it.copy(
                            cancionActual = first,
                            durationMs = first.duracion
                        )
                    }
                    extractAmbientColors(first)
                }
            }
        }

        viewModelScope.launch {
            repository.getAllPlaylists().collectLatest { playlists ->
                _uiState.update { it.copy(playlists = playlists) }
            }
        }
    }

    private fun updateAlbums(songs: List<CancionEntity>) {
        val grouped = songs.groupBy { it.album }
            .map { (albumName, albumSongs) ->
                val artist = albumSongs.firstOrNull()?.artista ?: "Varios"
                val artUri = albumSongs.firstOrNull { !it.albumArtUri.isNullOrEmpty() }?.albumArtUri
                AlbumItem(
                    nombre = albumName,
                    artista = artist,
                    canciones = albumSongs,
                    albumArtUri = artUri
                )
            }
            .sortedBy { it.nombre.lowercase() }
        _uiState.update { it.copy(albums = grouped) }
    }

    fun scanStorage() {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true) }
            repository.refreshLibrary()
            _uiState.update { it.copy(isScanning = false) }
        }
    }

    fun onPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(hasStoragePermission = granted) }
        if (granted) {
            scanStorage()
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        filterCurrentTabList()
    }

    fun setLibraryTab(tab: LibraryTab) {
        _uiState.update {
            it.copy(
                selectedTab = tab,
                selectedPlaylist = null,
                selectedAlbum = null
            )
        }
        filterCurrentTabList()
    }

    fun selectPlaylist(playlist: PlaylistEntity?) {
        _uiState.update { it.copy(selectedPlaylist = playlist) }
        playlistSongsJob?.cancel()
        if (playlist != null) {
            playlistSongsJob = viewModelScope.launch {
                repository.getSongsForPlaylist(playlist.id).collectLatest { songs ->
                    _uiState.update { it.copy(playlistSongs = songs) }
                    filterCurrentTabList()
                }
            }
        } else {
            filterCurrentTabList()
        }
    }

    fun selectAlbum(album: AlbumItem?) {
        _uiState.update { it.copy(selectedAlbum = album) }
        filterCurrentTabList()
    }

    private fun filterCurrentTabList() {
        val query = _uiState.value.searchQuery.trim().lowercase()
        val currentTab = _uiState.value.selectedTab
        val selPlaylist = _uiState.value.selectedPlaylist
        val selAlbum = _uiState.value.selectedAlbum

        val baseList: List<CancionEntity> = when {
            selAlbum != null -> selAlbum.canciones
            selPlaylist != null -> _uiState.value.playlistSongs
            currentTab == LibraryTab.FAVORITES -> allSongsCache.filter { it.isFavorite }
            currentTab == LibraryTab.MOST_PLAYED -> allSongsCache.sortedByDescending { it.playCount }
            currentTab == LibraryTab.RECENTLY_PLAYED -> allSongsCache.filter { it.lastPlayedTimestamp > 0 }
                .sortedByDescending { it.lastPlayedTimestamp }
            else -> allSongsCache
        }

        val filtered = if (query.isBlank()) {
            baseList
        } else {
            baseList.filter {
                it.titulo.lowercase().contains(query) || it.artista.lowercase().contains(query)
            }
        }

        _uiState.update { it.copy(canciones = filtered) }
    }

    fun toggleFavorite(cancion: CancionEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(cancion.id, cancion.isFavorite)
            val updated = cancion.copy(isFavorite = !cancion.isFavorite)
            if (_uiState.value.cancionActual?.id == cancion.id) {
                _uiState.update { it.copy(cancionActual = updated) }
            }
        }
    }

    fun createPlaylist(name: String, colorHex: String = "#00E5FF") {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.createPlaylist(name, colorHex)
            _uiState.update { it.copy(showCreatePlaylistDialog = false) }
        }
    }

    fun deletePlaylist(playlist: PlaylistEntity) {
        viewModelScope.launch {
            repository.deletePlaylist(playlist.id)
            if (_uiState.value.selectedPlaylist?.id == playlist.id) {
                selectPlaylist(null)
            }
        }
    }

    fun showAddToPlaylist(cancion: CancionEntity?) {
        _uiState.update { it.copy(addToPlaylistSong = cancion) }
    }

    fun addSongToPlaylist(playlist: PlaylistEntity, cancion: CancionEntity) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlist.id, cancion.id)
            _uiState.update { it.copy(addToPlaylistSong = null) }
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun setShowCreatePlaylistDialog(show: Boolean) {
        _uiState.update { it.copy(showCreatePlaylistDialog = show) }
    }

    fun toggleAmoledMode() {
        _uiState.update { it.copy(isAmoledMode = !it.isAmoledMode) }
    }

    fun setSettingsSheetOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isSettingsSheetOpen = isOpen) }
    }

    fun setPlayerExpanded(isExpanded: Boolean) {
        _uiState.update { it.copy(isPlayerExpanded = isExpanded) }
    }

    fun toggleShuffle() {
        val newShuffle = !_uiState.value.isShuffleEnabled
        _uiState.update { it.copy(isShuffleEnabled = newShuffle) }
        mediaController?.shuffleModeEnabled = newShuffle
    }

    fun toggleRepeatMode() {
        val nextMode = when (_uiState.value.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        _uiState.update { it.copy(repeatMode = nextMode) }
        mediaController?.repeatMode = nextMode
    }

    fun setSleepTimer(optionMinutes: Int) {
        sleepTimerJob?.cancel()
        _uiState.update {
            it.copy(
                sleepTimerOption = optionMinutes,
                sleepTimerRemainingSec = if (optionMinutes > 0) optionMinutes * 60L else 0L
            )
        }

        if (optionMinutes > 0) {
            sleepTimerJob = viewModelScope.launch {
                var remaining = optionMinutes * 60L
                while (remaining > 0 && isActive) {
                    delay(1000)
                    remaining--
                    _uiState.update { it.copy(sleepTimerRemainingSec = remaining) }
                }
                if (remaining <= 0) {
                    togglePlayPause()
                    _uiState.update { it.copy(sleepTimerOption = 0, sleepTimerRemainingSec = 0L) }
                }
            }
        }
    }

    fun setEqPreset(preset: String) {
        _uiState.update { it.copy(eqPreset = preset) }
        val controller = mediaController ?: return
        val args = Bundle().apply {
            putString(PlaybackService.ARG_PRESET_NAME, preset)
        }
        controller.sendCustomCommand(
            SessionCommand(PlaybackService.CMD_SET_EQ_PRESET, Bundle.EMPTY),
            args
        )
    }

    fun setLoudnessBoost(gainMb: Int) {
        val clamped = gainMb.coerceIn(0, 1000)
        _uiState.update { it.copy(loudnessGainMb = clamped) }
        val controller = mediaController ?: return
        val args = Bundle().apply {
            putInt(PlaybackService.ARG_LOUDNESS_GAIN_MB, clamped)
        }
        controller.sendCustomCommand(
            SessionCommand(PlaybackService.CMD_SET_LOUDNESS_BOOST, Bundle.EMPTY),
            args
        )
    }

    fun setBassBoost(strength: Int) {
        val clamped = strength.coerceIn(0, 1000)
        _uiState.update { it.copy(bassBoostStrength = clamped) }
        val controller = mediaController ?: return
        val args = Bundle().apply {
            putInt(PlaybackService.ARG_BASS_BOOST_STRENGTH, clamped)
        }
        controller.sendCustomCommand(
            SessionCommand(PlaybackService.CMD_SET_BASS_BOOST, Bundle.EMPTY),
            args
        )
    }

    fun setVirtualizer(strength: Int) {
        val clamped = strength.coerceIn(0, 1000)
        _uiState.update { it.copy(virtualizerStrength = clamped) }
        val controller = mediaController ?: return
        val args = Bundle().apply {
            putInt(PlaybackService.ARG_VIRTUALIZER_STRENGTH, clamped)
        }
        controller.sendCustomCommand(
            SessionCommand(PlaybackService.CMD_SET_VIRTUALIZER, Bundle.EMPTY),
            args
        )
    }

    fun setVisualizerMode(mode: Int) {
        _uiState.update { it.copy(visualizerMode = mode) }
    }

    fun setQueueSheetOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isQueueSheetOpen = isOpen) }
    }

    fun playNext(song: CancionEntity) {
        val currentQueue = _uiState.value.playbackQueue.toMutableList()
        currentQueue.add(0, song)
        _uiState.update { it.copy(playbackQueue = currentQueue) }
    }

    fun addToQueue(song: CancionEntity) {
        val currentQueue = _uiState.value.playbackQueue.toMutableList()
        currentQueue.add(song)
        _uiState.update { it.copy(playbackQueue = currentQueue) }
    }

    fun removeFromQueue(index: Int) {
        val currentQueue = _uiState.value.playbackQueue.toMutableList()
        if (index in currentQueue.indices) {
            currentQueue.removeAt(index)
            _uiState.update { it.copy(playbackQueue = currentQueue) }
        }
    }

    fun clearQueue() {
        _uiState.update { it.copy(playbackQueue = emptyList()) }
    }

    fun setCrossfadeDuration(seconds: Int) {
        _uiState.update { it.copy(crossfadeDurationSec = seconds.coerceIn(0, 8)) }
    }

    fun setBassBoostStrength(strength: Int) {
        _uiState.update { it.copy(bassBoostStrength = strength.coerceIn(0, 1000)) }
    }

    fun setVirtualizerStrength(strength: Int) {
        _uiState.update { it.copy(virtualizerStrength = strength.coerceIn(0, 1000)) }
    }

    fun setGithubRepoPath(path: String) {
        _uiState.update { it.copy(githubRepoPath = path) }
    }

    fun checkForOtaUpdates() {
        val repo = _uiState.value.githubRepoPath
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingOtaUpdate = true, otaStatusMessage = "Conectando con GitHub Releases...") }
            val info = OtaUpdateManager.checkForUpdates(repo, BuildConfig.VERSION_NAME)
            if (info != null) {
                _uiState.update {
                    it.copy(
                        isCheckingOtaUpdate = false,
                        otaUpdateInfo = info,
                        otaStatusMessage = if (info.isUpdateAvailable) "¡Nueva versión ${info.tagName} disponible!" else "¡Pulse v${BuildConfig.VERSION_NAME} está actualizado!"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isCheckingOtaUpdate = false,
                        otaStatusMessage = "No se pudo comprobar. Verifica tu conexión a internet."
                    )
                }
            }
        }
    }

    fun triggerOtaInstall(context: Context) {
        val info = _uiState.value.otaUpdateInfo ?: return
        OtaUpdateManager.downloadAndInstallApk(
            context = context,
            downloadUrl = info.downloadUrl,
            versionName = info.versionName
        ) { status ->
            _uiState.update { it.copy(otaStatusMessage = status) }
        }
    }

    fun toggleLyricsView() {
        _uiState.update { it.copy(showLyricsView = !it.showLyricsView) }
    }

    fun saveLyrics(songId: Long, lyrics: String) {
        viewModelScope.launch {
            repository.updateLyrics(songId, lyrics)
            val current = _uiState.value.cancionActual
            if (current?.id == songId) {
                _uiState.update { it.copy(cancionActual = current.copy(lyrics = lyrics)) }
            }
        }
    }

    fun toggleAutoGenreEq() {
        _uiState.update { it.copy(isAutoGenreEqEnabled = !it.isAutoGenreEqEnabled) }
    }

    fun setEditingSong(song: CancionEntity?) {
        _uiState.update { it.copy(editingSong = song) }
    }

    fun saveEditedSongMetadata(songId: Long, title: String, artist: String, album: String) {
        viewModelScope.launch {
            val cleanTitle = title.trim().ifBlank { "Sin título" }
            val cleanArtist = artist.trim().ifBlank { "Artista desconocido" }
            val cleanAlbum = album.trim().ifBlank { "Álbum desconocido" }

            repository.updateSongMetadata(songId, cleanTitle, cleanArtist, cleanAlbum)

            val current = _uiState.value.cancionActual
            if (current?.id == songId) {
                _uiState.update {
                    it.copy(
                        cancionActual = current.copy(
                            titulo = cleanTitle,
                            artista = cleanArtist,
                            album = cleanAlbum
                        )
                    )
                }
            }
            _uiState.update { it.copy(editingSong = null) }
        }
    }

    fun setPlaybackSpeedAndPitch(speed: Float, pitch: Float) {
        val newSpeed = speed.coerceIn(0.5f, 2.0f)
        val newPitch = pitch.coerceIn(0.5f, 2.0f)
        _uiState.update { it.copy(playbackSpeed = newSpeed, playbackPitch = newPitch) }
        mediaController?.playbackParameters = PlaybackParameters(newSpeed, newPitch)
    }

    fun toggleTrimMode() {
        _uiState.update {
            it.copy(
                isTrimModeActive = !it.isTrimModeActive,
                trimStartFraction = 0.1f,
                trimEndFraction = 0.9f,
                trimSuccessMessage = null
            )
        }
    }

    fun setTrimRange(start: Float, end: Float) {
        _uiState.update {
            it.copy(
                trimStartFraction = start.coerceIn(0f, 0.95f),
                trimEndFraction = end.coerceIn(start + 0.05f, 1f)
            )
        }
    }

    fun trimSelectedTrack() {
        val song = _uiState.value.cancionActual ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isTrimming = true) }
            val context = getApplication<Application>()
            val result = repository.trimAudioTrack(
                context,
                song,
                _uiState.value.trimStartFraction,
                _uiState.value.trimEndFraction
            )
            _uiState.update {
                it.copy(
                    isTrimming = false,
                    isTrimModeActive = false,
                    trimSuccessMessage = if (result != null) "Tono guardado: ${result.titulo}" else "Error al cortar audio"
                )
            }
        }
    }

    fun clearTrimMessage() {
        _uiState.update { it.copy(trimSuccessMessage = null) }
    }

    fun play(cancion: CancionEntity) {
        val now = System.currentTimeMillis()
        val updatedSong = cancion.copy(
            playCount = cancion.playCount + 1,
            lastPlayedTimestamp = now
        )
        _uiState.update {
            it.copy(
                cancionActual = updatedSong,
                durationMs = cancion.duracion,
                currentPositionMs = 0L
            )
        }
        extractAmbientColors(cancion)

        if (_uiState.value.isAutoGenreEqEnabled) {
            val titleLower = cancion.titulo.lowercase()
            val artistLower = cancion.artista.lowercase()
            val albumLower = cancion.album.lowercase()
            val combined = "$titleLower $artistLower $albumLower"

            val autoPreset = when {
                combined.contains("rock") || combined.contains("metal") || combined.contains("guitar") -> "Rock"
                combined.contains("bass") || combined.contains("synth") || combined.contains("cyber") || combined.contains("techno") || combined.contains("trap") || combined.contains("dance") -> "Bass Boost"
                combined.contains("vocal") || combined.contains("pop") || combined.contains("voice") || combined.contains("sing") -> "Vocal"
                combined.contains("acoustic") || combined.contains("jazz") || combined.contains("ambient") || combined.contains("piano") || combined.contains("nocturne") -> "Acústico"
                else -> "Flat"
            }
            setEqPreset(autoPreset)
        }

        // Record play count and last played in database asynchronously
        viewModelScope.launch {
            repository.recordPlay(cancion.id, now)
        }

        val controller = mediaController ?: return

        val mediaItem = MediaItem.Builder()
            .setMediaId(cancion.id.toString())
            .setUri(cancion.uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(cancion.titulo)
                    .setArtist(cancion.artista)
                    .setAlbumTitle(cancion.album)
                    .build()
            )
            .build()

        controller.setMediaItem(mediaItem)
        controller.prepare()
        controller.play()
    }

    fun togglePlayPause() {
        val controller = mediaController
        if (controller == null) {
            _uiState.update { it.copy(isPlaying = !it.isPlaying) }
            return
        }

        if (controller.isPlaying) {
            controller.pause()
        } else {
            if (controller.playbackState == Player.STATE_IDLE || controller.mediaItemCount == 0) {
                _uiState.value.cancionActual?.let { play(it) }
            } else {
                controller.play()
            }
        }
    }

    fun seekToFraction(fraction: Float) {
        val total = if (_uiState.value.durationMs > 0) {
            _uiState.value.durationMs
        } else {
            _uiState.value.cancionActual?.duracion ?: 0L
        }
        if (total <= 0) return

        val targetMs = (fraction * total).toLong().coerceIn(0L, total)
        _uiState.update { it.copy(currentPositionMs = targetMs) }
        mediaController?.seekTo(targetMs)
    }

    fun skipNext() {
        // 1. If there are items in the user's custom Play Next / Queue, consume the front item
        val queue = _uiState.value.playbackQueue
        if (queue.isNotEmpty()) {
            val nextSong = queue.first()
            _uiState.update { it.copy(playbackQueue = queue.drop(1)) }
            play(nextSong)
            return
        }

        // 2. Otherwise navigate the current active library/playlist
        val list = if (_uiState.value.canciones.isNotEmpty()) _uiState.value.canciones else allSongsCache
        if (list.isEmpty()) return

        if (_uiState.value.isShuffleEnabled) {
            val randomIndex = (0 until list.size).random()
            play(list[randomIndex])
            return
        }

        val current = _uiState.value.cancionActual ?: return
        val currentIndex = list.indexOfFirst { it.id == current.id }
        val nextIndex = if (currentIndex != -1 && currentIndex < list.size - 1) currentIndex + 1 else 0
        play(list[nextIndex])
    }

    fun skipPrevious() {
        val current = _uiState.value.cancionActual ?: return
        val list = if (_uiState.value.canciones.isNotEmpty()) _uiState.value.canciones else allSongsCache
        if (list.isEmpty()) return

        if (_uiState.value.currentPositionMs > 3000L) {
            mediaController?.seekTo(0L)
            _uiState.update { it.copy(currentPositionMs = 0L) }
            return
        }

        val currentIndex = list.indexOfFirst { it.id == current.id }
        val prevIndex = if (currentIndex > 0) currentIndex - 1 else list.size - 1
        play(list[prevIndex])
    }

    private fun startProgressPolling() {
        progressPollingJob?.cancel()
        progressPollingJob = viewModelScope.launch {
            while (isActive) {
                val pos = mediaController?.currentPosition?.coerceAtLeast(0L) ?: _uiState.value.currentPositionMs
                val dur = mediaController?.duration?.coerceAtLeast(0L) ?: _uiState.value.durationMs
                _uiState.update {
                    it.copy(
                        currentPositionMs = pos,
                        durationMs = if (dur > 0) dur else it.durationMs
                    )
                }
                delay(200)
            }
        }
    }

    private fun stopProgressPolling() {
        progressPollingJob?.cancel()
        progressPollingJob = null
    }

    private fun extractAmbientColors(cancion: CancionEntity) {
        if (_uiState.value.isAmoledMode) return
        val uriStr = cancion.albumArtUri ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val uri = Uri.parse(uriStr)
                val stream = context.contentResolver.openInputStream(uri) ?: return@launch

                val options = BitmapFactory.Options().apply {
                    inSampleSize = 8
                    inPreferredConfig = android.graphics.Bitmap.Config.RGB_565
                }
                val bitmap = BitmapFactory.decodeStream(stream, null, options)
                stream.close()

                if (bitmap != null) {
                    val palette = Palette.from(bitmap).maximumColorCount(8).generate()
                    val domColor = palette.getDominantColor(Color(0xFF0F0F11).toArgb())
                    val vibColor = palette.getVibrantColor(palette.getMutedColor(Color(0xFF00E5FF).toArgb()))

                    val primary = Color(domColor).copy(alpha = 0.35f)
                    val secondary = Color(vibColor)

                    _uiState.update {
                        it.copy(
                            ambientPrimaryColor = primary,
                            ambientSecondaryColor = secondary
                        )
                    }
                    bitmap.recycle()
                }
            } catch (_: Exception) {}
        }
    }

    override fun onCleared() {
        stopProgressPolling()
        sleepTimerJob?.cancel()
        playlistSongsJob?.cancel()
        controllerFuture?.let { MediaController.releaseFuture(it) }
        mediaController = null
        super.onCleared()
    }
}
