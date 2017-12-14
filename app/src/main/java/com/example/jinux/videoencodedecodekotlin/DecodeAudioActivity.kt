package com.example.jinux.videoencodedecodekotlin

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.*
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.v7.app.AppCompatActivity
import com.example.jinux.videoencodedecodekotlin.mediacodec.*
import com.example.jinux.videoencodedecodekotlin.utils.log
import kotlinx.android.synthetic.main.activity_decode_audio.*
import org.jetbrains.anko.doAsync
import java.nio.ByteBuffer


class DecodeAudioActivity : AppCompatActivity() {
    private val REQ_CODE_PICK_SOUND_FILE = 0x332

    private val mime = "audio/mpeg"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_decode_audio)
        decode_mp3.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = mime
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(intent, REQ_CODE_PICK_SOUND_FILE)
        }
    }

    @SuppressLint("SwitchIntDef")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode === REQ_CODE_PICK_SOUND_FILE && resultCode === Activity.RESULT_OK) {
            if (data != null && data.data != null) {
                val audioFileUri = data.data
                log("audio file uri = $audioFileUri")

                playAudio(audioFileUri)
            }
        }
    }

    @SuppressLint("SwitchIntDef")
    private fun playAudio(audioFileUri: Uri) {
        doAsync {

            val audioSource = AudioInputSource(baseContext, audioFileUri)
            val player = AudioPlayer()
            val mediaCodec = MediaCodec.createDecoderByType(mime)
            val mediaCodecMachine = MediaCodecMachine(audioSource, mediaCodec, player)
            mediaCodecMachine.start()
        }
    }
}

class AudioPlayer : IMediaCodecOutputSource {
    val thread = HandlerThread("handler").apply { start() }
    val mHandler = Handler(thread.looper)

    var audioTrack: AudioTrack? = null

    override fun writeData(data: MediaCodecOutputData): Unit {
        if (audioTrack == null) {
            initPlayer(data.format)
        }

        writeAudioTrack(data)
    }

    private fun initPlayer(format: MediaFormat) {
        val minBufferSize = AudioTrack.getMinBufferSize(
                format.getInteger(MediaFormat.KEY_SAMPLE_RATE),
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT)
        log("audio track min buffer size = $minBufferSize")
        audioTrack = AudioTrack(AudioManager.STREAM_MUSIC,
                format.getInteger(MediaFormat.KEY_SAMPLE_RATE),
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT, minBufferSize, AudioTrack.MODE_STREAM)
        audioTrack!!.play()
    }

    private fun writeAudioTrack(data: MediaCodecOutputData) {
        mHandler.post {
            if (data.isEOS) {
                audioTrack?.stop()
                audioTrack?.release()
            } else {
                audioTrack?.write(data.buffer, data.size, AudioTrack.WRITE_BLOCKING)
                data.giveBufferBack()
            }
        }
    }
}

class AudioInputSource(ctx: Context, uri: Uri) : IMediaCodecInputSource {

    private val mime = "audio/mpeg"

    override var trackFormat: MediaFormat? = null;

    private var extractor: MediaExtractor = MediaExtractor()

    init {
        extractor.setDataSource(ctx, uri, null)
        log("track count = ${extractor.trackCount}")

        for (i in 0 until extractor.trackCount) {
            trackFormat = extractor.getTrackFormat(i)
            log("track format = $trackFormat")
            if (mime.equals(trackFormat?.getString(MediaFormat.KEY_MIME))) {
                extractor.selectTrack(i)
                break
            }
        }

        if (trackFormat == null) {
            log("not find the track for ${mime}")
        }
    }

    override fun readData(byteBuffer: ByteBuffer): MediaCodecInputData {
        val size = extractor.readSampleData(byteBuffer, 0)
        val data = MediaCodecInputData(byteBuffer, size, extractor.sampleTime * 1000)

        extractor.advance()

        return data
    }

    override fun release() {
        extractor.release()
    }
}

