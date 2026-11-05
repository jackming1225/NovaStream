package com.example

import com.example.model.StreamConfig
import com.example.model.StreamProtocol
import com.example.model.VideoCodec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StreamConfigTest {

    @Test
    fun testDefaultStreamConfiguration() {
        val config = StreamConfig(
            id = "test_stream",
            name = "RTSP Live Test",
            protocol = StreamProtocol.RTSP,
            codec = VideoCodec.H264_AVC,
            url = "rtsp://10.0.0.1:8554/live"
        )

        assertEquals("test_stream", config.id)
        assertEquals(StreamProtocol.RTSP, config.protocol)
        assertEquals(VideoCodec.H264_AVC, config.codec)
        assertTrue(config.lowLatencyMode)
        assertTrue(config.asyncCallbackMode)
        assertTrue(config.zeroCopySurface)
    }
}
