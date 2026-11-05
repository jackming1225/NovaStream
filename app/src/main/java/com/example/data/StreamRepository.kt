package com.example.data

import com.example.model.PacingMode
import com.example.model.PipelineArchitecture
import com.example.model.StreamConfig
import com.example.model.StreamProtocol
import com.example.model.VideoCodec
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StreamRepository(private val dao: StreamDao) {

    val allStreams: Flow<List<StreamConfig>> = dao.getAllStreams().map { entities ->
        entities.map { it.toStreamConfig() }
    }

    suspend fun saveStream(config: StreamConfig) {
        dao.insertStream(StreamEntity.fromStreamConfig(config, isPreset = false))
    }

    suspend fun deleteStream(config: StreamConfig) {
        dao.deleteStream(StreamEntity.fromStreamConfig(config, isPreset = false))
    }

    suspend fun ensureDefaultPresets() {
        if (dao.getCount() == 0) {
            val defaults = listOf(
                StreamConfig(
                    id = "synth_60fps",
                    name = "Photon-to-Photon Sync Clock",
                    protocol = StreamProtocol.TEST_SIGNAL,
                    codec = VideoCodec.H264_AVC,
                    url = "synth://calibration-feed/60fps-clock",
                    targetFps = 60,
                    lowLatencyMode = true,
                    asyncCallbackMode = true,
                    zeroCopySurface = true,
                    pacingMode = PacingMode.VULKAN_MAILBOX,
                    initialJitterBufferMs = 12,
                    architecture = PipelineArchitecture.AMEDIACODEC_DIRECT
                ),
                StreamConfig(
                    id = "moonlight_pc_stream",
                    name = "Moonlight / Sunshine PC Game Stream",
                    protocol = StreamProtocol.RTSP,
                    codec = VideoCodec.H264_AVC,
                    url = "rtsp://192.168.1.150:48010/stream0",
                    targetFps = 60,
                    lowLatencyMode = true,
                    asyncCallbackMode = true,
                    zeroCopySurface = true,
                    pacingMode = PacingMode.VULKAN_MAILBOX,
                    initialJitterBufferMs = 10,
                    architecture = PipelineArchitecture.AMEDIACODEC_DIRECT
                ),
                StreamConfig(
                    id = "dji_fpv_feed",
                    name = "DJI O3 Air Unit FPV Drone Cam",
                    protocol = StreamProtocol.DIRECT_UDP,
                    codec = VideoCodec.H265_HEVC,
                    url = "udp://192.168.2.1:5000",
                    targetFps = 60,
                    lowLatencyMode = true,
                    asyncCallbackMode = true,
                    zeroCopySurface = true,
                    pacingMode = PacingMode.VULKAN_MAILBOX,
                    initialJitterBufferMs = 14,
                    architecture = PipelineArchitecture.AMEDIACODEC_DIRECT
                ),
                StreamConfig(
                    id = "webrtc_whep_edge",
                    name = "WebRTC WHEP Ultra-Low Latency Peer",
                    protocol = StreamProtocol.WEBRTC,
                    codec = VideoCodec.H264_AVC,
                    url = "https://edge.novastream.internal/whep/session",
                    targetFps = 60,
                    lowLatencyMode = true,
                    asyncCallbackMode = true,
                    zeroCopySurface = true,
                    pacingMode = PacingMode.VULKAN_MAILBOX,
                    initialJitterBufferMs = 8,
                    architecture = PipelineArchitecture.AMEDIACODEC_DIRECT
                ),
                StreamConfig(
                    id = "gopro_rtsp_live",
                    name = "GoPro Hero RTSP Field Monitor",
                    protocol = StreamProtocol.RTSP,
                    codec = VideoCodec.H264_AVC,
                    url = "rtsp://10.5.5.9:8554/live/stream",
                    targetFps = 60,
                    lowLatencyMode = true,
                    asyncCallbackMode = true,
                    zeroCopySurface = true,
                    pacingMode = PacingMode.VULKAN_MAILBOX,
                    initialJitterBufferMs = 18,
                    architecture = PipelineArchitecture.AMEDIACODEC_DIRECT
                )
            ).map { StreamEntity.fromStreamConfig(it, isPreset = true) }

            dao.insertStreams(defaults)
        }
    }
}
