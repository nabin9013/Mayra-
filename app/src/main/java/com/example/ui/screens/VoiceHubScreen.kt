package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import com.example.R
import com.example.service.AssistantMode
import com.example.ui.components.CosmicGlassCard
import com.example.ui.components.MayraVoiceOrb
import com.example.viewmodel.MayraUiState
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun VoiceHubScreen(
    uiState: MayraUiState,
    hasMicPermission: Boolean,
    hasAllDevicePermissions: Boolean,
    onRequestPermissions: () -> Unit,
    onToggleAssistant: (Boolean) -> Unit,
    onOrbPrimaryAction: () -> Unit,
    onStopLiveSession: () -> Unit,
    onInterruptPlayback: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val runtime = uiState.runtime
    val isLive = runtime.mode == AssistantMode.CONNECTING_LIVE ||
        runtime.mode == AssistantMode.LISTENING ||
        runtime.mode == AssistantMode.SPEAKING ||
        runtime.mode == AssistantMode.EXECUTING_TOOL

    AnimatedContent(
        targetState = isLive,
        transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(180)) },
        label = "voiceModeTransition",
        modifier = modifier.fillMaxSize()
    ) { liveActive ->
        if (liveActive) {
            ActiveLiveVoiceView(
                uiState = uiState,
                onInterruptPlayback = onInterruptPlayback,
                onStopLiveSession = onStopLiveSession
            )
        } else {
            StandbyVoiceHomeView(
                uiState = uiState,
                hasMicPermission = hasMicPermission,
                hasAllDevicePermissions = hasAllDevicePermissions,
                onRequestPermissions = onRequestPermissions,
                onToggleAssistant = onToggleAssistant,
                onStartLive = onOrbPrimaryAction,
                onOpenSettings = onOpenSettings,
                onOpenAbout = onOpenAbout
            )
        }
    }
}

