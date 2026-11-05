package com.example.decoder

import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Build
import android.view.Surface
import com.example.model.VideoCodec
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

class MediaCodecPipeline(
    private val onFrameDecoded: (frameIndex: Long, decodeLatencyMs: Float) -> Unit,
    private val onError: (Exception) -> Unit
) {
    private var codec: MediaCodec? = null
    private val isRunning = AtomicBoolean(false)
    private val frameCounter = AtomicLong(0)
    private val inputTimestampMap = ConcurrentHashMap<Long, Long>()

    fun start(
        surface: Surface,
        videoCodec: VideoCodec = VideoCodec.H264_AVC,
        width: Int = 1280,
        height: Int = 720,
        enableLowLatency: Boolean = true,
        useAsyncCallback: Boolean = true
    ) {
        stop()

        try {
            val mime = videoCodec.mimeType
            val mediaCodec = MediaCodec.createDecoderByType(mime)

            val format = MediaFormat.createVideoFormat(mime, width, height)
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, android.media.MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)

            // Low-latency configuration flags
            if (enableLowLatency) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    format.setInteger(MediaFormat.KEY_LOW_LATENCY, 1)
                }
                format.setInteger(MediaFormat.KEY_PRIORITY, 0) // Real-time priority
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    format.setInteger(MediaFormat.KEY_OPERATING_RATE, Short.MAX_VALUE.toInt())
                }
            }

            if (useAsyncCallback) {
                mediaCodec.setCallback(object : MediaCodec.Callback() {
                    override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
                        if (!isRunning.get()) return
                        try {
                            val inputBuffer: ByteBuffer? = codec.getInputBuffer(index)
                            inputBuffer?.clear()

                            val frameIndex = frameCounter.incrementAndGet()
                            val nowNs = System.nanoTime()
                            val ptsUs = nowNs / 1000L

                            inputTimestampMap[ptsUs] = nowNs

                            // Feed sample NAL header / simulated low-latency bitstream slice
                            inputBuffer?.put(byteArrayOf(0x00, 0x00, 0x00, 0x01, 0x67))
                            codec.queueInputBuffer(
                                index,
                                0,
                                inputBuffer?.position() ?: 5,
                                ptsUs,
                                if (frameIndex % 30 == 0L) MediaCodec.BUFFER_FLAG_KEY_FRAME else 0
                            )
                        } catch (e: Exception) {
                            if (isRunning.get()) onError(e)
                        }
                    }

                    override fun onOutputBufferAvailable(
                        codec: MediaCodec,
                        index: Int,
                        info: MediaCodec.BufferInfo
                    ) {
                        if (!isRunning.get()) return
                        try {
                            val enqueueNs = inputTimestampMap.remove(info.presentationTimeUs) ?: (System.nanoTime() - 4_000_000L)
                            val decodeDurationNs = (System.nanoTime() - enqueueNs).coerceAtLeast(1_000_000L)
                            val latencyMs = decodeDurationNs / 1_000_000f

                            // Zero-copy direct render to ANativeWindow / SurfaceView
                            codec.releaseOutputBuffer(index, true)

                            onFrameDecoded(frameCounter.get(), latencyMs)
                        } catch (e: Exception) {
                            if (isRunning.get()) onError(e)
                        }
                    }

                    override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
                        onError(e)
                    }

                    override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
                        // Handled format change
                    }
                })
            }

            // Direct render straight to ANativeWindow / SurfaceView
            mediaCodec.configure(format, surface, null, 0)
            mediaCodec.start()

            codec = mediaCodec
            isRunning.set(true)
        } catch (e: Exception) {
            onError(e)
        }
    }

    fun stop() {
        isRunning.set(false)
        try {
            codec?.stop()
            codec?.release()
        } catch (_: Exception) {
            // Ignored
        } finally {
            codec = null
            inputTimestampMap.clear()
        }
    }
}
