package com.example.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class CancionRepository(
    private val cancionDao: CancionDao,
    private val playlistDao: PlaylistDao,
    private val audioScanner: LocalAudioScanner
) {

    fun getAllCanciones(): Flow<List<CancionEntity>> {
        return cancionDao.getAllCanciones()
    }

    fun getFavoriteCanciones(): Flow<List<CancionEntity>> {
        return cancionDao.getFavoriteCanciones()
    }

    fun searchCanciones(query: String): Flow<List<CancionEntity>> {
        return if (query.isBlank()) {
            cancionDao.getAllCanciones()
        } else {
            cancionDao.searchCanciones(query.trim())
        }
    }

    suspend fun getCancionById(id: Long): CancionEntity? {
        return cancionDao.getCancionById(id)
    }

    suspend fun toggleFavorite(id: Long, currentIsFavorite: Boolean) = withContext(Dispatchers.IO) {
        cancionDao.setFavorite(id, !currentIsFavorite)
    }

    suspend fun updateLyrics(id: Long, lyrics: String) = withContext(Dispatchers.IO) {
        cancionDao.updateLyrics(id, lyrics)
    }

    suspend fun updateSongMetadata(id: Long, titulo: String, artista: String, album: String) = withContext(Dispatchers.IO) {
        cancionDao.updateSongMetadata(id, titulo, artista, album)
    }

    // Playlists Operations
    fun getAllPlaylists(): Flow<List<PlaylistEntity>> = playlistDao.getAllPlaylists()

    suspend fun createPlaylist(name: String, colorHex: String = "#00E5FF"): Long = withContext(Dispatchers.IO) {
        playlistDao.insertPlaylist(PlaylistEntity(nombre = name.trim(), colorHex = colorHex))
    }

    suspend fun deletePlaylist(playlistId: Long) = withContext(Dispatchers.IO) {
        playlistDao.clearPlaylistSongs(playlistId)
        playlistDao.deletePlaylist(playlistId)
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) = withContext(Dispatchers.IO) {
        playlistDao.addSongToPlaylist(PlaylistSongCrossRef(playlistId = playlistId, cancionId = songId))
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) = withContext(Dispatchers.IO) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }

    fun getSongsForPlaylist(playlistId: Long): Flow<List<CancionEntity>> =
        playlistDao.getSongsForPlaylist(playlistId)

    fun getMostPlayed(limit: Int = 50): Flow<List<CancionEntity>> =
        cancionDao.getMostPlayed(limit)

    fun getRecentlyPlayed(limit: Int = 50): Flow<List<CancionEntity>> =
        cancionDao.getRecentlyPlayed(limit)

    suspend fun recordPlay(id: Long, timestamp: Long = System.currentTimeMillis()) = withContext(Dispatchers.IO) {
        try {
            cancionDao.recordPlay(id, timestamp)
        } catch (_: Exception) {}
    }

    suspend fun refreshLibrary(): Int = withContext(Dispatchers.IO) {
        val existingMap = try {
            cancionDao.getAllCancionesStatic().associateBy { it.id }
        } catch (_: Exception) {
            emptyMap()
        }
        val scanned = audioScanner.scanMediaStore()
        if (scanned.isNotEmpty()) {
            val preserved = scanned.map { s ->
                val old = existingMap[s.id]
                if (old != null) {
                    s.copy(
                        isFavorite = old.isFavorite,
                        lyrics = old.lyrics,
                        playCount = old.playCount,
                        lastPlayedTimestamp = old.lastPlayedTimestamp,
                        addedTimestamp = old.addedTimestamp
                    )
                } else s
            }
            cancionDao.deleteAll()
            cancionDao.insertAll(preserved)
        }
        scanned.size
    }

    suspend fun getCount(): Int = withContext(Dispatchers.IO) {
        cancionDao.getCount()
    }

    /**
     * Trims an audio file between startFraction and endFraction (0.0f..1.0f)
     * directly reading from the InputStream in 8KB chunks without loading the
     * full track into RAM, preserving low-memory constraints (<60MB).
     */
    suspend fun trimAudioTrack(
        context: Context,
        cancion: CancionEntity,
        startFraction: Float,
        endFraction: Float
    ): CancionEntity? = withContext(Dispatchers.IO) {
        try {
            val uri = Uri.parse(cancion.uri)
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null

            val totalBytes = try {
                inputStream.available().toLong().coerceAtLeast(8192L)
            } catch (_: Exception) {
                8192L
            }

            val startByte = (totalBytes * startFraction.coerceIn(0f, 1f)).toLong()
            val endByte = (totalBytes * endFraction.coerceIn(startFraction + 0.05f, 1f)).toLong()
            val bytesToRead = (endByte - startByte).coerceAtLeast(4096L)

            val outputDir = context.getExternalFilesDir("Ringtones") ?: context.filesDir
            if (!outputDir.exists()) outputDir.mkdirs()

            val fileName = "Pulse_Cut_${System.currentTimeMillis()}.mp3"
            val outputFile = File(outputDir, fileName)

            inputStream.use { input ->
                var skipped = 0L
                while (skipped < startByte) {
                    val actual = input.skip(startByte - skipped)
                    if (actual <= 0) break
                    skipped += actual
                }

                FileOutputStream(outputFile).use { output ->
                    val buffer = ByteArray(8192)
                    var totalWritten = 0L
                    var read: Int
                    while (totalWritten < bytesToRead) {
                        val toRead = minOf(buffer.size.toLong(), bytesToRead - totalWritten).toInt()
                        read = input.read(buffer, 0, toRead)
                        if (read <= 0) break
                        output.write(buffer, 0, read)
                        totalWritten += read
                    }
                }
            }

            val trimmedDuration = ((endFraction - startFraction) * cancion.duracion).toLong().coerceAtLeast(1000L)
            val newId = -System.currentTimeMillis()
            val trimmedTitle = "${cancion.titulo} (Cortado)"
            val newWaveform = audioScanner.generatePrecomputedWaveform(newId, trimmedTitle, trimmedDuration)

            val newEntity = CancionEntity(
                id = newId,
                titulo = trimmedTitle,
                artista = cancion.artista,
                album = "Pulse Trims",
                duracion = trimmedDuration,
                uri = Uri.fromFile(outputFile).toString(),
                albumArtUri = cancion.albumArtUri,
                waveformData = newWaveform
            )

            cancionDao.insertAll(listOf(newEntity))
            newEntity
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
