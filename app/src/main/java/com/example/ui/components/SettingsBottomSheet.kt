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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
    isScanning: Boolean,
    onScanStorage: () -> Unit,
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
            Text(
                text = "Ajustes y Configuración",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 1. AMOLED Black / Dynamic Dark Toggle
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

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Hardware DSP Equalizer Section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Equalizer,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ecualizador Hardware DSP",
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

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Hardware Loudness Enhancer (DSP Preamp / Volume Boost)
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

            Spacer(modifier = Modifier.height(18.dp))

            // 3b. Bass Boost Ultra (DSP por hardware)
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
                            imageVector = Icons.Default.GraphicEq,
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

            Spacer(modifier = Modifier.height(18.dp))

            // 3c. Virtualizador 3D Espacial (DSP por hardware)
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

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Playback Speed & Pitch
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

            Spacer(modifier = Modifier.height(18.dp))

            // 5. Audio Crossfade (Fundido Suave)
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

            Spacer(modifier = Modifier.height(18.dp))

            // 6. Library Scan and Room Maintenance
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
                            text = "Reindexa MediaStore y calcula formas de onda",
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

            Spacer(modifier = Modifier.height(18.dp))

            // 7. System Audit & Version Information v2.0.0
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
                        text = "Pulse Music",
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
                            text = "v3.0.0",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "• Versión de compilación: 3.0.0 (Build 3)\n• Motor de audio: Jetpack Media3 (ExoPlayer) con Cola Dinámica\n• DSP Nativo por Hardware: 5 Bandas EQ + Loudness Preamp (+10dB) + Bass Boost Ultra + Virtualizador 3D Espacial\n• Almacenamiento: Room v3 (Estadísticas Inteligentes, Playlists, Historial, Favoritos, Letras)\n• Visualizador: Espectro de Frecuencias de Neón en tiempo real\n• Háptica: Perfiles táctiles de latencia ultrabaja\n• Consumo activo: < 60 MB RAM (1 GB hardware optimizado)",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
