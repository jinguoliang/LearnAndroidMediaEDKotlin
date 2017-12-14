package com.example.jinux.videoencodedecodekotlin.mediacodec

import android.annotation.SuppressLint
import android.media.MediaCodec
import com.example.jinux.videoencodedecodekotlin.utils.log

class MediaCodecMachine(var inputSource: IMediaCodecInputSource,
                        val mediaCodec: MediaCodec,
                        var outputSource: IMediaCodecOutputSource) {


    private var worker: Worker

    init {

        worker = Worker()
    }

    fun start(): Unit {
        mediaCodec.configure(inputSource.trackFormat, null, null, 0)
        log("output format: ${mediaCodec.outputFormat}")

        mediaCodec.start()
        worker.start()
    }

    inner class Worker : Thread() {
        @SuppressLint("SwitchIntDef")
        override fun run() {
            super.run()
            val inputBuffers = mediaCodec.inputBuffers
            val outBuffers = mediaCodec.outputBuffers

            val decodeBufferInfo = MediaCodec.BufferInfo()

            while (true) {
                val index = mediaCodec.dequeueInputBuffer(-1)
                if (index < 0) {
                    log("no input buffer")
                    continue
                }

                val inputBuffer = inputBuffers[index]
                inputBuffer.clear()

                val audioData = inputSource.readData(inputBuffer)
                log("extract data: ${audioData.size}")
                if (audioData.size < 0) {
                    log("no input data")
                    break;
                }
                mediaCodec.queueInputBuffer(index, 0, audioData.size,
                        audioData.prsentationTimeUs,
                        0)

                var outIndex = mediaCodec.dequeueOutputBuffer(decodeBufferInfo, 10000)
                log("output format: ${mediaCodec.outputFormat}")
                when (outIndex) {
                    MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        log("out format chagned")
                    }
                    MediaCodec.INFO_TRY_AGAIN_LATER -> {
                        log("try again later")
                    }
                    else -> {
                        while (outIndex >= 0) {
                            val outputBuffer = outBuffers[outIndex]

                            val decodeData = MediaCodecOutputData(outIndex, outputBuffer,
                                    decodeBufferInfo, mediaCodec)

                            if (decodeData.isEOS) {
                                log("end of stream")
                                mediaCodec.stop()
                                mediaCodec.release()
                                inputSource.release()
                            }

                            outputSource.writeData(decodeData)

                            log("the out buffer info: ${decodeBufferInfo.offset}/${decodeBufferInfo
                                    .size} -- ${decodeBufferInfo.presentationTimeUs}")

                            outIndex = mediaCodec.dequeueOutputBuffer(decodeBufferInfo, 10000)
                        }
                    }
                }


            }
        }
    }
}

