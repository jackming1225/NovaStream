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
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.ConsoleBackground
import com.example.ui.theme.InstrumentCyan
import com.example.ui.theme.PhosphorGreen
import com.example.ui.theme.SafetyRed
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun FrameTimeGraph(
    frameTimes: List<Float>,
    targetFps: Int = 60,
    modifier: Modifier = Modifier
) {
    val targetBudgetMs = 1000f / targetFps.coerceAtLeast(30)
    val maxDisplayMs = 33.3f // 2 frames budget

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("frame_time_graph_card"),
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
                        text = "FRAME PACING OSCILLOSCOPE",
                        style = MaterialTheme.typography.labelMedium,
                        color = InstrumentCyan,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Real-time frame durations (Budget: ${String.format("%.1f", targetBudgetMs)}ms)",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                val currentMs = frameTimes.lastOrNull() ?: targetBudgetMs
                Text(
                    text = String.format("%.2f ms", currentMs),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (currentMs <= targetBudgetMs + 1f) PhosphorGreen else AmberWarning,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .height(105.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(ConsoleBackground)
                    .border(1.dp, StudioBorder, RoundedCornerShape(6.dp))
            ) {
                Canvas(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("frame_time_waveform_canvas")
                ) {
                    val w = size.width
                    val h = size.height

                    // 60 FPS Target Budget Line (16.6ms)
                    val targetY = h - (targetBudgetMs / maxDisplayMs) * h
                    drawLine(
                        color = AmberWarning.copy(alpha = 0.5f),
                        start = Offset(0f, targetY),
                        end = Offset(w, targetY),
                        strokeWidth = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                    )

                    // 120 FPS Target Line (8.3ms)
                    val fastY = h - (8.33f / maxDisplayMs) * h
                    drawLine(
                        color = PhosphorGreen.copy(alpha = 0.3f),
                        start = Offset(0f, fastY),
                        end = Offset(w, fastY),
                        strokeWidth = 1.2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )

                    if (frameTimes.isNotEmpty()) {
                        val stepX = w / (frameTimes.size - 1).coerceAtLeast(1)
                        val path = Path()

                        frameTimes.forEachIndexed { index, duration ->
                            val x = index * stepX
                            val clampedDuration = duration.coerceIn(0f, maxDisplayMs)
                            val y = h - (clampedDuration / maxDisplayMs) * h

                            if (index == 0) {
                                path.moveTo(x, y)
                            } else {
                                path.lineTo(x, y)
                            }

                            // Highlight jank spikes
                            if (duration > targetBudgetMs + 1f) {
                                drawCircle(
                                    color = if (duration > targetBudgetMs * 1.5f) SafetyRed else AmberWarning,
                                    radius = 3.5f,
                                    center = Offset(x, y)
                                )
                            }
                        }

                        drawPath(
                            path = path,
                            color = InstrumentCyan,
                            style = Stroke(
                                width = 2.5f,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }
            }

            // Bottom metric chips
            val avg = if (frameTimes.isNotEmpty()) frameTimes.average().toFloat() else targetBudgetMs
            val max = frameTimes.maxOrNull() ?: targetBudgetMs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "VSync Target: ${String.format("%.1f", targetBudgetMs)}ms",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
                Text(
                    text = "Avg: ${String.format("%.1f", avg)}ms",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
                Text(
                    text = "Peak: ${String.format("%.1f", max)}ms",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (max > targetBudgetMs * 1.5f) SafetyRed else PhosphorGreen,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
