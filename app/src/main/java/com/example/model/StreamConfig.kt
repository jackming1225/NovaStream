package com.example.model

enum class StreamProtocol(val displayName: String, val badge: String) {
    RTSP("RTSP / RTP over UDP", "RTSP"),
    WEBRTC("WebRTC / WHEP Peer", "WebRTC"),
    TEST_SIGNAL("Synthetic 60FPS Signal", "HW-TEST"),
    DIRECT_UDP("Direct Raw UDP Stream", "UDP-RTP")
}

enum class VideoCodec(val mimeType: String, val shortName: String, val hardwareSuffix: String) {
    H264_AVC("video/avc", "H.264 (AVC)", "OMX/C2 AVC"),
    H265_HEVC("video/hevc", "H.265 (HEVC)", "OMX/C2 HEVC"),
    VP9("video/x-vnd.on2.vp9", "VP9", "OMX/C2 VP9"),
    AV1("video/av01", "AV1", "C2 AV1")
}

enum class PacingMode(
    val title: String,
    val technicalName: String,
    val description: String,
    val isTearFree: Boolean,
    val typicalLatencyDeltaMs: Float
) {
    VULKAN_MAILBOX(
        "Vulkan Mailbox",
        "VK_PRESENT_MODE_MAILBOX_KHR",
        "Unpinned triple-buffering swapchain. Incoming frames overwrite pending slot. Zero tearing without waiting 2 VSyncs.",
        isTearFree = true,
        typicalLatencyDeltaMs = 0f
    ),
    VULKAN_FIFO(
        "Vulkan FIFO",
        "VK_PRESENT_MODE_FIFO_KHR",
        "Standard VSync queued presentation. Frames wait in order for display refresh. Tear-free but adds 1-2 frames latency.",
        isTearFree = true,
        typicalLatencyDeltaMs = 16.6f
    ),
    IMMEDIATE(
        "Immediate Mode",
        "VK_PRESENT_MODE_IMMEDIATE_KHR",
        "Direct presentation to display scanout without waiting for VSync. Lowest latency but risks screen tearing.",
        isTearFree = false,
        typicalLatencyDeltaMs = -3.0f
    ),
    CHOREOGRAPHER(
        "AOSP Choreographer",
        "Choreographer.postFrameCallback",
        "Standard Android VSync loop. Coordinates buffer release with SurfaceFlinger tick.",
        isTearFree = true,
        typicalLatencyDeltaMs = 12.0f
    )
}

enum class PipelineArchitecture(
    val title: String,
    val subtitle: String,
    val isDirectHardware: Boolean
) {
    AMEDIACODEC_DIRECT(
        "Direct AMediaCodec + Mailbox",
        "Asynchronous callback, ANativeWindow zero-copy surface, Vulkan Mailbox pacing",
        isDirectHardware = true
    ),
    SYNCHRONOUS_LEGACY(
        "Synchronous Legacy Pipeline",
        "Blocking dequeueInput/Output, CPU memory buffer copy, FIFO presentation",
        isDirectHardware = false
    )
}

data class StreamConfig(
    val id: String = "default_stream",
    val name: String = "Synthetic Low-Latency 60 FPS",
    val protocol: StreamProtocol = StreamProtocol.TEST_SIGNAL,
    val codec: VideoCodec = VideoCodec.H264_AVC,
    val url: String = "synth://60fps/low-latency-clock",
    val targetFps: Int = 60,
    val lowLatencyMode: Boolean = true,
    val asyncCallbackMode: Boolean = true,
    val zeroCopySurface: Boolean = true,
    val pacingMode: PacingMode = PacingMode.VULKAN_MAILBOX,
    val initialJitterBufferMs: Int = 15,
    val nackRecoveryEnabled: Boolean = true,
    val fecRecoveryEnabled: Boolean = true,
    val architecture: PipelineArchitecture = PipelineArchitecture.AMEDIACODEC_DIRECT
)
