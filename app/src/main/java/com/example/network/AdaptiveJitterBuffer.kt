package com.example.network

import com.example.model.JitterBufferStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

data class RtpPacket(
    val sequenceNumber: Long,
    val timestampMs: Long,
    val payloadSize: Int,
    val isKeyFrame: Boolean,
    val isFecParity: Boolean = false,
    val payload: ByteArray = ByteArray(0)
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as RtpPacket
        return sequenceNumber == other.sequenceNumber
    }

    override fun hashCode(): Int = sequenceNumber.hashCode()
}

class AdaptiveJitterBuffer(
    private var minTargetDelayMs: Float = 10.0f,
    private var maxTargetDelayMs: Float = 120.0f
) {
    private val packetQueue = java.util.concurrent.ConcurrentSkipListMap<Long, RtpPacket>()

    private var lastSequenceNumber: Long = -1L
    private var lastTransitTimeMs: Long = 0L
    private var measuredJitterMs: Float = 5.0f
    private var targetDelayMs: Float = 15.0f

    private var totalPacketsReceived: Long = 0L
    private var totalPacketsLost: Long = 0L
    private var nackRequestsSent: Long = 0L
    private var nackRecoveredPackets: Long = 0L
    private var fecRecoveredPackets: Long = 0L
    private var underrunCount: Int = 0
    private var overrunCount: Int = 0

    private val jitterHistory = ArrayDeque<Float>()
    private val maxHistorySize = 40

    private val _statsFlow = MutableStateFlow(JitterBufferStats())
    val statsFlow: StateFlow<JitterBufferStats> = _statsFlow.asStateFlow()

    fun reset() {
        packetQueue.clear()
        lastSequenceNumber = -1L
        lastTransitTimeMs = 0L
        measuredJitterMs = 5.0f
        targetDelayMs = 15.0f
        totalPacketsReceived = 0L
        totalPacketsLost = 0L
        nackRequestsSent = 0L
        nackRecoveredPackets = 0L
        fecRecoveredPackets = 0L
        underrunCount = 0
        overrunCount = 0
        jitterHistory.clear()
        publishStats()
    }

    fun onPacketReceived(
        packet: RtpPacket,
        arrivalTimeMs: Long = System.currentTimeMillis(),
        nackEnabled: Boolean = true,
        fecEnabled: Boolean = true
    ) {
        totalPacketsReceived++

        // RFC 3550 Inter-arrival jitter calculation
        val transit = arrivalTimeMs - packet.timestampMs
        if (lastTransitTimeMs != 0L) {
            val d = abs(transit - lastTransitTimeMs).toFloat()
            measuredJitterMs += (d - measuredJitterMs) / 16.0f
        }
        lastTransitTimeMs = transit

        // Update adaptive target buffer delay
        targetDelayMs = (measuredJitterMs * 2.2f + 8.0f).coerceIn(minTargetDelayMs, maxTargetDelayMs)

        // Check for sequence gaps (Packet loss & NACK trigger)
        if (lastSequenceNumber >= 0 && packet.sequenceNumber > lastSequenceNumber + 1) {
            val lostCount = packet.sequenceNumber - lastSequenceNumber - 1
            totalPacketsLost += lostCount

            if (nackEnabled) {
                nackRequestsSent += lostCount
                // 85% simulated NACK recovery rate on standard WiFi/5G
                nackRecoveredPackets += (lostCount * 0.85f).toLong().coerceAtLeast(1L)
            }

            if (fecEnabled && packet.isFecParity) {
                // Parity packet reconstructed missing sequence
                fecRecoveredPackets += 1
            }
        }

        if (packet.sequenceNumber > lastSequenceNumber) {
            lastSequenceNumber = packet.sequenceNumber
        }

        // Store into jitter buffer
        packetQueue[packet.sequenceNumber] = packet

        // Manage buffer overrun
        if (packetQueue.size > 60) {
            overrunCount++
            packetQueue.pollFirstEntry()
        }

        // Update history
        jitterHistory.addLast(measuredJitterMs)
        if (jitterHistory.size > maxHistorySize) {
            jitterHistory.removeFirst()
        }

        publishStats()
    }

    fun dequeueNextPacket(currentTimeMs: Long): RtpPacket? {
        val firstEntry = packetQueue.firstEntry() ?: run {
            underrunCount++
            publishStats()
            return null
        }

        val packet = firstEntry.value
        val ageInBufferMs = currentTimeMs - packet.timestampMs

        // If the packet has met the dynamic target jitter delay (or buffer is building up), release to decoder
        if (ageInBufferMs >= targetDelayMs || packetQueue.size > 20) {
            packetQueue.remove(firstEntry.key)
            publishStats()
            return packet
        }

        return null
    }

    private fun publishStats() {
        val lossPct = if (totalPacketsReceived + totalPacketsLost > 0) {
            (totalPacketsLost.toFloat() / (totalPacketsReceived + totalPacketsLost).toFloat()) * 100f
        } else {
            0.0f
        }

        _statsFlow.value = JitterBufferStats(
            currentBufferDelayMs = (packetQueue.size * 16.6f).coerceAtMost(100f),
            targetDelayMs = targetDelayMs,
            measuredJitterMs = measuredJitterMs,
            jitterStdDevMs = (measuredJitterMs * 0.35f),
            packetLossPercent = lossPct,
            totalPacketsReceived = totalPacketsReceived,
            totalPacketsLost = totalPacketsLost,
            nackRequestsSent = nackRequestsSent,
            nackRecoveredPackets = nackRecoveredPackets,
            fecRecoveredPackets = fecRecoveredPackets,
            underrunCount = underrunCount,
            overrunCount = overrunCount,
            jitterHistory = jitterHistory.toList()
        )
    }
}
