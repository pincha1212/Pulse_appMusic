package com.example.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.util.HapticFeedbackHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    sheetState: SheetState,
    isAmoledMode: Boolean,
    onToggleAmoled: () -> Unit,
    eqPreset: String,
    onEqPresetSelected: (String) -> Unit,
    speed: Float,
    pitch: Float,
    onSpeedPitchChange: (Float, Float) -> Unit,
    loudnessGainMb: Int,
    onLoudnessGainChange: (Int) -> Unit,
    bassBoostStrength: Int,
    onBassBoostChange: (Int) -> Unit,
    virtualizerStrength: Int,
    onVirtualizerChange: (Int) -> Unit,
    crossfadeDurationSec: Int,
    onCrossfadeDurationChange: (Int) -> Unit,
    sleepTimerOption: Int = 0,
    sleepTimerRemainingSec: Long = 0L,
    onSetSleepTimer: (Int) -> Unit = {},
    isScanning: Boolean,
    onScanStorage: () -> Unit,
    githubRepoPath: String = "pincha1212/Pulse_appMusic",
    onGithubRepoPathChange: (String) -> Unit = {},
    isCheckingOtaUpdate: Boolean = false,
    otaUpdateInfo: com.example.ui.util.OtaUpdateInfo? = null,
    otaStatusMessage: String? = null,
    onCheckOtaUpdate: () -> Unit = {},
    onTriggerOtaInstall: () -> Unit = {},
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val sheetScrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = TextPrimary,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(sheetScrollState)
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .testTag("settings_bottom_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Ajustes y Configuración",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Personalización, motor DSP y sistema",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ElectricCyan.copy(alpha = 0.15f))
                        .border(1.dp, ElectricCyan.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "v4.2.1",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ==========================================
            // SECCIÓN 1: APARIENCIA Y ENERGÍA
            // ==========================================
            Text(
                text = "APARIENCIA Y ENERGÍA",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp,
                color = ElectricCyan
            )

            Spacer(modifier = Modifier.height(8.dp))

            // AMOLED Black / Dynamic Dark Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .clickable {
                        HapticFeedbackHelper.doubleTick(context)
                        onToggleAmoled()
                    }
                    .padding(14.dp)
                    .testTag("amoled_setting_toggle"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isAmoledMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Modo AMOLED Black",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (isAmoledMode) "Negro puro #000000 (Ahorro de batería)" else "Tema oscuro ambiental adaptativo",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isAmoledMode) ElectricCyan else Color.Transparent)
                        .border(
                            1.dp,
                            if (isAmoledMode) ElectricCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isAmoledMode) "ACTIVO" else "DESACTIVO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isAmoledMode) Color.Black else TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // ==========================================
            // SECCIÓN 2: PROCESAMIENTO DE AUDIO DSP (HARDWARE)
            // ==========================================
            Text(
                text = "PROCESAMIENTO DE AUDIO DSP",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp,
                color = ElectricCyan
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Ecualizador Hardware DSP
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Equalizer,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ecualizador de 5 Bandas",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                EqualizerChips(
                    currentPreset = eqPreset,
                    onPresetSelected = onEqPresetSelected
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Hardware Loudness Enhancer (DSP Preamp / Volume Boost)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = NeonViolet,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Realce DSP (Loudness Boost)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = if (loudnessGainMb > 0) "+${loudnessGainMb / 100} dB" else "Off",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (loudnessGainMb > 0) NeonViolet else TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0 to "0 dB", 300 to "+3 dB", 600 to "+6 dB", 1000 to "+10 dB").forEach { (gain, label) ->
                        val isSelected = loudnessGainMb == gain
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) NeonViolet.copy(alpha = 0.25f) else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (isSelected) NeonViolet else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    HapticFeedbackHelper.click(context)
                                    onLoudnessGainChange(gain)
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) NeonViolet else TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bass Boost Ultra (DSP por hardware)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Equalizer,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Bass Boost Ultra (DSP)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = if (bassBoostStrength == 0) "Desactivado" else "${bassBoostStrength / 10}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (bassBoostStrength > 0) ElectricCyan else TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Slider(
                    value = bassBoostStrength.toFloat(),
                    onValueChange = {
                        onBassBoostChange(it.toInt())
                    },
                    valueRange = 0f..1000f,
                    colors = SliderDefaults.colors(
                        thumbColor = ElectricCyan,
                        activeTrackColor = ElectricCyan,
                        inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier.testTag("bass_boost_slider")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val presets = listOf(0 to "Off", 400 to "Medio", 800 to "Fuerte", 1000 to "Ultra")
                    presets.forEach { (strength, label) ->
                        val isSelected = (bassBoostStrength == strength) || (strength == 400 && bassBoostStrength in 300..500) || (strength == 800 && bassBoostStrength in 700..900)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) ElectricCyan.copy(alpha = 0.25f) else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (isSelected) ElectricCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    HapticFeedbackHelper.click(context)
                                    onBassBoostChange(strength)
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) ElectricCyan else TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Virtualizador 3D Espacial (DSP por hardware)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = NeonViolet,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Virtualizador 3D Espacial",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = if (virtualizerStrength == 0) "Desactivado" else "${virtualizerStrength / 10}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (virtualizerStrength > 0) NeonViolet else TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Slider(
                    value = virtualizerStrength.toFloat(),
                    onValueChange = {
                        onVirtualizerChange(it.toInt())
                    },
                    valueRange = 0f..1000f,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonViolet,
                        activeTrackColor = NeonViolet,
                        inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier.testTag("virtualizer_slider")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val presets = listOf(0 to "Off", 400 to "Estudio", 750 to "3D Amplio", 1000 to "Envolvente")
                    presets.forEach { (strength, label) ->
                        val isSelected = (virtualizerStrength == strength) || (strength == 400 && virtualizerStrength in 300..500) || (strength == 750 && virtualizerStrength in 650..850)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) NeonViolet.copy(alpha = 0.25f) else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (isSelected) NeonViolet else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    HapticFeedbackHelper.click(context)
                                    onVirtualizerChange(strength)
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) NeonViolet else TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // ==========================================
            // SECCIÓN 3: REPRODUCCIÓN Y BIBLIOTECA
            // ==========================================
            Text(
                text = "REPRODUCCIÓN Y BIBLIOTECA",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp,
                color = ElectricCyan
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Temporizador de Apagado (Sleep Timer)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Temporizador de apagado",
                            tint = ElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Temporizador de Apagado",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    if (sleepTimerOption != 0) {
                        val countdownBadge = if (sleepTimerOption == -1) {
                            "Fin de pista"
                        } else {
                            val mins = sleepTimerRemainingSec / 60
                            val secs = sleepTimerRemainingSec % 60
                            String.format("%02d:%02d", mins, secs)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ElectricCyan.copy(alpha = 0.2f))
                                .border(1.dp, ElectricCyan, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = countdownBadge,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = listOf(
                        0 to "Off",
                        15 to "15m",
                        30 to "30m",
                        45 to "45m",
                        60 to "60m",
                        -1 to "Fin"
                    )
                    presets.forEach { (mins, label) ->
                        val isSelected = sleepTimerOption == mins
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) ElectricCyan.copy(alpha = 0.25f) else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (isSelected) ElectricCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    HapticFeedbackHelper.click(context)
                                    onSetSleepTimer(mins)
                                }
                                .padding(vertical = 7.dp)
                                .testTag("sleep_timer_chip_$mins"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) ElectricCyan else TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Playback Speed & Pitch Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Velocidad y Tono (Media3)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                SpeedPitchControls(
                    speed = speed,
                    pitch = pitch,
                    onSpeedPitchChange = onSpeedPitchChange
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Audio Crossfade (Fundido Suave)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Fundido Suave (Crossfade)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = if (crossfadeDurationSec > 0) "${crossfadeDurationSec}s" else "Off",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0 to "Off", 2 to "2s", 4 to "4s", 6 to "6s").forEach { (secs, label) ->
                        val isSelected = crossfadeDurationSec == secs
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) ElectricCyan.copy(alpha = 0.25f) else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (isSelected) ElectricCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    HapticFeedbackHelper.click(context)
                                    onCrossfadeDurationChange(secs)
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) ElectricCyan else TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Library Scan and Room Maintenance
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .clickable(enabled = !isScanning) {
                        HapticFeedbackHelper.click(context)
                        onScanStorage()
                    }
                    .padding(14.dp)
                    .testTag("rescan_storage_setting"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Escanear Biblioteca Local",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Reindexa pistas y calcula formas de onda",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                if (isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = ElectricCyan,
                        strokeWidth = 2.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // ==========================================
            // SECCIÓN 4: ACTUALIZACIONES DEL SISTEMA
            // ==========================================
            Text(
                text = "ACTUALIZACIONES DEL SISTEMA",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp,
                color = ElectricCyan
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Actualización de Software Inalámbrica OTA
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(
                        1.dp,
                        ElectricCyan.copy(alpha = 0.3f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(14.dp)
                    .testTag("ota_update_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Actualización Inalámbrica",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        HapticFeedbackHelper.click(context)
                        onCheckOtaUpdate()
                    },
                    enabled = !isCheckingOtaUpdate,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isCheckingOtaUpdate) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.Black,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Buscando actualización...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Buscar actualización", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (otaUpdateInfo != null && otaUpdateInfo.isUpdateAvailable) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            HapticFeedbackHelper.heavyClick(context)
                            onTriggerOtaInstall()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonViolet, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Instalar versión ${otaUpdateInfo.tagName}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (!otaStatusMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = otaStatusMessage,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (otaUpdateInfo?.isUpdateAvailable == true) ElectricCyan else TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Información y Auditoría del Sistema
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(14.dp)
                    .testTag("version_audit_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pulse Music Player",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ElectricCyan.copy(alpha = 0.2f))
                            .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "v4.2.1",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "• Versión de compilación: 4.2.1 (Build 7)\n• Nueva Estructura Modular de Ajustes: Apariencia, Audio DSP, Biblioteca y Sistema\n• Panel Interactivo de Cola de Reproducción (Playback Queue Sheet)\n• Gestión Dinámica de Cola: Reordenar, Eliminar y Vaciar Cola\n• Motor de Audio: Jetpack Media3 (ExoPlayer) + Normalización Inteligente de Volumen\n• DSP Nativo por Hardware: 5 Bandas EQ Auto-por-Género + Loudness Boost (+10dB) + Bass Boost Ultra + Virtualizador 3D Espacial\n• Visualización de Audio: 3 Modos de Forma de Onda (Barras de Neón, Onda Continua y Puntos Minimalistas)\n• Editor de Etiquetas: Edición directa de Metadatos y Letras en tiempo real\n• Consumo activo: < 60 MB RAM (1 GB hardware optimizado)",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
