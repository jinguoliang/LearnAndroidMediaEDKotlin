package com.example.jinux.videoencodedecodekotlin.mediacodec

import android.media.MediaCodec
import java.nio.ByteBuffer

class MediaCodecOutputData(val index: Int, val buffer: ByteBuffer,
                           val bufferInfo: MediaCodec.BufferInfo,
                           private val mediaCodec: MediaCodec) {
    init {
        buffer.clear()
        buffer.position(bufferInfo.offset)
        buffer.limit(bufferInfo.size)
    }

    val size = bufferInfo.size
    val presentationTimeUs = bufferInfo.presentationTimeUs
    val flags = bufferInfo.flags
    val isEOS = bufferInfo.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM
    val format = mediaCodec.outputFormat

    fun giveBufferBack(): Unit {
        mediaCodec.releaseOutputBuffer(index, false)
    }
}