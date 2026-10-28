package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Query("SELECT * FROM playlists ORDER BY nombre COLLATE NOCASE ASC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSongToPlaylist(crossRef: PlaylistSongCrossRef)

    @Query("DELETE FROM playlist_canciones WHERE playlistId = :playlistId AND cancionId = :cancionId")
    suspend fun removeSongFromPlaylist(playlistId: Long, cancionId: Long)

    @Query("""
        SELECT c.* FROM canciones c
        INNER JOIN playlist_canciones pc ON c.id = pc.cancionId
        WHERE pc.playlistId = :playlistId
        ORDER BY pc.addedAt ASC
    """)
    fun getSongsForPlaylist(playlistId: Long): Flow<List<CancionEntity>>

    @Query("SELECT COUNT(*) FROM playlist_canciones WHERE playlistId = :playlistId")
    fun getSongCountForPlaylist(playlistId: Long): Flow<Int>

    @Query("DELETE FROM playlist_canciones WHERE playlistId = :playlistId")
    suspend fun clearPlaylistSongs(playlistId: Long)
}
