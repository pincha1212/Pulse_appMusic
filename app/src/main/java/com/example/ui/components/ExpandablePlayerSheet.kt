package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.example.data.CancionEntity
import com.example.ui.theme.AccentGradientEnd
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.util.HapticFeedbackHelper

/**
 * Mini Player Bar (Compact State) shown when player is collapsed.
 * Enriched with low-latency tactile haptic feedback.
 */
@Composable
fun MiniPlayerBar(
    cancion: CancionEntity,
    isPlaying: Boolean,
    progressFraction: Float,
    waveformArray: FloatArray,
    onTogglePlayPause: () -> Unit,
    onExpandPlayer: () -> Unit,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(
                1.dp,
                Brush.horizontalGradient(listOf(ElectricCyan.copy(alpha = 0.35f), NeonViolet.copy(alpha = 0.20f))),
                RoundedCornerShape(20.dp)
            )
            .clickable {
                HapticFeedbackHelper.click(context)
                onExpandPlayer()
            }
            .testTag("mini_player_bar"),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mini Cover
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (!cancion.albumArtUri.isNullOrEmpty()) {
                        AsyncImage(
                            model = cancion.albumArtUri,
                            contentDescription = "Cover",
                            modifier = Modifier.size(38.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cancion.titulo,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = cancion.artista,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Play / Pause Button with heavy haptic click
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(ElectricCyan, AccentGradientEnd)))
                        .clickable {
                            HapticFeedbackHelper.heavyClick(context)
                            onTogglePlayPause()
                        }
                        .testTag("mini_play_pause"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            WaveformCanvas(
                waveform = waveformArray,
                progressFraction = progressFraction,
                onSeek = onSeek,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
            )
        }
    }
}

