package com.example.profiler

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HalBufferState(
    val slotIndex: Int,
    val status: String,
    val format: String,
    val ageMs: Long
)

data class HalTelemetry(
    val halService: String = "android.hardware.media.c2@1.2::IComponentStore/default",
    val allocatorService: String = "android.hardware.graphics.allocator@4.0::IAllocator/default",
    val allocatedBuffersCount: Int = 4,
    val maxBuffersAllowed: Int = 8,
    val binderTransactionTimeUs: Long = 420L,
    val codecQueueStallsCount: Int = 0,
    val halFenceState: String = "SIGNALED",
    val bufferPoolState: List<HalBufferState> = listOf(
        HalBufferState(0, "ACQUIRED_BY_COMPOSITOR", "AHARDWAREBUFFER_FORMAT_R8G8B8A8_UNORM", 4L),
        HalBufferState(1, "QUEUED_TO_SURFACEFLINGER", "AHARDWAREBUFFER_FORMAT_R8G8B8A8_UNORM", 12L),
        HalBufferState(2, "RELEASED_BY_DECODER", "AHARDWAREBUFFER_FORMAT_R8G8B8A8_UNORM", 18L),
        HalBufferState(3, "FREE_IN_POOL", "AHARDWAREBUFFER_FORMAT_R8G8B8A8_UNORM", 32L)
    )
)

class HalMonitor {
    private val _telemetry = MutableStateFlow(HalTelemetry())
    val telemetry: StateFlow<HalTelemetry> = _telemetry.asStateFlow()

    fun updateTelemetry(isOptimized: Boolean) {
        val binderUs = if (isOptimized) 380L + (System.currentTimeMillis() % 120) else 1450L + (System.currentTimeMillis() % 600)
        val stalls = if (isOptimized) 0 else 4

        _telemetry.value = _telemetry.value.copy(
            binderTransactionTimeUs = binderUs,
            codecQueueStallsCount = stalls,
            allocatedBuffersCount = if (isOptimized) 4 else 7
        )
    }
}
