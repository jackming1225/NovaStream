package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.decoder.VideoStreamingEngine
import com.example.model.PacingMode
import com.example.model.StreamConfig
import com.example.ui.components.ConsoleLogViewer
import com.example.ui.components.LatencyWaterfallChart
import com.example.ui.components.OptimizationBenchmarkCard
import com.example.ui.components.VideoSurfaceView
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.ConsoleBackground
import com.example.ui.theme.InstrumentCyan
import com.example.ui.theme.PhosphorGreen
import com.example.ui.theme.SafetyRed
import com.example.ui.theme.StudioBlack
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioCardHover
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlayerScreen(
    engine: VideoStreamingEngine,
    streamList: List<StreamConfig>,
    onSelectStream: (StreamConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val config by engine.streamConfig.collectAsState()
    val isPlaying by engine.isPlaying.collectAsState()
    val metrics by engine.latencyMetrics.collectAsState()
    val dumpsys by engine.dumpsysStats.collectAsState()
    val logs by engine.consoleLogs.collectAsState()
    val isOptimizationActive by engine.isOptimizationEnabled.collectAsState()

    var showLogs by remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBlack)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Engineering Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "NOVASTREAM",
                        style = MaterialTheme.typography.titleLarge,
                        color = InstrumentCyan,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(StudioCardHover)
                            .border(1.dp, StudioBorder, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = config.protocol.badge,
                            style = MaterialTheme.typography.labelSmall,
                            color = InstrumentCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
                Text(
                    text = config.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1
                )
            }

            // Real-Time Monospace Metric Chips
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StudioMetricChip(
                    label = "FRAME RATE",
                    value = String.format("%.0f FPS", metrics.fps),
                    color = if (metrics.fps >= 58f) PhosphorGreen else AmberWarning
                )
                StudioMetricChip(
                    label = "INGEST-TO-DISPLAY",
                    value = String.format("%.0f ms", metrics.totalLatencyMs),
                    color = if (metrics.totalLatencyMs < 25f) PhosphorGreen else InstrumentCyan
                )
            }
        }

        // Native SurfaceView Video Canvas with HUD Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, StudioBorder, RoundedCornerShape(14.dp))
        ) {
            VideoSurfaceView(engine = engine)

            // Tactile Bottom Overlay HUD
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(ConsoleBackground.copy(alpha = 0.88f))
                    .border(1.dp, StudioBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(if (isPlaying) PhosphorGreen else AmberWarning, CircleShape)
                )
                Text(
                    text = if (isPlaying) "60 FPS DECODE" else "STREAM PAUSED",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )

                IconButton(
                    onClick = { engine.togglePlayback() },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("play_pause_button")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Toggle stream playback",
                        tint = InstrumentCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Vulkan / EGL Frame Pacing Selector
        Card(
            modifier = Modifier.fillMaxWidth().testTag("pacing_selector_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = StudioCard),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(StudioBorder))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = InstrumentCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "VULKAN / EGL PRESENT PACING",
                            style = MaterialTheme.typography.labelMedium,
                            color = InstrumentCyan,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                    Text(
                        text = if (config.pacingMode.isTearFree) "Tear-Free" else "Zero-Sync",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (config.pacingMode.isTearFree) PhosphorGreen else AmberWarning,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = config.pacingMode.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PacingMode.values().forEach { mode ->
                        FilterChip(
                            selected = config.pacingMode == mode,
                            onClick = { engine.setPacingMode(mode) },
                            label = {
                                Text(
                                    text = mode.title,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (config.pacingMode == mode) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = InstrumentCyan,
                                selectedLabelColor = StudioBlack,
                                containerColor = ConsoleBackground,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = config.pacingMode == mode,
                                borderColor = StudioBorder,
                                selectedBorderColor = InstrumentCyan
                            ),
                            modifier = Modifier.testTag("pacing_chip_${mode.name.lowercase()}")
                        )
                    }
                }
            }
        }

        // Latency Waterfall Breakdown
        LatencyWaterfallChart(metrics = metrics)

        // Architecture Benchmark Card (40% Peak Jank Reduction)
        OptimizationBenchmarkCard(
            isOptimized = isOptimizationActive,
            dumpsys = dumpsys,
            onToggleOptimization = { engine.setOptimizationEnabled(it) }
        )

        // Real-Time Event Logcat Viewer
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showLogs = !showLogs },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "LIVE PIPELINE LOGS (${logs.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
                Icon(
                    imageVector = if (showLogs) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(visible = showLogs) {
                ConsoleLogViewer(
                    logs = logs,
                    onCopyReport = { engine.exportBenchmarkReport() }
                )
            }
        }

        // Stream Presets Section
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "STREAM PROFILES & TARGET FEEDS",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                streamList.forEach { stream ->
                    val isSelected = stream.id == config.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelectStream(stream) }
                            .testTag("stream_preset_${stream.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) StudioCardHover else StudioCard
                        ),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(
                                if (isSelected) InstrumentCyan else StudioBorder
                            )
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(if (isSelected) PhosphorGreen else TextMuted, CircleShape)
                                )
                                Column(modifier = Modifier.padding(start = 10.dp)) {
                                    Text(
                                        text = stream.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isSelected) TextPrimary else TextSecondary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Text(
                                        text = "${stream.protocol.displayName} • ${stream.codec.shortName} • ${stream.targetFps} FPS",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = if (isSelected) InstrumentCyan else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun StudioMetricChip(
    label: String,
    value: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(ConsoleBackground)
            .border(1.dp, StudioBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 8.5.sp)
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
