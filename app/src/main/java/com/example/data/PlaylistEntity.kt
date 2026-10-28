package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "playlists",
    indices = [Index(value = ["nombre"])]
)
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val nombre: String,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val colorHex: String = "#00E5FF"
)

@Entity(
    tableName = "playlist_canciones",
    primaryKeys = ["playlistId", "cancionId"],
    indices = [
        Index(value = ["playlistId"]),
        Index(value = ["cancionId"])
    ]
)
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val cancionId: Long,
    val addedAt: Long = System.currentTimeMillis()
)

data class PlaylistWithSongCount(
    val playlist: PlaylistEntity,
    val songCount: Int
)
