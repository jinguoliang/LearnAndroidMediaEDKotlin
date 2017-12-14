package com.example.jinux.videoencodedecodekotlin.mediacodec

interface IMediaCodecOutputSource {
    fun writeData(data: MediaCodecOutputData): Unit
}