/**
 * Full Player View Sheet (Expanded State) with 200x200 album art, full controls,
 * repeat, shuffle, sleep timer and audio trimming with distinct vibration patterns.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandedPlayerSheet(
    sheetState: SheetState,
    cancion: CancionEntity,
    isPlaying: Boolean,
    progressFraction: Float,
    currentPositionMs: Long,
    durationMs: Long,
    isShuffleEnabled: Boolean,
    onToggleShuffle: () -> Unit,
    repeatMode: Int,
    onToggleRepeatMode: () -> Unit,
    sleepTimerOption: Int,
    sleepTimerRemainingSec: Long,
    onSetSleepTimer: (Int) -> Unit,
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
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    showLyricsView: Boolean = false,
    onToggleLyricsView: () -> Unit = {},
    onSaveLyrics: ((String) -> Unit)? = null,
    visualizerMode: Int = 0,
    onToggleVisualizerMode: () -> Unit = {},
    onOpenQueue: () -> Unit = {},
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    var showEditLyricsDialog by remember { mutableStateOf(false) }
    var lyricsDraft by remember(cancion.id, cancion.lyrics) { mutableStateOf(cancion.lyrics ?: "") }

    val playScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.05f else 1.0f,
        animationSpec = spring(stiffness = 300f),
        label = "playScale"
    )

    if (showEditLyricsDialog) {
        AlertDialog(
            onDismissRequest = { showEditLyricsDialog = false },
            title = {
                Text(
                    text = "Letras y Notas de la Canción",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Introduce la letra o notas para ${cancion.titulo}:",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = lyricsDraft,
                        onValueChange = { lyricsDraft = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        placeholder = {
                            Text("Escribe las estrofas o notas aquí…", color = TextSecondary)
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        HapticFeedbackHelper.click(context)
                        onSaveLyrics?.invoke(lyricsDraft)
                        showEditLyricsDialog = false
                    }
                ) {
                    Text("Guardar", color = ElectricCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditLyricsDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = TextPrimary,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .testTag("expanded_player_sheet"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Visualizer Mode Toggle, Collapse Handle, Queue Open Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Visualizer Mode Switch (Waveform vs Neon Bars)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (visualizerMode == 1) ElectricCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, if (visualizerMode == 1) ElectricCyan else Color.Transparent, RoundedCornerShape(10.dp))
                        .clickable {
                            HapticFeedbackHelper.click(context)
                            onToggleVisualizerMode()
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Modo Visualizador",
                            tint = if (visualizerMode == 1) ElectricCyan else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (visualizerMode == 1) "Barras" else "Onda",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (visualizerMode == 1) ElectricCyan else TextSecondary
                        )
                    }
                }

                // Collapse handle
                IconButton(
                    onClick = {
                        HapticFeedbackHelper.click(context)
                        onDismissRequest()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse Player",
                        tint = TextSecondary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Queue open button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            HapticFeedbackHelper.click(context)
                            onOpenQueue()
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.QueueMusic,
                            contentDescription = "Cola de reproducción",
                            tint = ElectricCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Cola",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 200x200 Album Cover
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        2.dp,
                        Brush.linearGradient(listOf(ElectricCyan.copy(alpha = 0.5f), NeonViolet.copy(alpha = 0.3f))),
                        RoundedCornerShape(24.dp)
                    )
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (!cancion.albumArtUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = cancion.albumArtUri,
                        contentDescription = "Album Cover",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(72.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Track Title, Artist, Lyrics & Favorite Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cancion.titulo,
                        color = TextPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${cancion.artista} • ${cancion.album}",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            HapticFeedbackHelper.click(context)
                            onToggleLyricsView()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (showLyricsView) Icons.Default.Audiotrack else Icons.Default.Notes,
                            contentDescription = "Letras",
                            tint = if (showLyricsView) ElectricCyan else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            HapticFeedbackHelper.click(context)
                            onToggleFavorite()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (isFavorite) NeonViolet else TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interactive Waveform Canvas or Lyrics/Notes Panel
            if (showLyricsView) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    val lyricsScroll = rememberScrollState()
                    val lyricsText = cancion.lyrics?.ifBlank { null }
                    if (lyricsText != null) {
                        Text(
                            text = lyricsText,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(lyricsScroll)
                        )
                    } else {
                        Text(
                            text = "No hay letra guardada. Toca el lápiz para añadirla.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                            .clickable {
                                HapticFeedbackHelper.click(context)
                                showEditLyricsDialog = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar letra",
                            tint = ElectricCyan,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            } else if (visualizerMode == 1) {
                NeonFrequencyVisualizer(
                    waveform = cancion.parsedWaveform(),
                    progressFraction = progressFraction,
                    isPlaying = isPlaying,
                    onSeek = onSeek,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .testTag("neon_frequency_visualizer")
                )
            } else {
                WaveformCanvas(
                    waveform = cancion.parsedWaveform(),
                    progressFraction = progressFraction,
                    onSeek = onSeek,
                    isTrimModeActive = isTrimModeActive,
                    trimStartFraction = trimStartFraction,
                    trimEndFraction = trimEndFraction,
                    onTrimRangeChange = onTrimRangeChange,
                    visualizerMode = visualizerMode,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expanded_waveform")
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Time and Trim Action
            if (isTrimModeActive) {
                val totalMs = if (durationMs > 0) durationMs else cancion.duracion
                val startMs = (totalMs * trimStartFraction).toLong()
                val endMs = (totalMs * trimEndFraction).toLong()
                val trimDurMs = (endMs - startMs).coerceAtLeast(0L)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cortar: ${formatDuration(startMs)} - ${formatDuration(endMs)} (${formatDuration(trimDurMs)})",
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
                        text = "-${formatDuration(((if (durationMs > 0) durationMs else cancion.duracion) - currentPositionMs).coerceAtLeast(0L))}",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Full Playback Controls Row with Haptics: Shuffle, Prev, Play/Pause, Next, Repeat
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle Button
                IconButton(onClick = {
                    HapticFeedbackHelper.doubleTick(context)
                    onToggleShuffle()
                }) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffleEnabled) ElectricCyan else TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Previous Track
                IconButton(onClick = {
                    HapticFeedbackHelper.click(context)
                    onSkipPrevious()
                }) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = TextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Play / Pause Button with Punchy Heavy Haptic Click
                Box(
                    modifier = Modifier
                        .scale(playScale)
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(ElectricCyan, AccentGradientEnd)))
                        .clickable {
                            HapticFeedbackHelper.heavyClick(context)
                            onTogglePlayPause()
                        }
                        .testTag("expanded_play_pause"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Next Track
                IconButton(onClick = {
                    HapticFeedbackHelper.click(context)
                    onSkipNext()
                }) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = TextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Repeat Mode Button
                IconButton(onClick = {
                    HapticFeedbackHelper.doubleTick(context)
                    onToggleRepeatMode()
                }) {
                    Icon(
                        imageVector = if (repeatMode == Player.REPEAT_MODE_ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                        contentDescription = "Repeat",
                        tint = if (repeatMode != Player.REPEAT_MODE_OFF) ElectricCyan else TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Quick Actions: Audio Trimmer Toggle & Sleep Timer Selector with Haptics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Trim Toggle Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isTrimModeActive) NeonViolet.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, if (isTrimModeActive) NeonViolet else Color.Transparent, RoundedCornerShape(12.dp))
                        .clickable {
                            HapticFeedbackHelper.doubleTick(context)
                            onToggleTrimMode()
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ContentCut,
                            contentDescription = null,
                            tint = if (isTrimModeActive) NeonViolet else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isTrimModeActive) "Modo Corte Activo" else "Cortar Audio",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isTrimModeActive) NeonViolet else TextSecondary
                        )
                    }
                }

                // Sleep Timer Quick Chips
                SleepTimerSelector(
                    currentOption = sleepTimerOption,
                    remainingSec = sleepTimerRemainingSec,
                    onOptionSelected = onSetSleepTimer
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Sleep Timer Quick Selector Chips with Haptic Click Feedback
 */
