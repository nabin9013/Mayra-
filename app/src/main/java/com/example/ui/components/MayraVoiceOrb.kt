package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.service.AssistantMode
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MayraVoiceOrb(
    mode: AssistantMode,
    inputAmplitude: Float,
    outputAmplitude: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeEnergy = when (mode) {
        AssistantMode.SPEAKING -> maxOf(outputAmplitude, 0.25f)
        AssistantMode.LISTENING -> maxOf(inputAmplitude, 0.14f)
        AssistantMode.CONNECTING_LIVE,
        AssistantMode.EXECUTING_TOOL -> 0.35f
        AssistantMode.STANDBY_WAKE_WORD -> maxOf(inputAmplitude * 0.6f, 0.05f)
        AssistantMode.OFF, AssistantMode.ERROR -> 0.02f
    }

    val smoothEnergy by animateFloatAsState(
        targetValue = activeEnergy,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 280f),
        label = "orbEnergy"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "orbSpin")
    val rotationDeg by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "haloRotation"
    )

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val errorColor = MaterialTheme.colorScheme.error

    val ringColors = when (mode) {
        AssistantMode.SPEAKING -> listOf(primary, Color(0xFFFF4D94), secondary, primary)
        AssistantMode.LISTENING -> listOf(secondary, tertiary, primary, secondary)
        AssistantMode.STANDBY_WAKE_WORD -> listOf(tertiary.copy(alpha = 0.8f), primary.copy(alpha = 0.7f), tertiary.copy(alpha = 0.8f))
        AssistantMode.CONNECTING_LIVE,
        AssistantMode.EXECUTING_TOOL -> listOf(secondary, primary, secondary)
        AssistantMode.ERROR -> listOf(errorColor, primary, errorColor)
        AssistantMode.OFF -> listOf(Color.Gray.copy(alpha = 0.3f), Color.DarkGray.copy(alpha = 0.3f))
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(268.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val baseRadius = size.minDimension * 0.34f
                val pulseRadius = baseRadius + (smoothEnergy * size.minDimension * 0.11f)

                // Ambient glow behind avatar
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ringColors.first().copy(alpha = 0.32f + smoothEnergy * 0.25f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = size.minDimension * 0.50f
                    ),
                    center = center,
                    radius = size.minDimension * 0.50f
                )

                // Outer rotating gradient ring
                rotate(rotationDeg, pivot = center) {
                    drawCircle(
                        brush = Brush.sweepGradient(ringColors, center = center),
                        radius = pulseRadius,
                        center = center,
                        style = Stroke(width = 3.5.dp.toPx())
                    )
                }

                // Radial voice reactive spikes around the orb
                val barCount = 44
                val outerStart = pulseRadius + 8.dp.toPx()
                for (i in 0 until barCount) {
                    val angle = (i.toFloat() / barCount) * (2f * PI.toFloat())
                    val harmonic = 0.5f + 0.5f * sin(wavePhase + i * 0.45f)
                    val barLen = (4.dp.toPx() + smoothEnergy * 24.dp.toPx() * harmonic)
                    val start = Offset(
                        x = center.x + cos(angle) * outerStart,
                        y = center.y + sin(angle) * outerStart
                    )
                    val end = Offset(
                        x = center.x + cos(angle) * (outerStart + barLen),
                        y = center.y + sin(angle) * (outerStart + barLen)
                    )
                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(secondary, primary),
                            start = start,
                            end = end
                        ),
                        start = start,
                        end = end,
                        strokeWidth = 2.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            val avatarScale = 1f + (smoothEnergy * 0.06f)
            val interactionSource = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .size(176.dp)
                    .scale(avatarScale)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(ringColors),
                        shape = CircleShape
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = ripple(bounded = true, color = primary),
                        role = Role.Button,
                        onClick = onClick
                    )
                    .testTag("voice_orb_button"),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_mayra_avatar_1794683237081),
                    contentDescription = stringResource(id = R.string.cd_mayra_avatar),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Real-time horizontal dual-tone equalizer bar
        AudioWaveformStrip(
            energy = smoothEnergy,
            phase = wavePhase,
            active = mode != AssistantMode.OFF,
            primaryColor = primary,
            secondaryColor = secondary,
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .height(44.dp)
        )
    }
}

@Composable
private fun AudioWaveformStrip(
    energy: Float,
    phase: Float,
    active: Boolean,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val bars = 27
        val gap = size.width / (bars * 1.85f)
        val barWidth = (size.width - gap * (bars - 1)) / bars
        val centerY = size.height / 2f
        val maxHalfHeight = size.height * 0.45f

        for (i in 0 until bars) {
            val distanceFromMid = kotlin.math.abs(i - (bars - 1) / 2f) / ((bars - 1) / 2f)
            val envelope = 1f - (distanceFromMid * 0.68f)
            val osc = if (active) {
                0.35f + 0.65f * kotlin.math.abs(sin(phase * 1.4f + i * 0.5f))
            } else {
                0.15f
            }
            val halfH = (3.dp.toPx() + maxHalfHeight * envelope * energy * osc)
                .coerceAtMost(maxHalfHeight)
            val x = i * (barWidth + gap)

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(secondaryColor, primaryColor, secondaryColor)
                ),
                topLeft = Offset(x, centerY - halfH),
                size = Size(barWidth, halfH * 2f),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
                alpha = if (active) 0.92f else 0.35f
            )
        }
    }
}
