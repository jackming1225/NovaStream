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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.JitterBufferStats
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.ConsoleBackground
import com.example.ui.theme.InstrumentCyan
import com.example.ui.theme.PhosphorGreen
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

@Composable
fun JitterBufferGraph(
    stats: JitterBufferStats,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("jitter_buffer_graph_card"),
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
                Column {
                    Text(
                        text = "ADAPTIVE JITTER BUFFER (RFC 3550)",
                        style = MaterialTheme.typography.labelMedium,
                        color = InstrumentCyan,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Dynamic target queue delay vs arrival jitter variance",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Text(
                    text = String.format("%.1f ms", stats.targetDelayMs),
                    style = MaterialTheme.typography.titleMedium,
                    color = PhosphorGreen,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .height(90.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(ConsoleBackground)
                    .border(1.dp, StudioBorder, RoundedCornerShape(6.dp))
            ) {
                Canvas(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("jitter_history_canvas")
                ) {
                    val w = size.width
                    val h = size.height
                    val maxJitterScale = 40.0f

                    // Draw Target Delay line
                    val targetY = h - (stats.targetDelayMs / maxJitterScale).coerceIn(0f, 1f) * h
                    drawLine(
                        color = InstrumentCyan.copy(alpha = 0.5f),
                        start = Offset(0f, targetY),
                        end = Offset(w, targetY),
                        strokeWidth = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )

                    val history = stats.jitterHistory
                    if (history.isNotEmpty()) {
                        val stepX = w / (history.size - 1).coerceAtLeast(1)
                        val path = Path()

                        history.forEachIndexed { i, jitter ->
                            val x = i * stepX
                            val clamped = jitter.coerceIn(0f, maxJitterScale)
                            val y = h - (clamped / maxJitterScale) * h
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }

                        drawPath(
                            path = path,
                            color = AmberWarning,
                            style = Stroke(
                                width = 2.2f,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Jitter: ${String.format("%.1f", stats.measuredJitterMs)}ms (σ ${String.format("%.1f", stats.jitterStdDevMs)})",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
                Text(
                    text = "NACK: ${stats.nackRecoveredPackets} | FEC: ${stats.fecRecoveredPackets}",
                    style = MaterialTheme.typography.labelSmall,
                    color = PhosphorGreen,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
                Text(
                    text = "Loss: ${String.format("%.1f%%", stats.packetLossPercent)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (stats.packetLossPercent > 2f) AmberWarning else TextMuted,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
            }
        }
    }
}
