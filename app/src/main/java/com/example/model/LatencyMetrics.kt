package com.example.model

data class ConsoleLog(
    val timestamp: String,
    val tag: String,
    val message: String,
    val level: LogLevel = LogLevel.INFO
)

enum class LogLevel {
    DEBUG, INFO, WARN, ERROR
}

data class LatencyMetrics(
    val fps: Float = 60.0f,
    val targetFps: Int = 60,
    val decodeLatencyMs: Float = 4.2f,
    val networkJitterMs: Float = 6.5f,
    val surfaceCompositionMs: Float = 2.1f,
    val gpuPresentFenceMs: Float = 1.4f,
    val totalLatencyMs: Float = 14.2f,
    val frameDropRate: Float = 0.4f,
    val totalFramesRendered: Long = 0L,
    val totalFramesDropped: Long = 0L,
    val jankCount: Int = 0,
    val bitrateMbps: Float = 8.5f,
    val frameTimeHistory: List<Float> = emptyList(),
    val isOptimizationActive: Boolean = true
)

data class JitterBufferStats(
    val currentBufferDelayMs: Float = 12.4f,
    val targetDelayMs: Float = 15.0f,
    val measuredJitterMs: Float = 4.8f,
    val jitterStdDevMs: Float = 2.1f,
    val packetLossPercent: Float = 0.5f,
    val totalPacketsReceived: Long = 0L,
    val totalPacketsLost: Long = 0L,
    val nackRequestsSent: Long = 0L,
    val nackRecoveredPackets: Long = 0L,
    val fecRecoveredPackets: Long = 0L,
    val underrunCount: Int = 0,
    val overrunCount: Int = 0,
    val jitterHistory: List<Float> = emptyList()
)

data class CodecCapability(
    val codecName: String,
    val mimeType: String,
    val isEncoder: Boolean,
    val isHardwareAccelerated: Boolean,
    val isSoftwareOnly: Boolean,
    val isVendorProvided: Boolean,
    val supportsLowLatency: Boolean,
    val maxResolution: String,
    val maxFrameRate: Int,
    val colorFormats: List<String>
)

data class DumpsysMetrics(
    val totalFramesTracked: Int = 1840,
    val jankyFramesCount: Int = 14,
    val percentile50Ms: Float = 8.1f,
    val percentile90Ms: Float = 12.3f,
    val percentile95Ms: Float = 14.8f,
    val percentile99Ms: Float = 16.4f,
    val missedVsyncCount: Int = 3,
    val surfaceFlingerLatencyMs: Float = 3.2f,
    val halBinderLatencyMs: Float = 0.65f,
    val halQueueDepth: Int = 1,
    val baselineDropRate: Float = 14.8f,
    val optimizedDropRate: Float = 0.8f,
    val dropReductionPercentage: Float = 42.5f
)
