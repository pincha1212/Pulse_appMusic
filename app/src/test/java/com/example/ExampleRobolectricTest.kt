package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.CancionEntity
import com.example.data.LocalAudioScanner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        val appVersion = context.getString(R.string.app_version)
        assertEquals("Pulse", appName)
        assertEquals("v2.0.0", appVersion)
    }

    @Test
    fun `verify v2 entity fields favorites and lyrics`() {
        val entity = CancionEntity(
            id = 2L,
            titulo = "Cyber Groove",
            artista = "Neon Pulse",
            album = "Digital Age",
            duracion = 210000L,
            uri = "content://media/external/audio/media/2",
            albumArtUri = null,
            waveformData = CancionEntity.defaultWaveform().joinToString(","),
            isFavorite = true,
            lyrics = "Verse 1: Lights in the grid"
        )
        assertTrue(entity.isFavorite)
        assertEquals("Verse 1: Lights in the grid", entity.lyrics)
    }

    @Test
    fun `verify precomputed waveform produces 50 normalized points`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val scanner = LocalAudioScanner(context)
        val waveformStr = scanner.generatePrecomputedWaveform(12345L, "Test Track", 180000L)
        val parts = waveformStr.split(",")
        assertEquals(50, parts.size)

        val entity = CancionEntity(
            id = 1L,
            titulo = "Test Track",
            artista = "Test Artist",
            album = "Test Album",
            duracion = 180000L,
            uri = "content://media/external/audio/media/1",
            albumArtUri = null,
            waveformData = waveformStr
        )

        val parsed = entity.parsedWaveform()
        assertEquals(50, parsed.size)
        for (v in parsed) {
            assertTrue("Waveform point should be between 0.1 and 1.0, got $v", v in 0.10f..1.00f)
        }
    }
}
