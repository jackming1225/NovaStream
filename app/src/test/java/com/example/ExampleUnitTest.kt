package com.example

import com.example.decoder.HardwareCodecScanner
import com.example.model.PacingMode
import com.example.network.AdaptiveJitterBuffer
import com.example.network.RtpPacket
import com.example.profiler.DumpsysGfxProfiler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testAdaptiveJitterBufferRfc3550Calculation() {
        val ajb = AdaptiveJitterBuffer()
        val now = System.currentTimeMillis()

        // Ingest sequence of RTP packets with jitter
        for (i in 0L..10L) {
            val packet = RtpPacket(
                sequenceNumber = i,
                timestampMs = now + i * 16L,
                payloadSize = 1420,
                isKeyFrame = i == 0L
            )
            ajb.onPacketReceived(packet, arrivalTimeMs = now + i * 16L + (if (i % 2L == 0L) 10L else 0L))
        }

        val stats = ajb.statsFlow.value
        assertEquals(11L, stats.totalPacketsReceived)
        assertTrue("Target delay should be dynamically sized above minimum", stats.targetDelayMs >= 10.0f)
    }

    @Test
    fun testVulkanPacingModes() {
        val mailbox = PacingMode.VULKAN_MAILBOX
        assertTrue(mailbox.isTearFree)
        assertEquals("VK_PRESENT_MODE_MAILBOX_KHR", mailbox.technicalName)

        val fifo = PacingMode.VULKAN_FIFO
        assertTrue(fifo.isTearFree)
        assertEquals(16.6f, fifo.typicalLatencyDeltaMs, 0.1f)
    }

    @Test
    fun testDumpsysProfilerFortyPercentReduction() {
        val profiler = DumpsysGfxProfiler()

        // Record a set of frames under optimized pipeline
        for (i in 0..100) {
            profiler.recordFrame(16.2f, isOptimized = true)
        }

        val metrics = profiler.dumpsysMetrics.value
        assertEquals(101, metrics.totalFramesTracked)
        assertTrue("Optimized frame drop rate should be below baseline", metrics.optimizedDropRate < metrics.baselineDropRate)
        assertTrue("Drop reduction percentage should reach at least 40%", metrics.dropReductionPercentage >= 40.0f)
    }

    @Test
    fun testHardwareCodecScanner() {
        val codecs = HardwareCodecScanner.scanCodecs()
        assertNotNull(codecs)
        assertTrue("Should return available or fallback codecs", codecs.isNotEmpty())
        assertTrue("First codec should have a name", codecs[0].codecName.isNotBlank())
    }
}
