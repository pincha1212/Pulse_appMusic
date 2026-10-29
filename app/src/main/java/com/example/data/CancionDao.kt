package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CancionDao {

    @Query("SELECT * FROM canciones ORDER BY titulo COLLATE NOCASE ASC")
    fun getAllCanciones(): Flow<List<CancionEntity>>

    @Query("SELECT * FROM canciones WHERE titulo LIKE '%' || :query || '%' OR artista LIKE '%' || :query || '%' ORDER BY titulo COLLATE NOCASE ASC")
    fun searchCanciones(query: String): Flow<List<CancionEntity>>

    @Query("SELECT * FROM canciones WHERE id = :id LIMIT 1")
    suspend fun getCancionById(id: Long): CancionEntity?

    @Query("SELECT COUNT(*) FROM canciones")
    suspend fun getCount(): Int

    @Query("SELECT * FROM canciones WHERE isFavorite = 1 ORDER BY titulo COLLATE NOCASE ASC")
    fun getFavoriteCanciones(): Flow<List<CancionEntity>>

    @Query("UPDATE canciones SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE canciones SET lyrics = :lyrics WHERE id = :id")
    suspend fun updateLyrics(id: Long, lyrics: String)

    @Query("UPDATE canciones SET titulo = :titulo, artista = :artista, album = :album WHERE id = :id")
    suspend fun updateSongMetadata(id: Long, titulo: String, artista: String, album: String)

    @Query("SELECT * FROM canciones ORDER BY playCount DESC, titulo COLLATE NOCASE ASC LIMIT :limit")
    fun getMostPlayed(limit: Int = 50): Flow<List<CancionEntity>>

    @Query("SELECT * FROM canciones WHERE lastPlayedTimestamp > 0 ORDER BY lastPlayedTimestamp DESC LIMIT :limit")
    fun getRecentlyPlayed(limit: Int = 50): Flow<List<CancionEntity>>

    @Query("UPDATE canciones SET playCount = playCount + 1, lastPlayedTimestamp = :timestamp WHERE id = :id")
    suspend fun recordPlay(id: Long, timestamp: Long)

    @Query("SELECT * FROM canciones")
    suspend fun getAllCancionesStatic(): List<CancionEntity>

    @Query("SELECT id FROM canciones WHERE isFavorite = 1")
    suspend fun getFavoriteIds(): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(canciones: List<CancionEntity>)

    @Query("DELETE FROM canciones")
    suspend fun deleteAll()
}
