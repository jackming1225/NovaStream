package com.example.renderer

import android.view.Choreographer
import com.example.model.PacingMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicLong

data class FenceSyncStats(
    val vkFenceWaitDurationMs: Float = 0.8f,
    val eglSyncWaitDurationMs: Float = 0.4f,
    val acquireToPresentMs: Float = 1.2f,
    val presentationMode: PacingMode = PacingMode.VULKAN_MAILBOX,
    val activeSwapchainIndex: Int = 1,
    val totalSwapchainSlots: Int = 3,
    val isTearFree: Boolean = true
)

class FramePacingEngine(
    private var pacingMode: PacingMode = PacingMode.VULKAN_MAILBOX
) : Choreographer.FrameCallback {

    private val _fenceStats = MutableStateFlow(FenceSyncStats())
    val fenceStats: StateFlow<FenceSyncStats> = _fenceStats.asStateFlow()

    private val frameCount = AtomicLong(0)
    private var isRunning = false
    private var lastVsyncTimeNs: Long = 0L

    fun setPacingMode(mode: PacingMode) {
        this.pacingMode = mode
        updateFenceMetrics()
    }

    fun start() {
        if (!isRunning) {
            isRunning = true
            try {
                Choreographer.getInstance().postFrameCallback(this)
            } catch (_: Exception) {
                // Background thread fallback
            }
        }
    }

    fun stop() {
        isRunning = false
        try {
            Choreographer.getInstance().removeFrameCallback(this)
        } catch (_: Exception) {
            // Ignored
        }
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (!isRunning) return

        val deltaNs = if (lastVsyncTimeNs > 0) frameTimeNanos - lastVsyncTimeNs else 16_666_667L
        lastVsyncTimeNs = frameTimeNanos
        frameCount.incrementAndGet()

        updateFenceMetrics()

        if (isRunning) {
            try {
                Choreographer.getInstance().postFrameCallback(this)
            } catch (_: Exception) {
                // Ignored
            }
        }
    }

    /**
     * Simulates explicit sync fence release (VkFence / EGLSyncKHR) and swapchain latching
     */
    fun onFramePresented(frameIndex: Long): Float {
        val (fenceWaitMs, eglWaitMs) = when (pacingMode) {
            PacingMode.VULKAN_MAILBOX -> Pair(0.4f + (frameIndex % 3) * 0.1f, 0.2f)
            PacingMode.VULKAN_FIFO -> Pair(8.4f + (frameIndex % 5) * 0.3f, 4.2f)
            PacingMode.IMMEDIATE -> Pair(0.1f, 0.05f)
            PacingMode.CHOREOGRAPHER -> Pair(4.2f, 1.8f)
        }

        val acquireToPresentMs = fenceWaitMs + eglWaitMs + pacingMode.typicalLatencyDeltaMs

        _fenceStats.value = FenceSyncStats(
            vkFenceWaitDurationMs = fenceWaitMs,
            eglSyncWaitDurationMs = eglWaitMs,
            acquireToPresentMs = acquireToPresentMs,
            presentationMode = pacingMode,
            activeSwapchainIndex = (frameIndex % 3).toInt(),
            totalSwapchainSlots = 3,
            isTearFree = pacingMode.isTearFree
        )

        return acquireToPresentMs
    }

    private fun updateFenceMetrics() {
        val current = _fenceStats.value
        _fenceStats.value = current.copy(
            presentationMode = pacingMode,
            isTearFree = pacingMode.isTearFree
        )
    }
}
