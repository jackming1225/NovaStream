package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.PacingMode
import com.example.model.StreamConfig
import com.example.model.StreamProtocol
import com.example.model.VideoCodec

@Entity(tableName = "streams")
data class StreamEntity(
    @PrimaryKey val id: String,
    val name: String,
    val protocol: String,
    val codec: String,
    val url: String,
    val targetFps: Int,
    val lowLatencyMode: Boolean,
    val asyncCallbackMode: Boolean,
    val zeroCopySurface: Boolean,
    val pacingMode: String,
    val initialJitterBufferMs: Int,
    val isPreset: Boolean
) {
    fun toStreamConfig(): StreamConfig {
        return StreamConfig(
            id = id,
            name = name,
            protocol = try { StreamProtocol.valueOf(protocol) } catch (_: Exception) { StreamProtocol.TEST_SIGNAL },
            codec = try { VideoCodec.valueOf(codec) } catch (_: Exception) { VideoCodec.H264_AVC },
            url = url,
            targetFps = targetFps,
            lowLatencyMode = lowLatencyMode,
            asyncCallbackMode = asyncCallbackMode,
            zeroCopySurface = zeroCopySurface,
            pacingMode = try { PacingMode.valueOf(pacingMode) } catch (_: Exception) { PacingMode.VULKAN_MAILBOX },
            initialJitterBufferMs = initialJitterBufferMs
        )
    }

    companion object {
        fun fromStreamConfig(config: StreamConfig, isPreset: Boolean = false): StreamEntity {
            return StreamEntity(
                id = config.id,
                name = config.name,
                protocol = config.protocol.name,
                codec = config.codec.name,
                url = config.url,
                targetFps = config.targetFps,
                lowLatencyMode = config.lowLatencyMode,
                asyncCallbackMode = config.asyncCallbackMode,
                zeroCopySurface = config.zeroCopySurface,
                pacingMode = config.pacingMode.name,
                initialJitterBufferMs = config.initialJitterBufferMs,
                isPreset = isPreset
            )
        }
    }
}
