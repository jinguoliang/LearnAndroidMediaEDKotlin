package com.example.jinux.videoencodedecodekotlin

import android.media.*
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.jinux.videoencodedecodekotlin.utils.log
import kotlinx.android.synthetic.main.activity_record_play.*
import org.jetbrains.anko.doAsync

/**
 * Created by Jinux on 2017/12/5 49 周.
 *
 * 录制声音 直接播放
 */
class RecordPlayActivity : AppCompatActivity() {

    val sampleRate = 11025
    val channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO
    val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_record_play)

        record_play.setOnClickListener {
            doAsync {
                val minBufferSize = AudioTrack.getMinBufferSize(
                        sampleRate, channelConfig, audioFormat);
                val track = AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, channelConfig, audioFormat,
                        minBufferSize * 2, AudioTrack.MODE_STREAM);
                track.play()
                log("play start");

                recordSound { data, size ->
                    playSound(track, data, size)
                }

                track.stop()
                log("play stop")
            }
        }
    }

    private fun playSound(track: AudioTrack, data: ByteArray, size: Int) {
        val writeSize = track.write(data, 0, size)
        log("play data ${writeSize}")
    }

    private fun recordSound(onDataArrivalListener: ((ByteArray, Int) -> Unit)) {
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        val recorder = AudioRecord(MediaRecorder.AudioSource.MIC,
                sampleRate, channelConfig, audioFormat, bufferSize * 2);
        recorder.startRecording()
        log("record start")

        val data = ByteArray(bufferSize * 2)

        while (!isDestroyed()) {
            val size = recorder.read(data, 0, bufferSize * 2)
            log("record data ${size}")
            onDataArrivalListener(data, size)
        }

        recorder.stop()
        log("record stop")
    }

}