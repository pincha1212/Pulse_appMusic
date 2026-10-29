package com.example.service

import android.app.PendingIntent
import android.content.Intent
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.os.Bundle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.example.MainActivity
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var equalizer: Equalizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var currentLoudnessGainMb: Int = 0
    private var currentBassBoostStrength: Int = 0
    private var currentVirtualizerStrength: Int = 0

    companion object {
        const val CMD_SET_EQ_PRESET = "com.example.pulse.SET_EQ_PRESET"
        const val CMD_SET_EQ_BAND = "com.example.pulse.SET_EQ_BAND"
        const val CMD_TOGGLE_EQ = "com.example.pulse.TOGGLE_EQ"
        const val CMD_SET_LOUDNESS_BOOST = "com.example.pulse.SET_LOUDNESS_BOOST"
        const val CMD_SET_BASS_BOOST = "com.example.pulse.SET_BASS_BOOST"
        const val CMD_SET_VIRTUALIZER = "com.example.pulse.SET_VIRTUALIZER"

        const val ARG_PRESET_NAME = "preset_name"
        const val ARG_BAND_INDEX = "band_index"
        const val ARG_BAND_LEVEL = "band_level"
        const val ARG_EQ_ENABLED = "eq_enabled"
        const val ARG_LOUDNESS_GAIN_MB = "loudness_gain_mb"
        const val ARG_BASS_BOOST_STRENGTH = "bass_boost_strength"
        const val ARG_VIRTUALIZER_STRENGTH = "virtualizer_strength"
    }

    override fun onCreate() {
        super.onCreate()

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, /* handleAudioFocus = */ true)
            .setHandleAudioBecomingNoisy(true)
            .build()

        player.addListener(object : Player.Listener {
            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                if (audioSessionId != C.AUDIO_SESSION_ID_UNSET && audioSessionId != 0) {
                    initAudioEffects(audioSessionId)
                }
            }
        })

        val activityIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            activityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val callback = object : MediaSession.Callback {
            override fun onCustomCommand(
                session: MediaSession,
                controller: MediaSession.ControllerInfo,
                customCommand: SessionCommand,
                args: Bundle
            ): ListenableFuture<SessionResult> {
                when (customCommand.customAction) {
                    CMD_SET_EQ_PRESET -> {
                        val preset = args.getString(ARG_PRESET_NAME) ?: "Flat"
                        applyPreset(preset)
                    }
                    CMD_SET_EQ_BAND -> {
                        val band = args.getInt(ARG_BAND_INDEX, 0)
                        val level = args.getInt(ARG_BAND_LEVEL, 0).toShort()
                        setBandLevel(band, level)
                    }
                    CMD_TOGGLE_EQ -> {
                        val enabled = args.getBoolean(ARG_EQ_ENABLED, true)
                        try {
                            equalizer?.enabled = enabled
                        } catch (_: Exception) {}
                    }
                    CMD_SET_LOUDNESS_BOOST -> {
                        val gainMb = args.getInt(ARG_LOUDNESS_GAIN_MB, 0)
                        setLoudnessGain(gainMb)
                    }
                    CMD_SET_BASS_BOOST -> {
                        val strength = args.getInt(ARG_BASS_BOOST_STRENGTH, 0)
                        setBassBoost(strength)
                    }
                    CMD_SET_VIRTUALIZER -> {
                        val strength = args.getInt(ARG_VIRTUALIZER_STRENGTH, 0)
                        setVirtualizer(strength)
                    }
                }
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
        }

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(pendingIntent)
            .setCallback(callback)
            .build()
    }

    private fun initAudioEffects(sessionId: Int) {
        try {
            equalizer?.release()
            equalizer = Equalizer(0, sessionId).apply {
                enabled = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            loudnessEnhancer?.release()
            loudnessEnhancer = LoudnessEnhancer(sessionId).apply {
                if (currentLoudnessGainMb > 0) {
                    enabled = true
                    setTargetGain(currentLoudnessGainMb)
                } else {
                    enabled = false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            bassBoost?.release()
            bassBoost = BassBoost(0, sessionId).apply {
                if (currentBassBoostStrength > 0 && strengthSupported) {
                    enabled = true
                    setStrength(currentBassBoostStrength.toShort())
                } else {
                    enabled = false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            virtualizer?.release()
            virtualizer = Virtualizer(0, sessionId).apply {
                if (currentVirtualizerStrength > 0 && strengthSupported) {
                    enabled = true
                    setStrength(currentVirtualizerStrength.toShort())
                } else {
                    enabled = false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setLoudnessGain(gainMb: Int) {
        currentLoudnessGainMb = gainMb.coerceIn(0, 1000)
        try {
            loudnessEnhancer?.apply {
                if (currentLoudnessGainMb > 0) {
                    enabled = true
                    setTargetGain(currentLoudnessGainMb)
                } else {
                    setTargetGain(0)
                    enabled = false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setBassBoost(strength: Int) {
        currentBassBoostStrength = strength.coerceIn(0, 1000)
        try {
            bassBoost?.apply {
                if (strengthSupported) {
                    if (currentBassBoostStrength > 0) {
                        enabled = true
                        setStrength(currentBassBoostStrength.toShort())
                    } else {
                        setStrength(0)
                        enabled = false
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setVirtualizer(strength: Int) {
        currentVirtualizerStrength = strength.coerceIn(0, 1000)
        try {
            virtualizer?.apply {
                if (strengthSupported) {
                    if (currentVirtualizerStrength > 0) {
                        enabled = true
                        setStrength(currentVirtualizerStrength.toShort())
                    } else {
                        setStrength(0)
                        enabled = false
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun applyPreset(presetName: String) {
        val eq = equalizer ?: return
        try {
            val numBands = eq.numberOfBands.toInt()
            val minEqLevel = eq.bandLevelRange?.get(0) ?: -1500
            val maxEqLevel = eq.bandLevelRange?.get(1) ?: 1500

            // Apply hardware DSP equalizer curves in millibels (mB)
            val levels = when (presetName.lowercase()) {
                "rock" -> intArrayOf(300, 100, -100, 200, 400)
                "bass boost", "bass" -> intArrayOf(600, 400, 0, -100, -200)
                "vocal" -> intArrayOf(-200, 100, 400, 300, 0)
                "acoustic" -> intArrayOf(200, 300, 100, 200, 100)
                else -> intArrayOf(0, 0, 0, 0, 0) // Flat
            }

            for (i in 0 until minOf(numBands, levels.size)) {
                val target = levels[i].coerceIn(minEqLevel.toInt(), maxEqLevel.toInt()).toShort()
                eq.setBandLevel(i.toShort(), target)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setBandLevel(band: Int, level: Short) {
        try {
            equalizer?.setBandLevel(band.toShort(), level)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        try {
            equalizer?.release()
            equalizer = null
        } catch (_: Exception) {}

        try {
            loudnessEnhancer?.release()
            loudnessEnhancer = null
        } catch (_: Exception) {}

        try {
            bassBoost?.release()
            bassBoost = null
        } catch (_: Exception) {}

        try {
            virtualizer?.release()
            virtualizer = null
        } catch (_: Exception) {}

        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
