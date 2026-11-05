package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.decoder.VideoStreamingEngine
import com.example.model.JitterBufferStats
import com.example.ui.components.JitterBufferGraph
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.ConsoleBackground
import com.example.ui.theme.InstrumentCyan
import com.example.ui.theme.PhosphorGreen
import com.example.ui.theme.SafetyRed
import com.example.ui.theme.StudioBlack
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JitterBufferScreen(
    engine: VideoStreamingEngine,
    modifier: Modifier = Modifier
) {
    val stats by engine.jitterStats.collectAsState()
    val streamConfig by engine.streamConfig.collectAsState()
    val currentJitterSim by engine.simulatedNetworkJitterMs.collectAsState()
    val currentLossSim by engine.simulatedPacketLossPct.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBlack)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Router,
                        contentDescription = null,
                        tint = InstrumentCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "RTSP & WEBRTC INGESTION",
                        style = MaterialTheme.typography.titleMedium,
                        color = InstrumentCyan,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Text(
                    text = "RTP / UDP Layer with RFC 3550 Adaptive Jitter Buffer",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(ConsoleBackground)
                    .border(1.dp, PhosphorGreen.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "60 FPS PACED",
                    style = MaterialTheme.typography.labelSmall,
                    color = PhosphorGreen,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp
                )
            }
        }

        // Live Jitter Buffer Graph
        JitterBufferGraph(stats = stats)

        // Packet Loss Recovery & Telemetry Metrics Card
        PacketRecoveryTelemetryCard(stats = stats)

        // Interactive Network Impairment Injector
        NetworkImpairmentCard(
            simJitterMs = currentJitterSim,
            simLossPct = currentLossSim,
            onJitterChanged = { engine.simulatedNetworkJitterMs.value = it },
            onLossChanged = { engine.simulatedPacketLossPct.value = it }
        )

        // Resilience Feature Toggles
        Card(
            modifier = Modifier.fillMaxWidth().testTag("resilience_toggles_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = StudioCard),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(StudioBorder))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "PACKET LOSS RECOVERY MECHANISMS",
                    style = MaterialTheme.typography.labelMedium,
                    color = InstrumentCyan,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "NACK Retransmission", style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text(text = "Requests missing RTP sequence numbers via RTCP feedback", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                    Switch(
                        checked = streamConfig.nackRecoveryEnabled,
                        onCheckedChange = { engine.setStreamConfig(streamConfig.copy(nackRecoveryEnabled = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = StudioBlack, checkedTrackColor = InstrumentCyan),
                        modifier = Modifier.testTag("nack_switch")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Forward Error Correction (FEC)", style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text(text = "Reconstructs dropped frames instantly using parity blocks", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                    Switch(
                        checked = streamConfig.fecRecoveryEnabled,
                        onCheckedChange = { engine.setStreamConfig(streamConfig.copy(fecRecoveryEnabled = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = StudioBlack, checkedTrackColor = InstrumentCyan),
                        modifier = Modifier.testTag("fec_switch")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PacketRecoveryTelemetryCard(stats: JitterBufferStats) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("packet_telemetry_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = StudioCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(StudioBorder))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = InstrumentCyan, modifier = Modifier.size(18.dp))
                    Text(
                        text = "PACKET LOSS RECOVERY TELEMETRY",
                        style = MaterialTheme.typography.labelMedium,
                        color = InstrumentCyan,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
                Text(
                    text = "${stats.totalPacketsReceived} received",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatPill(
                    icon = Icons.Default.Replay,
                    title = "NACK Recovered",
                    value = "${stats.nackRecoveredPackets}",
                    color = PhosphorGreen,
                    modifier = Modifier.weight(1f)
                )
                StatPill(
                    icon = Icons.Default.Healing,
                    title = "FEC Fixed",
                    value = "${stats.fecRecoveredPackets}",
                    color = InstrumentCyan,
                    modifier = Modifier.weight(1f)
                )
                StatPill(
                    icon = Icons.Default.NetworkCheck,
                    title = "Lost Packets",
                    value = "${stats.totalPacketsLost}",
                    color = if (stats.totalPacketsLost > 20) AmberWarning else TextSecondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NetworkImpairmentCard(
    simJitterMs: Float,
    simLossPct: Float,
    onJitterChanged: (Float) -> Unit,
    onLossChanged: (Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("network_impairment_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = StudioCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(StudioBorder))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = InstrumentCyan, modifier = Modifier.size(18.dp))
                    Text(
                        text = "FIELD IMPAIRMENT SIMULATOR",
                        style = MaterialTheme.typography.labelMedium,
                        color = InstrumentCyan,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
                Text(text = "Test 60 FPS resilience", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }

            // Quick scenario chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                QuickScenarioChip(label = "Clean LAN (2ms / 0%)", onClick = { onJitterChanged(2f); onLossChanged(0f) })
                QuickScenarioChip(label = "4G LTE (18ms / 2%)", onClick = { onJitterChanged(18f); onLossChanged(2f) })
                QuickScenarioChip(label = "Hotel Wi-Fi (45ms / 6%)", onClick = { onJitterChanged(45f); onLossChanged(6f) })
                QuickScenarioChip(label = "Extreme (75ms / 12%)", onClick = { onJitterChanged(75f); onLossChanged(12f) })
            }

            // Sliders
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Injected Jitter", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text(
                        text = String.format("%.0f ms", simJitterMs),
                        style = MaterialTheme.typography.bodySmall,
                        color = InstrumentCyan,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Slider(
                    value = simJitterMs,
                    onValueChange = onJitterChanged,
                    valueRange = 0f..80f,
                    colors = SliderDefaults.colors(thumbColor = InstrumentCyan, activeTrackColor = InstrumentCyan),
                    modifier = Modifier.testTag("jitter_slider")
                )
            }

            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Injected Packet Loss", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text(
                        text = String.format("%.1f %%", simLossPct),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (simLossPct > 5f) AmberWarning else InstrumentCyan,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Slider(
                    value = simLossPct,
                    onValueChange = onLossChanged,
                    valueRange = 0f..15f,
                    colors = SliderDefaults.colors(thumbColor = AmberWarning, activeTrackColor = AmberWarning),
                    modifier = Modifier.testTag("loss_slider")
                )
            }
        }
    }
}

@Composable
private fun QuickScenarioChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(ConsoleBackground)
            .border(1.dp, StudioBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp)
            .clickable { onClick() }
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 10.5.sp)
    }
}

@Composable
private fun StatPill(
    icon: ImageVector,
    title: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(ConsoleBackground)
            .border(1.dp, StudioBorder, RoundedCornerShape(6.dp))
            .padding(8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 8.5.sp, modifier = Modifier.padding(top = 2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            )
        }
    }
}
