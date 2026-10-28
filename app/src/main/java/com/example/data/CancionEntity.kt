package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "canciones",
    indices = [
        Index(value = ["titulo"]),
        Index(value = ["artista"]),
        Index(value = ["isFavorite"])
    ]
)
data class CancionEntity(
    @PrimaryKey val id: Long,
    val titulo: String,
    val artista: String,
    val album: String,
    val duracion: Long,
    val uri: String,
    val albumArtUri: String?,
    val waveformData: String,
    val isFavorite: Boolean = false,
    val lyrics: String? = null
) {
    /**
     * Lazily parsed float array representation of the 50 normalized waveform samples (0.1f - 1.0f).
     */
    fun parsedWaveform(): FloatArray {
        return try {
            val parts = waveformData.split(",")
            if (parts.size >= 50) {
                FloatArray(50) { i -> parts[i].toFloatOrNull() ?: 0.3f }
            } else {
                defaultWaveform(50)
            }
        } catch (_: Exception) {
            defaultWaveform(50)
        }
    }

    companion object {
        fun defaultWaveform(points: Int = 50): FloatArray {
            return FloatArray(points) { i ->
                val progress = i.toFloat() / points
                val wave1 = kotlin.math.sin(progress * Math.PI.toFloat())
                val wave2 = kotlin.math.sin(progress * 4 * Math.PI.toFloat()) * 0.25f
                ((wave1 * 0.65f + wave2 + 0.3f).coerceIn(0.1f, 1.0f))
            }
        }
    }
}
