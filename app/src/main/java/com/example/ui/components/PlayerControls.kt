package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.CancionEntity
import com.example.ui.util.HapticFeedbackHelper
import com.example.ui.theme.AccentGradientEnd
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.util.Locale

fun formatDuration(ms: Long): String {
    val totalSec = (ms / 1000).coerceAtLeast(0)
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format(Locale.US, "%d:%02d", min, sec)
}

/**
 * Modern tactile Song Row with micro-spacing and no generic dividers.
 */
@Composable
fun SongItemRow(
    cancion: CancionEntity,
    isPlaying: Boolean,
    isCurrent: Boolean,
    onSongClick: (CancionEntity) -> Unit,
    onToggleFavorite: ((CancionEntity) -> Unit)? = null,
    onAddToPlaylist: ((CancionEntity) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val backgroundColor = if (isCurrent) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.60f)
    }

    val borderColor = if (isCurrent) {
        ElectricCyan.copy(alpha = 0.45f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable {
                HapticFeedbackHelper.click(context)
                onSongClick(cancion)
            }
            .testTag("song_row_${cancion.id}"),
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                if (isCurrent) ElectricCyan.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant,
                                if (isCurrent) NeonViolet.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!cancion.albumArtUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = cancion.albumArtUri,
                        contentDescription = "Cover art",
                        modifier = Modifier.size(48.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = if (isCurrent && isPlaying) Icons.Default.GraphicEq else Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = if (isCurrent) ElectricCyan else TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = cancion.titulo,
                    color = if (isCurrent) ElectricCyan else TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${cancion.artista} • ${cancion.album}",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isCurrent && isPlaying) {
                    MiniEqualizerBars(
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }

                if (onToggleFavorite != null) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable {
                                HapticFeedbackHelper.click(context)
                                onToggleFavorite(cancion)
                            }
                            .testTag("fav_button_${cancion.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (cancion.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (cancion.isFavorite) "Quitar favorito" else "Añadir favorito",
                            tint = if (cancion.isFavorite) NeonViolet else TextSecondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                if (onAddToPlaylist != null) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable {
                                HapticFeedbackHelper.click(context)
                                onAddToPlaylist(cancion)
                            }
                            .testTag("playlist_add_${cancion.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlaylistAdd,
                            contentDescription = "Añadir a playlist",
                            tint = TextSecondary.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = formatDuration(cancion.duracion),
                    color = if (isCurrent) ElectricCyan.copy(alpha = 0.9f) else TextTertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Native Hardware Equalizer Preset Chips
 */
@Composable
fun EqualizerChips(
    currentPreset: String,
    onPresetSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val presets = listOf("Flat", "Rock", "Bass Boost", "Vocal", "Acoustic")
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        presets.forEach { preset ->
            val isSelected = currentPreset.equals(preset, ignoreCase = true)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) ElectricCyan.copy(alpha = 0.20f) else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .border(
                        1.dp,
                        if (isSelected) ElectricCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        HapticFeedbackHelper.click(context)
                        onPresetSelected(preset)
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("eq_preset_$preset"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = preset,
                    color = if (isSelected) ElectricCyan else TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Lightweight Speed & Pitch Control Panel
 */
@Composable
fun SpeedPitchControls(
    speed: Float,
    pitch: Float,
    onSpeedPitchChange: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val quickSpeeds = listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Velocidad: ${String.format(Locale.US, "%.2fx", speed)}",
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                quickSpeeds.forEach { qSpeed ->
                    val selected = kotlin.math.abs(speed - qSpeed) < 0.05f
                    Text(
                        text = "${qSpeed}x",
                        color = if (selected) ElectricCyan else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selected) ElectricCyan.copy(alpha = 0.2f) else Color.Transparent)
                            .clickable {
                                HapticFeedbackHelper.click(context)
                                onSpeedPitchChange(qSpeed, pitch)
                            }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Slider(
            value = speed,
            onValueChange = { onSpeedPitchChange(it, pitch) },
            valueRange = 0.5f..2.0f,
            colors = SliderDefaults.colors(
                thumbColor = ElectricCyan,
                activeTrackColor = ElectricCyan,
                inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            ),
            modifier = Modifier.height(28.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tono (Pitch): ${String.format(Locale.US, "%.2fx", pitch)}",
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Restablecer",
                color = NeonViolet,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable {
                        HapticFeedbackHelper.click(context)
                        onSpeedPitchChange(1.0f, 1.0f)
                    }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }

        Slider(
            value = pitch,
            onValueChange = { onSpeedPitchChange(speed, it) },
            valueRange = 0.5f..2.0f,
            colors = SliderDefaults.colors(
                thumbColor = NeonViolet,
                activeTrackColor = NeonViolet,
                inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            ),
            modifier = Modifier.height(28.dp)
        )
    }
}

/**
 * Lightweight micro animated equalizer bars
 */
@Composable
fun MiniEqualizerBars(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "eq")
    val h1 by transition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(400), RepeatMode.Reverse), label = "h1"
    )
    val h2 by transition.animateFloat(
        initialValue = 0.9f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(550), RepeatMode.Reverse), label = "h2"
    )
    val h3 by transition.animateFloat(
        initialValue = 0.4f, targetValue = 0.85f,
        animationSpec = infiniteRepeatable(tween(450), RepeatMode.Reverse), label = "h3"
    )

    Row(
        modifier = modifier.height(14.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height((14 * h1).dp)
                .clip(CircleShape)
                .background(ElectricCyan)
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height((14 * h2).dp)
                .clip(CircleShape)
                .background(NeonViolet)
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height((14 * h3).dp)
                .clip(CircleShape)
                .background(ElectricCyan)
        )
    }
}

/**
 * Stylized floating player card with hardware DSP, speed/pitch and trim handles
 */
@Composable
fun FloatingPlayerCard(
    cancion: CancionEntity?,
    isPlaying: Boolean,
    progressFraction: Float,
    currentPositionMs: Long,
    durationMs: Long,
    eqPreset: String,
    onPresetSelected: (String) -> Unit,
    speed: Float,
    pitch: Float,
    onSpeedPitchChange: (Float, Float) -> Unit,
    isTrimModeActive: Boolean,
    onToggleTrimMode: () -> Unit,
    trimStartFraction: Float,
    trimEndFraction: Float,
    onTrimRangeChange: (Float, Float) -> Unit,
    isTrimming: Boolean,
    onTrimConfirm: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    if (cancion == null) return

    val context = LocalContext.current
    val totalMs = if (durationMs > 0) durationMs else cancion.duracion
    var showEqPanel by remember { mutableStateOf(false) }
    var showSpeedPanel by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(
                        ElectricCyan.copy(alpha = 0.40f),
                        AccentGradientEnd.copy(alpha = 0.20f),
                        Color.White.copy(alpha = 0.10f)
                    )
                ),
                RoundedCornerShape(24.dp)
            )
            .testTag("floating_player_card"),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // Track header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (!cancion.albumArtUri.isNullOrEmpty()) {
                        AsyncImage(
                            model = cancion.albumArtUri,
                            contentDescription = "Playing track art",
                            modifier = Modifier.size(42.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cancion.titulo,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = cancion.artista,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Action toggles: EQ, Speed, Trim
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = {
                            HapticFeedbackHelper.click(context)
                            showEqPanel = !showEqPanel
                            showSpeedPanel = false
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Equalizer,
                            contentDescription = "Equalizer",
                            tint = if (showEqPanel) ElectricCyan else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            HapticFeedbackHelper.click(context)
                            showSpeedPanel = !showSpeedPanel
                            showEqPanel = false
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Speed & Pitch",
                            tint = if (showSpeedPanel || speed != 1.0f || pitch != 1.0f) ElectricCyan else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            HapticFeedbackHelper.doubleTick(context)
                            onToggleTrimMode()
                            showEqPanel = false
                            showSpeedPanel = false
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCut,
                            contentDescription = "Trim Audio",
                            tint = if (isTrimModeActive) NeonViolet else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Expandable EQ Panel
            AnimatedVisibility(visible = showEqPanel) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = "Ecualizador Hardware DSP",
                        color = ElectricCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    EqualizerChips(
                        currentPreset = eqPreset,
                        onPresetSelected = onPresetSelected
                    )
                }
            }

            // Expandable Speed/Pitch Panel
            AnimatedVisibility(visible = showSpeedPanel) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    SpeedPitchControls(
                        speed = speed,
                        pitch = pitch,
                        onSpeedPitchChange = onSpeedPitchChange
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Interactive Waveform Canvas
            val waveformArray = cancion.parsedWaveform()
            WaveformCanvas(
                waveform = waveformArray,
                progressFraction = progressFraction,
                onSeek = onSeek,
                isTrimModeActive = isTrimModeActive,
                trimStartFraction = trimStartFraction,
                trimEndFraction = trimEndFraction,
                onTrimRangeChange = onTrimRangeChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("waveform_canvas")
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Time Indicators or Trim Confirm Action
            if (isTrimModeActive) {
                val trimStartMs = (totalMs * trimStartFraction).toLong()
                val trimEndMs = (totalMs * trimEndFraction).toLong()
                val trimDurMs = (trimEndMs - trimStartMs).coerceAtLeast(0L)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Corte: ${formatDuration(trimStartMs)} - ${formatDuration(trimEndMs)} (${formatDuration(trimDurMs)})",
                        color = NeonViolet,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Brush.horizontalGradient(listOf(NeonViolet, ElectricCyan)))
                            .clickable(enabled = !isTrimming) {
                                HapticFeedbackHelper.heavyClick(context)
                                onTrimConfirm()
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isTrimming) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Guardar Tono",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatDuration(currentPositionMs),
                        color = ElectricCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "-${formatDuration((totalMs - currentPositionMs).coerceAtLeast(0L))}",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Playback Controls Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            HapticFeedbackHelper.click(context)
                            onSkipPrevious()
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .testTag("skip_prev_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous track",
                            tint = TextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(ElectricCyan, AccentGradientEnd))
                            )
                            .clickable {
                                HapticFeedbackHelper.heavyClick(context)
                                onTogglePlayPause()
                            }
                            .testTag("play_pause_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    IconButton(
                        onClick = {
                            HapticFeedbackHelper.click(context)
                            onSkipNext()
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .testTag("skip_next_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next track",
                            tint = TextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
