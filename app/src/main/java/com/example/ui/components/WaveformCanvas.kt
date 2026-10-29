package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AccentGradientEnd
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.WaveformInactive
import com.example.ui.util.HapticFeedbackHelper

/**
 * High-performance, zero-CPU interactive audio waveform rendered directly on Canvas.
 * Supports tactile haptic scrubbing (mechanical tick pattern on bar crossing) and click on seek.
 */
@Composable
fun WaveformCanvas(
    waveform: FloatArray,
    progressFraction: Float,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    isTrimModeActive: Boolean = false,
    trimStartFraction: Float = 0.1f,
    trimEndFraction: Float = 0.9f,
    onTrimRangeChange: (Float, Float) -> Unit = { _, _ -> },
    activeColorStart: Color = ElectricCyan,
    activeColorEnd: Color = AccentGradientEnd,
    inactiveColor: Color = WaveformInactive,
    visualizerMode: Int = 0 // 0: Neon Bars, 1: Smooth Spline Curve, 2: Minimalist Dots
) {
    val context = LocalContext.current
    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }
    var lastScrubbedBar by remember { mutableIntStateOf(-1) }

    val effectiveFraction = if (isDragging && !isTrimModeActive) dragFraction else progressFraction.coerceIn(0f, 1f)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .pointerInput(isTrimModeActive, trimStartFraction, trimEndFraction) {
                if (isTrimModeActive) {
                    detectTapGestures { offset ->
                        val tapFraction = (offset.x / size.width).coerceIn(0f, 1f)
                        val distStart = kotlin.math.abs(tapFraction - trimStartFraction)
                        val distEnd = kotlin.math.abs(tapFraction - trimEndFraction)
                        HapticFeedbackHelper.click(context)
                        if (distStart < distEnd) {
                            onTrimRangeChange(tapFraction, trimEndFraction)
                        } else {
                            onTrimRangeChange(trimStartFraction, tapFraction)
                        }
                    }
                } else {
                    detectTapGestures { offset ->
                        val frac = (offset.x / size.width).coerceIn(0f, 1f)
                        HapticFeedbackHelper.click(context)
                        onSeek(frac)
                    }
                }
            }
            .pointerInput(isTrimModeActive, trimStartFraction, trimEndFraction) {
                if (isTrimModeActive) {
                    var draggingHandle: Int = 0 // 1: start, 2: end
                    detectDragGestures(
                        onDragStart = { offset ->
                            val touchFrac = (offset.x / size.width).coerceIn(0f, 1f)
                            val distStart = kotlin.math.abs(touchFrac - trimStartFraction)
                            val distEnd = kotlin.math.abs(touchFrac - trimEndFraction)
                            draggingHandle = if (distStart < distEnd) 1 else 2
                            lastScrubbedBar = (touchFrac * 50).toInt().coerceIn(0, 49)
                            HapticFeedbackHelper.click(context)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val newFrac = (change.position.x / size.width).coerceIn(0f, 1f)
                            val currentBar = (newFrac * 50).toInt().coerceIn(0, 49)
                            if (currentBar != lastScrubbedBar) {
                                lastScrubbedBar = currentBar
                                HapticFeedbackHelper.tick(context)
                            }

                            if (draggingHandle == 1) {
                                onTrimRangeChange(newFrac, trimEndFraction)
                            } else {
                                onTrimRangeChange(trimStartFraction, newFrac)
                            }
                        }
                    )
                } else {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            dragFraction = (offset.x / size.width).coerceIn(0f, 1f)
                            lastScrubbedBar = (dragFraction * 50).toInt().coerceIn(0, 49)
                            HapticFeedbackHelper.click(context)
                        },
                        onDragEnd = {
                            isDragging = false
                            HapticFeedbackHelper.click(context)
                            onSeek(dragFraction)
                        },
                        onDragCancel = {
                            isDragging = false
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            dragFraction = (change.position.x / size.width).coerceIn(0f, 1f)
                            val currentBar = (dragFraction * 50).toInt().coerceIn(0, 49)
                            if (currentBar != lastScrubbedBar) {
                                lastScrubbedBar = currentBar
                                HapticFeedbackHelper.tick(context)
                            }
                        }
                    )
                }
            }
    ) {
        val totalBars = if (waveform.isNotEmpty()) waveform.size else 50
        val canvasWidth = size.width
        val canvasHeight = size.height

        val totalGapRatio = 0.40f
        val slotWidth = canvasWidth / totalBars
        val barWidth = slotWidth * (1f - totalGapRatio)
        val cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)

        val activeBrush = Brush.horizontalGradient(
            colors = listOf(activeColorStart, activeColorEnd),
            startX = 0f,
            endX = canvasWidth * effectiveFraction.coerceAtLeast(0.1f)
        )

        val trimBrush = Brush.horizontalGradient(
            colors = listOf(NeonViolet, ElectricCyan),
            startX = canvasWidth * trimStartFraction,
            endX = canvasWidth * trimEndFraction
        )

        if (visualizerMode == 2) {
            // Mode 2: Minimalist Dots
            for (i in 0 until totalBars) {
                val barFraction = (i + 0.5f) / totalBars
                val sample = if (i < waveform.size) waveform[i] else 0.35f
                val centerX = i * slotWidth + slotWidth / 2f
                val centerY = canvasHeight / 2f
                val dotRadius = (sample * 5.dp.toPx()).coerceAtLeast(2.dp.toPx())

                val isPlayed = !isTrimModeActive && barFraction <= effectiveFraction
                val inTrimRange = isTrimModeActive && barFraction in trimStartFraction..trimEndFraction

                val color = when {
                    isTrimModeActive && inTrimRange -> ElectricCyan
                    isTrimModeActive -> inactiveColor.copy(alpha = 0.15f)
                    isPlayed -> activeColorStart
                    else -> inactiveColor
                }

                drawCircle(
                    color = color,
                    radius = dotRadius,
                    center = Offset(centerX, centerY)
                )
            }
        } else if (visualizerMode == 1) {
            // Mode 1: Smooth Continuous Waveform
            for (i in 0 until totalBars) {
                val barFraction = (i + 0.5f) / totalBars
                val sample = if (i < waveform.size) waveform[i] else 0.35f
                val centerX = i * slotWidth + slotWidth / 2f
                val barHeight = (sample * canvasHeight * 0.80f).coerceAtLeast(3.dp.toPx())
                val topY = (canvasHeight - barHeight) / 2f

                val isPlayed = !isTrimModeActive && barFraction <= effectiveFraction
                val inTrimRange = isTrimModeActive && barFraction in trimStartFraction..trimEndFraction

                val color = when {
                    isTrimModeActive && inTrimRange -> NeonViolet
                    isTrimModeActive -> inactiveColor.copy(alpha = 0.15f)
                    isPlayed -> activeColorStart
                    else -> inactiveColor
                }

                drawLine(
                    color = color,
                    start = Offset(centerX, topY),
                    end = Offset(centerX, topY + barHeight),
                    strokeWidth = (slotWidth * 0.7f).coerceAtLeast(1.5.dp.toPx())
                )
            }
        } else {
            // Mode 0: Default Rounded Neon Bars
            for (i in 0 until totalBars) {
                val barFraction = (i + 0.5f) / totalBars

                val sample = if (i < waveform.size) waveform[i] else 0.35f
                val barHeight = (sample * canvasHeight * 0.85f).coerceAtLeast(4.dp.toPx())

                val left = i * slotWidth + (slotWidth - barWidth) / 2f
                val top = (canvasHeight - barHeight) / 2f

                if (isTrimModeActive) {
                    val inTrimRange = barFraction in trimStartFraction..trimEndFraction
                    if (inTrimRange) {
                        drawRoundRect(
                            brush = trimBrush,
                            topLeft = Offset(left, top),
                            size = Size(barWidth, barHeight),
                            cornerRadius = cornerRadius
                        )
                    } else {
                        drawRoundRect(
                            color = inactiveColor.copy(alpha = 0.15f),
                            topLeft = Offset(left, top),
                            size = Size(barWidth, barHeight),
                            cornerRadius = cornerRadius
                        )
                    }
                } else {
                    val isPlayed = barFraction <= effectiveFraction
                    if (isPlayed) {
                        drawRoundRect(
                            brush = activeBrush,
                            topLeft = Offset(left, top),
                            size = Size(barWidth, barHeight),
                            cornerRadius = cornerRadius
                        )
                    } else {
                        drawRoundRect(
                            color = inactiveColor,
                            topLeft = Offset(left, top),
                            size = Size(barWidth, barHeight),
                            cornerRadius = cornerRadius
                        )
                    }
                }
            }
        }

        if (isTrimModeActive) {
            val startX = (canvasWidth * trimStartFraction).coerceIn(0f, canvasWidth)
            drawLine(
                color = NeonViolet,
                start = Offset(startX, 0f),
                end = Offset(startX, canvasHeight),
                strokeWidth = 2.dp.toPx()
            )
            drawCircle(
                color = NeonViolet,
                radius = 5.dp.toPx(),
                center = Offset(startX, canvasHeight / 2f)
            )

            val endX = (canvasWidth * trimEndFraction).coerceIn(0f, canvasWidth)
            drawLine(
                color = ElectricCyan,
                start = Offset(endX, 0f),
                end = Offset(endX, canvasHeight),
                strokeWidth = 2.dp.toPx()
            )
            drawCircle(
                color = ElectricCyan,
                radius = 5.dp.toPx(),
                center = Offset(endX, canvasHeight / 2f)
            )
        } else {
            val headX = (canvasWidth * effectiveFraction).coerceIn(0f, canvasWidth)
            drawCircle(
                color = activeColorStart,
                radius = 3.5.dp.toPx(),
                center = Offset(headX, canvasHeight / 2f)
            )
        }
    }
}
