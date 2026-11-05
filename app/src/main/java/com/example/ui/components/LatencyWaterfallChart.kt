package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WaterfallChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LatencyMetrics
import com.example.ui.theme.ConsoleBackground
import com.example.ui.theme.InstrumentCyan
import com.example.ui.theme.PhosphorGreen
import com.example.ui.theme.PurpleBus
import com.example.ui.theme.SlateBlue
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LatencyWaterfallChart(
    metrics: LatencyMetrics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("latency_waterfall_card"),
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
                    Icon(
                        imageVector = Icons.Default.WaterfallChart,
                        contentDescription = null,
                        tint = InstrumentCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Column(modifier = Modifier.padding(start = 6.dp)) {
                        Text(
                            text = "STAGE LATENCY WATERFALL",
                            style = MaterialTheme.typography.labelMedium,
                            color = InstrumentCyan,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Photon-to-photon delay per pipeline stage",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = String.format("%.1f ms", metrics.totalLatencyMs),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (metrics.totalLatencyMs < 25f) PhosphorGreen else InstrumentCyan,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Total Glass-to-Glass",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 9.sp
                    )
                }
            }

            // Stacked Segment Canvas
            val net = metrics.networkJitterMs.coerceAtLeast(0.5f)
            val dec = metrics.decodeLatencyMs.coerceAtLeast(0.5f)
            val comp = metrics.surfaceCompositionMs.coerceAtLeast(0.5f)
            val fence = metrics.gpuPresentFenceMs.coerceAtLeast(0.5f)
            val total = (net + dec + comp + fence).coerceAtLeast(1f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(ConsoleBackground)
                    .border(1.dp, StudioBorder, RoundedCornerShape(6.dp))
                    .padding(4.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .testTag("waterfall_bar_canvas")
                ) {
                    val w = size.width
                    val h = size.height

                    val wNet = (net / total) * w
                    val wDec = (dec / total) * w
                    val wComp = (comp / total) * w
                    val wFence = (fence / total) * w

                    var curX = 0f

                    // 1. Network Jitter
                    drawRoundRect(
                        color = SlateBlue,
                        topLeft = Offset(curX, 0f),
                        size = Size(wNet, h),
                        cornerRadius = CornerRadius(0f, 0f)
                    )
                    curX += wNet

                    // 2. Hardware Decode (AMediaCodec)
                    drawRoundRect(
                        color = PhosphorGreen,
                        topLeft = Offset(curX, 0f),
                        size = Size(wDec, h),
                        cornerRadius = CornerRadius(0f, 0f)
                    )
                    curX += wDec

                    // 3. SurfaceFlinger / ANativeWindow Composition
                    drawRoundRect(
                        color = InstrumentCyan,
                        topLeft = Offset(curX, 0f),
                        size = Size(wComp, h),
                        cornerRadius = CornerRadius(0f, 0f)
                    )
                    curX += wComp

                    // 4. Vulkan Mailbox Present Fence
                    drawRoundRect(
                        color = PurpleBus,
                        topLeft = Offset(curX, 0f),
                        size = Size(wFence, h),
                        cornerRadius = CornerRadius(0f, 0f)
                    )
                }
            }

            // Legend Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LegendItem(color = SlateBlue, label = "RTP Jitter", value = String.format("%.1f ms", net))
                LegendItem(color = PhosphorGreen, label = "AMediaCodec", value = String.format("%.1f ms", dec))
                LegendItem(color = InstrumentCyan, label = "SurfaceView", value = String.format("%.1f ms", comp))
                LegendItem(color = PurpleBus, label = "VkFence", value = String.format("%.1f ms", fence))
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(color, CircleShape)
        )
        Column(modifier = Modifier.padding(start = 5.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 9.5.sp)
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.5.sp
            )
        }
    }
}
