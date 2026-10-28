package com.example

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.example.data.AppDatabase
import com.example.data.CancionRepository
import com.example.data.LocalAudioScanner

class PulseApplication : Application(), ImageLoaderFactory {

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val audioScanner: LocalAudioScanner by lazy { LocalAudioScanner(this) }
    val repository: CancionRepository by lazy {
        CancionRepository(database.cancionDao(), database.playlistDao(), audioScanner)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    /**
     * Custom Coil ImageLoader configured with a strict 15 MB LRU memory limit
     * to ensure low active RAM usage (<60 MB total) on 1 GB low-end devices.
     */
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizeBytes(15 * 1024 * 1024) // 15 MB strict limit
                    .build()
            }
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.DISABLED) // Avoid unneeded disk IO on low-end storage
            .respectCacheHeaders(false)
            .crossfade(true)
            .build()
    }

    companion object {
        lateinit var instance: PulseApplication
            private set
    }
}
