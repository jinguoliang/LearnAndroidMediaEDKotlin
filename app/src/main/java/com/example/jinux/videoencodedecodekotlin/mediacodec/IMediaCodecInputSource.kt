package com.example.jinux.videoencodedecodekotlin.mediacodec

import android.media.MediaFormat
import java.nio.ByteBuffer

interface IMediaCodecInputSource {
    var trackFormat: MediaFormat?
    fun readData(byteBuffer: ByteBuffer): MediaCodecInputData
    fun release()
}