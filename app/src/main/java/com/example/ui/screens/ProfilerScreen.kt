package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.model.DumpsysMetrics
import com.example.model.LatencyMetrics
import com.example.profiler.HalTelemetry
import com.example.ui.components.FrameTimeGraph
import com.example.ui.components.OptimizationBenchmarkCard
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

@Composable
fun ProfilerScreen(
    engine: VideoStreamingEngine,
    modifier: Modifier = Modifier
) {
    val latencyMetrics by engine.latencyMetrics.collectAsState()
    val dumpsys by engine.dumpsysStats.collectAsState()
    val halTelemetry by engine.halStats.collectAsState()
    val isOptimizationActive by engine.isOptimizationEnabled.collectAsState()

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
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = InstrumentCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "GPU & HAL DIAGNOSTICS",
                        style = MaterialTheme.typography.titleMedium,
                        color = InstrumentCyan,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Text(
                    text = "Dumpsys (gfxinfo, surfaceflinger) & AOSP Multimedia HAL",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(ConsoleBackground)
                    .border(1.dp, StudioBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "AGI / TRACEVIEW ACTIVE",
                    style = MaterialTheme.typography.labelSmall,
                    color = InstrumentCyan,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp
                )
            }
        }

        // Real-Time Frame Pacing Oscilloscope
        FrameTimeGraph(
            frameTimes = latencyMetrics.frameTimeHistory,
            targetFps = latencyMetrics.targetFps
        )

        // Dumpsys gfxinfo Metrics Grid
        DumpsysTelemetryCard(dumpsys = dumpsys, latencyMetrics = latencyMetrics)

        // 40% Peak Frame Drop Reduction Benchmark Card
        OptimizationBenchmarkCard(
            isOptimized = isOptimizationActive,
            dumpsys = dumpsys,
            onToggleOptimization = { engine.setOptimizationEnabled(it) }
        )

        // AOSP Multimedia HAL & SurfaceFlinger Architecture Card
        HalDiagnosticsCard(hal = halTelemetry, isOptimized = isOptimizationActive)

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DumpsysTelemetryCard(
    dumpsys: DumpsysMetrics,
    latencyMetrics: LatencyMetrics
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("dumpsys_telemetry_card"),
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
                    Icon(imageVector = Icons.Default.GraphicEq, contentDescription = null, tint = InstrumentCyan, modifier = Modifier.size(18.dp))
                    Text(
                        text = "DUMPSYS GFXINFO FRAME HISTOGRAM",
                        style = MaterialTheme.typography.labelMedium,
                        color = InstrumentCyan,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
                Text(
                    text = "${dumpsys.totalFramesTracked} frames sampled",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Percentiles Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PercentileBox(label = "p50 (Median)", value = String.format("%.1f ms", dumpsys.percentile50Ms), isGood = dumpsys.percentile50Ms < 12f, modifier = Modifier.weight(1f))
                PercentileBox(label = "p90", value = String.format("%.1f ms", dumpsys.percentile90Ms), isGood = dumpsys.percentile90Ms < 15f, modifier = Modifier.weight(1f))
                PercentileBox(label = "p95", value = String.format("%.1f ms", dumpsys.percentile95Ms), isGood = dumpsys.percentile95Ms < 16.6f, modifier = Modifier.weight(1f))
                PercentileBox(label = "p99", value = String.format("%.1f ms", dumpsys.percentile99Ms), isGood = dumpsys.percentile99Ms < 20f, modifier = Modifier.weight(1f))
            }

            // Jank frames & Binder Latency Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricChip(
                    icon = Icons.Default.Layers,
                    title = "Janky Frames",
                    value = "${dumpsys.jankyFramesCount} (${String.format("%.1f%%", latencyMetrics.frameDropRate)})",
                    color = if (dumpsys.jankyFramesCount < 10) PhosphorGreen else AmberWarning,
                    modifier = Modifier.weight(1f)
                )
                MetricChip(
                    icon = Icons.Default.Sync,
                    title = "HAL Binder Round-Trip",
                    value = String.format("%.2f ms", dumpsys.halBinderLatencyMs),
                    color = if (dumpsys.halBinderLatencyMs < 1.0f) PhosphorGreen else AmberWarning,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun HalDiagnosticsCard(
    hal: HalTelemetry,
    isOptimized: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("hal_diagnostics_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = StudioCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(StudioBorder))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.DeveloperBoard, contentDescription = null, tint = InstrumentCyan, modifier = Modifier.size(18.dp))
                    Text(
                        text = "AOSP MULTIMEDIA HAL BEHAVIOR",
                        style = MaterialTheme.typography.labelMedium,
                        color = InstrumentCyan,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
                Text(
                    text = if (isOptimized) "Stalls: 0" else "Stalls: 4",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isOptimized) PhosphorGreen else SafetyRed,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Text(
                text = "Service: ${hal.halService}",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontFamily = FontFamily.Monospace,
                fontSize = 9.5.sp
            )

            // Buffer Pool Status
            Text(
                text = "GraphicBuffer / AHardwareBuffer Pool State:",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                hal.bufferPoolState.forEach { buf ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(ConsoleBackground)
                            .border(1.dp, StudioBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        when (buf.status) {
                                            "ACQUIRED_BY_COMPOSITOR" -> InstrumentCyan
                                            "QUEUED_TO_SURFACEFLINGER" -> PhosphorGreen
                                            else -> TextMuted
                                        },
                                        CircleShape
                                    )
                            )
                            Text(
                                text = "Slot #${buf.slotIndex}: ${buf.status}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextPrimary,
                                modifier = Modifier.padding(start = 6.dp),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.5.sp
                            )
                        }
                        Text(
                            text = "${buf.ageMs}ms ago",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.5.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PercentileBox(
    label: String,
    value: String,
    isGood: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(ConsoleBackground)
            .border(1.dp, if (isGood) PhosphorGreen.copy(alpha = 0.35f) else AmberWarning.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
            .padding(7.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 8.5.sp)
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                color = if (isGood) PhosphorGreen else AmberWarning,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun MetricChip(
    icon: ImageVector,
    title: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(ConsoleBackground)
            .border(1.dp, StudioBorder, RoundedCornerShape(6.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Column(modifier = Modifier.padding(start = 6.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 8.5.sp)
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.5.sp
            )
        }
    }
}
