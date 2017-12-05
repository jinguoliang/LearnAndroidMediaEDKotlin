package com.example.jinux.videoencodedecodekotlin

import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import com.example.jinux.videoencodedecodekotlin.R.id.logMsg
import kotlinx.android.synthetic.main.activity_decoder_gif.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.io.File
import java.io.FileInputStream


fun FileInputStream.readBytes(count: Int): List<Int> {
    return (0 until count).map { read() }
}

class DecoderGIFActivity : AppCompatActivity(), AnkoLogger {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_decoder_gif)

        val targetFile = File(getExternalFilesDir(Environment
                .DIRECTORY_PICTURES).path + "/test.gif")
//        val decoder = GifDecoder(InputSource.FileSource(targetFile))

        val stream = targetFile.inputStream()
        val (type, version) = readTypeVersion(stream)
        log("type: $type")
        log("version: $version")

        val size = readGIFSize(stream)
        log("the GIF size: (${size.first}, ${size.second})")

        val packedData = readPackedData(stream)

        val colorTableByteSize = Math.pow(2.0, packedData.globalColorTableSize.toDouble() + 1).toInt()

        log("\n")
        log("has global color table = ${packedData.hasGlobalColorTable}")
        log("color resolution = ${packedData.colorResolution}")
        log("is Sort = ${packedData.isSort}")
        log("global color table size = ${colorTableByteSize}")

        val backgroundColorIndex = stream.readBytes(1)[0]
        log("backgroundColorIndex: $backgroundColorIndex")

        val aspectRatio = stream.readBytes(1)[0]
        log("aspectRatio: $aspectRatio")

        // 读取 global color table 并绘制
        val globalColorTableData = readColorTableData(stream, colorTableByteSize)
        globalColorTable.setData(globalColorTableData)

        var blockIndictor = stream.readBytes(1)[0]
        log("blockIndictor: 0x${Integer.toHexString(blockIndictor)}")
        while (blockIndictor != -1) {
            if (blockIndictor == 0x21) {

                val label = stream.readBytes(1)[0]
                log("label: 0x${Integer.toHexString(label)}")

                if (label == 0xF9) {
                    val graphicControl = readGraphicControlExtension(stream)
                    log("delay: ${graphicControl.delay}")
                    log(msg = "transparentColorIndex: ${graphicControl.transparentColprIndex}")


                } else if (label == 0xFF) {
                    val applicationExtension = readApplicationExtension(stream)
                    log("application extension = ${applicationExtension.application}")
                } else {
                    log("next 0x${Integer.toHexString(label)}")
                }
            } else if (blockIndictor == 0x2c) {
                val left = readShort(stream)
                val top = readShort(stream)
                val width = readShort(stream)
                val height = readShort(stream)
                // ignore packed field
                val packed = stream.readBytes(1)[0]
                val hasLocalColorTable = (packed and 0x80) shr 7 == 1
                log("image pos: ($left, ${top})")
                log("image size: ($width, ${height})")
                log("image has local color table: $hasLocalColorTable")
                if (hasLocalColorTable) {
                    // 读取 color table
                }
                val imageData = mutableListOf<Int>()
                val lzwMinCodeSize = stream.readBytes(1)[0]
                var terminal = stream.readBytes(1)[0]
                var size = 0
                while (terminal != 0x00) {
                    size = terminal
                    imageData.addAll(stream.readBytes(size))
                    terminal = stream.readBytes(1)[0]
                }
                var decompressedData = lzwDecoder(imageData, lzwMinCodeSize)

            } else {
                break
            }

            blockIndictor = stream.readBytes(1)[0]
            log("blockIndictor: 0x${Integer.toHexString(blockIndictor)}")
        }

        log("end")
//        val code =
//        log("next block code: $code")