@Composable
private fun StandbyVoiceHomeView(
    uiState: MayraUiState,
    hasMicPermission: Boolean,
    hasAllDevicePermissions: Boolean,
    onRequestPermissions: () -> Unit,
    onToggleAssistant: (Boolean) -> Unit,
    onStartLive: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit
) {
    val runtime = uiState.runtime
    val settings = uiState.settings

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.widthIn(max = 560.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar matching uploaded layout (Menu on left, Settings on right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onOpenAbout,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("open_menu_about_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Menu,
                        contentDescription = stringResource(id = R.string.nav_about),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("open_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = stringResource(id = R.string.nav_settings),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Hero Avatar Orb + Title
            MayraVoiceOrb(
                mode = runtime.mode,
                inputAmplitude = runtime.inputAmplitude,
                outputAmplitude = runtime.outputAmplitude,
                onClick = {
                    if (!hasMicPermission) onRequestPermissions() else onStartLive()
                }
            )

            Text(
                text = "Mayra",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Voice Assistant",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Green-accented "Assistant is ON" switch card matching uploaded photo
            CosmicGlassCard(
                highlighted = settings.assistantEnabled,
                modifier = Modifier.fillMaxWidth(0.86f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(
                                    color = if (settings.assistantEnabled && hasMicPermission) {
                                        MaterialTheme.colorScheme.tertiary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                    },
                                    shape = CircleShape
                                )
                        )
                        Text(
                            text = if (settings.assistantEnabled) {
                                "Assistant is ON"
                            } else {
                                "Assistant is OFF"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Switch(
                        checked = settings.assistantEnabled,
                        onCheckedChange = { checked ->
                            if (checked && !hasMicPermission) {
                                onRequestPermissions()
                            } else {
                                onToggleAssistant(checked)
                            }
                        },
                        modifier = Modifier.testTag("assistant_master_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onTertiary,
                            checkedTrackColor = MaterialTheme.colorScheme.tertiary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Glowing Circular Mic Button overlaid on Neon Waveform (Photo Screen 1)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(94.dp),
                contentAlignment = Alignment.Center
            ) {
                HorizontalNeonSpectrum(
                    energy = maxOf(runtime.inputAmplitude, 0.14f),
                    active = settings.assistantEnabled,
                    primary = MaterialTheme.colorScheme.primary,
                    secondary = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                )

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    border = BorderStroke(
                        2.5.dp,
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.secondary,
                                MaterialTheme.colorScheme.primary
                            )
                        )
                    ),
                    shadowElevation = 10.dp,
                    modifier = Modifier
                        .size(74.dp)
                        .minimumInteractiveComponentSize()
                        .clip(CircleShape)
                        .clickable {
                            if (!hasMicPermission) onRequestPermissions() else onStartLive()
                        }
                        .testTag("mic_primary_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Mic,
                            contentDescription = stringResource(id = R.string.cd_mic_toggle),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (!hasMicPermission) "Tap to Grant Mic Access" else "Tap to Speak",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "or",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (settings.wakeWordEnabled) {
                    "Say \"Hey Mayra\""
                } else {
                    "Wake-word paused in Settings"
                },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Last tool action pill (non-chat hardware execution badge)
            AnimatedVisibility(visible = runtime.lastToolEvent != null) {
                runtime.lastToolEvent?.let { event ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                        border = BorderStroke(
                            1.dp,
                            if (event.success) {
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.55f)
                            } else {
                                MaterialTheme.colorScheme.error.copy(alpha = 0.55f)
                            }
                        ),
                        modifier = Modifier.padding(bottom = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (event.success) {
                                    Icons.Rounded.CheckCircle
                                } else {
                                    Icons.Rounded.ErrorOutline
                                },
                                contentDescription = null,
                                tint = if (event.success) {
                                    MaterialTheme.colorScheme.tertiary
                                } else {
                                    MaterialTheme.colorScheme.error
                                },
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = event.summary,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Transparent error surface
            AnimatedVisibility(visible = runtime.errorMessage != null) {
                runtime.errorMessage?.let { errorText ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = errorText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            if (!hasMicPermission || !hasAllDevicePermissions) {
                CosmicGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.VerifiedUser,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = if (!hasMicPermission) {
                                "Enable Mic for \"Hey Mayra\" & Live Voice"
                            } else {
                                "Grant Contacts & Phone for calls/WhatsApp"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedButton(
                            onClick = onRequestPermissions,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("grant_permissions_button")
                        ) {
                            Text("Grant")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveLiveVoiceView(
    uiState: MayraUiState,
    onInterruptPlayback: () -> Unit,
    onStopLiveSession: () -> Unit
) {
    val runtime = uiState.runtime
    val energy = when (runtime.mode) {
        AssistantMode.SPEAKING -> maxOf(runtime.outputAmplitude, 0.35f)
        AssistantMode.LISTENING -> maxOf(runtime.inputAmplitude, 0.22f)
        AssistantMode.CONNECTING_LIVE,
        AssistantMode.EXECUTING_TOOL -> 0.30f
        else -> 0.1f
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 560.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top bar matching Photo Screen 2 (Avatar + Mayra + Online + Speaker/Interrupt)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(
                        onClick = onStopLiveSession,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("live_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(id = R.string.cd_back_button)
                        )
                    }
                    Image(
                        painter = painterResource(id = R.drawable.img_mayra_avatar_1794683237081),
                        contentDescription = stringResource(id = R.string.cd_mayra_avatar),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    )
                    Column {
                        Text(
                            text = "Mayra",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Online",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(MaterialTheme.colorScheme.tertiary, CircleShape)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onInterruptPlayback,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("live_interrupt_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                        contentDescription = "Interrupt Mayra",
                        tint = if (runtime.mode == AssistantMode.SPEAKING) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }

            // Center Purely Voice-Driven Reactive Waveform (No text chat UI)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                MayraVoiceOrb(
                    mode = runtime.mode,
                    inputAmplitude = runtime.inputAmplitude,
                    outputAmplitude = runtime.outputAmplitude,
                    onClick = onInterruptPlayback
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = runtime.statusHeadline,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = runtime.statusSubtext,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                runtime.lastToolEvent?.let { event ->
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = event.summary,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            // Red Square Stop Button matching Photo Screen 2 ("Tap to Stop")
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.error,
                    border = BorderStroke(3.dp, Color.White.copy(alpha = 0.25f)),
                    shadowElevation = 12.dp,
                    modifier = Modifier
                        .size(72.dp)
                        .minimumInteractiveComponentSize()
                        .clip(CircleShape)
                        .clickable(onClick = onStopLiveSession)
                        .testTag("stop_live_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Stop,
                            contentDescription = "Tap to Stop",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap to Stop",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
private fun HorizontalNeonSpectrum(
    energy: Float,
    active: Boolean,
    primary: Color,
    secondary: Color,
    modifier: Modifier = Modifier
) {
    val smoothEnergy by animateFloatAsState(
        targetValue = energy,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 280f),
        label = "spectrumEnergy"
    )
    val transition = rememberInfiniteTransition(label = "spectrumWave")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spectrumPhase"
    )

    Canvas(modifier = modifier) {
        val barCount = 34
        val gap = size.width / (barCount * 2.2f)
        val barWidth = (size.width - gap * (barCount - 1)) / barCount
        val midY = size.height / 2f
        val maxHalf = size.height * 0.46f

        for (i in 0 until barCount) {
            val normDist = abs(i - (barCount - 1) / 2f) / ((barCount - 1) / 2f)
            val env = 0.35f + 0.65f * (1f - normDist)
            val wave = if (active) {
                0.35f + 0.65f * abs(sin(phase + i * 0.42f))
            } else {
                0.15f
            }
            val halfH = (3.dp.toPx() + maxHalf * env * smoothEnergy * wave).coerceAtMost(maxHalf)
            val x = i * (barWidth + gap)
            drawRoundRect(
                brush = Brush.verticalGradient(listOf(secondary, primary, secondary)),
                topLeft = Offset(x, midY - halfH),
                size = Size(barWidth, halfH * 2f),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
                alpha = if (active) 0.85f else 0.3f
            )
        }
    }
}
