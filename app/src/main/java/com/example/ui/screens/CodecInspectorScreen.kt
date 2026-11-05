package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.model.CodecCapability
import com.example.model.StreamConfig
import com.example.model.StreamProtocol
import com.example.model.VideoCodec
import com.example.ui.MainViewModel
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.ConsoleBackground
import com.example.ui.theme.InstrumentCyan
import com.example.ui.theme.PhosphorGreen
import com.example.ui.theme.SafetyRed
import com.example.ui.theme.SlateBlue
import com.example.ui.theme.StudioBlack
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioCardHover
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.UUID

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CodecInspectorScreen(
    viewModel: MainViewModel,
    onStreamSelected: (StreamConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val codecs by viewModel.availableCodecs.collectAsState()
    val savedStreams by viewModel.streams.collectAsState()
    val currentConfig by viewModel.engine.streamConfig.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
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
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                        tint = InstrumentCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "HARDWARE CODECS & STREAMS",
                        style = MaterialTheme.typography.titleMedium,
                        color = InstrumentCyan,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Text(
                    text = "AOSP MediaCodecList & Zero-Copy ANativeWindow",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = InstrumentCyan),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.testTag("add_custom_stream_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = StudioBlack, modifier = Modifier.size(15.dp))
                Text("Add Stream", color = StudioBlack, fontWeight = FontWeight.Bold, fontSize = 11.5.sp, modifier = Modifier.padding(start = 4.dp))
            }
        }

        // Section: Hardware Decoders
        Text(
            text = "HARDWARE DECODERS DETECTED (${codecs.size})",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            letterSpacing = 1.sp,
            fontWeight = FontWeight.Bold
        )

        codecs.forEach { codec ->
            CodecCard(codec = codec)
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Section: Saved Streams
        Text(
            text = "SAVED STREAM PROFILES (${savedStreams.size})",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            letterSpacing = 1.sp,
            fontWeight = FontWeight.Bold
        )

        savedStreams.forEach { stream ->
            val isActive = stream.id == currentConfig.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("stream_row_${stream.id}"),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = if (isActive) StudioCardHover else StudioCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(if (isActive) InstrumentCyan else StudioBorder))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = stream.name, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                            if (isActive) {
                                Box(
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(PhosphorGreen.copy(alpha = 0.15f))
                                        .border(1.dp, PhosphorGreen.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("ACTIVE", style = MaterialTheme.typography.labelSmall, color = PhosphorGreen, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                        Text(text = stream.url, style = MaterialTheme.typography.bodySmall, color = TextMuted, fontFamily = FontFamily.Monospace, maxLines = 1, fontSize = 10.5.sp)
                        Text(
                            text = "${stream.protocol.displayName} • ${stream.codec.shortName} • ${stream.targetFps} FPS",
                            style = MaterialTheme.typography.labelSmall,
                            color = InstrumentCyan,
                            fontSize = 10.5.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onStreamSelected(stream) },
                            modifier = Modifier.size(34.dp).testTag("play_stream_${stream.id}")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play stream", tint = if (isActive) PhosphorGreen else InstrumentCyan)
                        }
                        if (stream.id != "synth_60fps") {
                            IconButton(
                                onClick = { viewModel.deleteStream(stream) },
                                modifier = Modifier.size(34.dp).testTag("delete_stream_${stream.id}")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showAddDialog) {
        AddStreamDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { newStream ->
                viewModel.saveCustomStream(newStream)
                onStreamSelected(newStream)
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CodecCard(codec: CodecCapability) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("codec_card_${codec.codecName}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = StudioCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(StudioBorder))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = codec.codecName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
                Text(
                    text = codec.mimeType,
                    style = MaterialTheme.typography.labelSmall,
                    color = InstrumentCyan,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.5.sp
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (codec.isHardwareAccelerated) {
                    PillBadge("HARDWARE ACCELERATED", PhosphorGreen)
                } else {
                    PillBadge("SOFTWARE FALLBACK", AmberWarning)
                }

                if (codec.supportsLowLatency) {
                    PillBadge("FEATURE_LowLatency", InstrumentCyan)
                }

                PillBadge("${codec.maxResolution} @ ${codec.maxFrameRate}FPS", SlateBlue)
            }

            Text(
                text = "Color Formats: " + codec.colorFormats.joinToString(", "),
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontFamily = FontFamily.Monospace,
                fontSize = 9.5.sp
            )
        }
    }
}

@Composable
private fun PillBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun AddStreamDialog(
    onDismiss: () -> Unit,
    onAdd: (StreamConfig) -> Unit
) {
    var name by remember { mutableStateOf("Local Game Stream") }
    var url by remember { mutableStateOf("rtsp://192.168.1.120:8554/live") }
    var protocol by remember { mutableStateOf(StreamProtocol.RTSP) }
    var codec by remember { mutableStateOf(VideoCodec.H264_AVC) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Stream Endpoint", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Stream Label") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = InstrumentCyan, focusedLabelColor = InstrumentCyan),
                    modifier = Modifier.fillMaxWidth().testTag("stream_name_input")
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Stream URL (RTSP / WebRTC / UDP)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = InstrumentCyan, focusedLabelColor = InstrumentCyan),
                    modifier = Modifier.fillMaxWidth().testTag("stream_url_input")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { protocol = StreamProtocol.RTSP },
                        colors = ButtonDefaults.buttonColors(containerColor = if (protocol == StreamProtocol.RTSP) InstrumentCyan else ConsoleBackground),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("RTSP", color = if (protocol == StreamProtocol.RTSP) StudioBlack else TextSecondary)
                    }
                    Button(
                        onClick = { protocol = StreamProtocol.WEBRTC },
                        colors = ButtonDefaults.buttonColors(containerColor = if (protocol == StreamProtocol.WEBRTC) InstrumentCyan else ConsoleBackground),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("WebRTC", color = if (protocol == StreamProtocol.WEBRTC) StudioBlack else TextSecondary)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newConfig = StreamConfig(
                        id = UUID.randomUUID().toString(),
                        name = name.ifBlank { "Custom Stream" },
                        protocol = protocol,
                        codec = codec,
                        url = url.ifBlank { "rtsp://127.0.0.1:8554/live" },
                        targetFps = 60,
                        lowLatencyMode = true,
                        asyncCallbackMode = true,
                        zeroCopySurface = true
                    )
                    onAdd(newConfig)
                },
                colors = ButtonDefaults.buttonColors(containerColor = InstrumentCyan),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.testTag("confirm_add_stream_btn")
            ) {
                Text("Save & Connect", color = StudioBlack, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = StudioCard
    )
}