@Composable
fun SleepTimerSelector(
    currentOption: Int,
    remainingSec: Long,
    onOptionSelected: (Int) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier.horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Timer,
            contentDescription = null,
            tint = if (currentOption != 0) ElectricCyan else TextSecondary,
            modifier = Modifier.size(16.dp)
        )

        listOf(0 to "Off", 15 to "15m", 30 to "30m", 60 to "60m", -1 to "Fin").forEach { (option, label) ->
            val isSelected = currentOption == option
            val textLabel = if (isSelected && option > 0 && remainingSec > 0) {
                "${remainingSec / 60}m"
            } else {
                label
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) ElectricCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, if (isSelected) ElectricCyan else Color.Transparent, RoundedCornerShape(10.dp))
                    .clickable {
                        HapticFeedbackHelper.click(context)
                        onOptionSelected(option)
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = textLabel,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) ElectricCyan else TextSecondary
                )
            }
        }
    }
}

/**
 * Real-time Neon Frequency Spectrum Bars Visualizer (v3.0.0)
 * Zero CPU software DSP: Renders purely on GPU via Compose Canvas with smooth harmonic oscillation
 * and interactive scrubbing support.
 */
@Composable
fun NeonFrequencyVisualizer(
    waveform: FloatArray,
    progressFraction: Float,
    isPlaying: Boolean,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val infiniteTransition = rememberInfiniteTransition(label = "neonVisualizer")
    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isPlaying) (2f * Math.PI.toFloat()) else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulsePhase"
    )

    val unplayedColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    HapticFeedbackHelper.click(context)
                    val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                    onSeek(fraction)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    HapticFeedbackHelper.tick(context)
                    val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                    onSeek(fraction)
                }
            }
    ) {
        val barCount = 32
        val totalWidth = size.width
        val totalHeight = size.height
        val barSpacing = 4.dp.toPx()
        val barWidth = ((totalWidth - (barSpacing * (barCount - 1))) / barCount).coerceAtLeast(2f)

        for (i in 0 until barCount) {
            val progressAtBar = i.toFloat() / barCount
            val isPlayed = progressAtBar <= progressFraction

            val sampleIdx = ((i.toFloat() / barCount) * (waveform.size - 1)).toInt().coerceIn(0, waveform.size - 1)
            val baseAmp = waveform.getOrElse(sampleIdx) { 0.4f }

            val dynamicBoost = if (isPlaying) {
                (kotlin.math.sin(pulsePhase + i * 0.45f) * 0.25f)
            } else 0f

            val clampedAmp = (baseAmp + dynamicBoost).coerceIn(0.12f, 0.95f)
            val barHeight = (totalHeight * clampedAmp).coerceAtLeast(6.dp.toPx())
            val left = i * (barWidth + barSpacing)
            val top = totalHeight - barHeight

            val color = if (isPlayed) {
                if (i % 2 == 0) ElectricCyan else NeonViolet
            } else {
                unplayedColor
            }

            drawRoundRect(
                color = color,
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
        }
    }
}