//        log("num: ${decoder.numberOfFrames}");
//        log("duration: ${decoder.duration}");
//        log("width: ${decoder.width}");
//        log("height: ${decoder.height}");
//        log("loopCount: ${decoder.loopCount}")
//        log("comment: ${decoder.comment}")
//
//
//
//        doAsync {
//            (0 until decoder.numberOfFrames).forEach { i ->
//                val b = Bitmap.createBitmap(decoder.width, decoder.height, Bitmap.Config.ARGB_8888)
//                decoder.seekToFrame(i, b)
//                uiThread {
//                    val iv = ImageView(this@DecoderGIFActivity)
//                    iv.setImageBitmap(b)
//                    container.addView(iv)
//                }
//            }
//
//        }

    }

    private fun lzwDecoder(imageData: List<Int>, lzwMinCodeSize: Int): MutableList<Int> {
        val CC = Math.pow(2.toDouble(), lzwMinCodeSize.toDouble()).toInt()
        val EOI = CC + 1
        var table = mutableMapOf<Int, List<Int>>()
        for (i in 0..EOI) {
            table[i] = listOf(i)
        }

        var out = mutableListOf<Int>()

        var CODE = imageData[1]
        out.add(CODE)

        var K = 0
        for (code in imageData.slice(2..imageData.size - 1)) {
            CODE = code

            if (table[CODE] == null) {
                K = 2
            } else {
                out.addAll(table[CODE]!!)
                K = table[CODE]!![0]
                table.put(table.size, table[CODE - 1]!!)
            }


        }
        return out;
    }

    data class ApplicationExtension(val application: String, val data: List<Int>)

    private fun readApplicationExtension(stream: FileInputStream): ApplicationExtension {
        var size = stream.readBytes(1)[0]
        val text = stream.readBytes(size).joinToString(separator = "") { it.toChar().toString() }
        var terminal = stream.readBytes(1)[0]
        while (terminal != 0x00) {
            size = terminal
            stream.readBytes(size)
            terminal = stream.readBytes(1)[0]
        }
        return ApplicationExtension(text, listOf())
    }

    data class GraphicControlExtention(val disposal: Int,
                                       val userInputFlag: Int,
                                       val transparentColorFlag: Int,
                                       val delay: Int,
                                       val transparentColprIndex: Int
    )

    private fun readGraphicControlExtension(stream: FileInputStream): GraphicControlExtention {
        val nextSize = stream.readBytes(1)[0]
        val graphicControlBlock = stream.readBytes(nextSize)
        val packedField = graphicControlBlock[0]
        val disposal = (packedField and 0x1c) shr 2
        val inputFlag = (packedField and 0x2) shr 1
        val transparentColorFlag = packedField and 0x1
        val delay = graphicControlBlock[2] * 0xff + graphicControlBlock[1]
        val transparentColorIndex = graphicControlBlock[3]
        val terminal = stream.readBytes(1)[0]
        if (terminal == 0x00) {
            log("read Graphic Control Extension finish")
        } else {
            log("read Graphic Control Extension error")
        }
        return GraphicControlExtention(disposal, inputFlag, transparentColorFlag, delay, transparentColorIndex)
    }

    private fun readColorTableData(stream: FileInputStream, colorTableByteSize: Int): List<Int> {
        val readColor = {
            val rgb = stream.readBytes(3).map { it.toInt() }
            Color.rgb(rgb[0], rgb[1], rgb[2])
        }

        return (0 until colorTableByteSize).map { readColor() }.toList()
    }

    data class PackedData(val hasGlobalColorTable: Boolean,
                          val colorResolution: Int,
                          val isSort: Boolean,
                          val globalColorTableSize: Int)

    private fun readPackedData(stream: FileInputStream): PackedData {
        val packedField = stream.readBytes(1)[0]
        val hasGlobalColorTable = (packedField.toInt() and 0x80) shr 7 == 1
        val colorResolution = (packedField.toInt() and 0x70) shr 4
        val isSort = (packedField.toInt() and 0x8) shr 3 == 1
        val globalColorTableSize = (packedField.toInt() and 0x7) as Int
        return PackedData(hasGlobalColorTable, colorResolution, isSort, globalColorTableSize)
    }

    private fun readGIFSize(input: FileInputStream): Pair<Int, Int> {
        return readShort(input) to readShort(input)
    }

    private fun readShort(input: FileInputStream): Int {
        return input.readBytes(2).foldRight(0) { initial, cur ->
            initial * 0xff + cur
        }
    }

    private fun readTypeVersion(input: FileInputStream): Pair<String, String> {
        return input.readBytes(3).joinToString(separator = "") { it.toChar().toString() } to input.readBytes(3)
                .joinToString(separator = "") { it.toChar().toString() }
    }

    fun log(msg: String) {
        runOnUiThread {
            logMsg.text = logMsg.text.toString() + "$msg\n"
        }
        info { msg }
    }
}