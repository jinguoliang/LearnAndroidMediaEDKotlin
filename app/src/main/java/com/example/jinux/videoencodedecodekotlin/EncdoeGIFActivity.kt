package com.example.jinux.videoencodedecodekotlin

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_encode_gif.*
import org.jetbrains.anko.*
import android.provider.MediaStore
import java.io.File


/**
 * Created by Jinux on 2017/11/1 44 周.
 *
 * 演示 GIF 编码
 */
class EncdoeGIFActivity : AppCompatActivity(), AnkoLogger {
    private val REQUEST_CODE_SELECT_PICS = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_encode_gif)

        selectPics.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/png"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(intent, REQUEST_CODE_SELECT_PICS)
        }

        createGIF.setOnClickListener {
            if (mUris == null) {
                log("Please select images")
            } else {
                val gifEncoder = AnimatedGifEncoder()
                val targetFile = File(getExternalFilesDir(Environment
                        .DIRECTORY_PICTURES).path + "/test.gif")
                if (targetFile.exists()) {
                    targetFile.delete()
                }

                val created = targetFile.createNewFile()
                if (!created) {
                    log("not created file")
                    return@setOnClickListener
                }

                val started = gifEncoder.start(targetFile.path)
                val quality = qualityEdit.text.toString().toInt()
                log("quality: $quality")
                gifEncoder.setQuality(quality)
                val repeat = repeatEdit.text.toString().toInt()
                log("repeat: $repeat")
                gifEncoder.setRepeat(repeat)
                val delay = delayEdit.text.toString().toInt()
                log("delay: $delay")
                gifEncoder.setDelay(delay)
                val (w, h) = wxh.text.toString().split("x").map { it.trim().toInt() }
                log("wxh: $w x $h")
                if (w != 0 && h != 0) {
                    gifEncoder.setSize(w, h)
                }
                gifEncoder.setComment(commentEdit.text.toString())

                if (!started) {
                    log("not started")
                    return@setOnClickListener
                }

                log("start encoding:")
                doAsync {
                    mUris!!.map { getFilePathFromUri(it) }.forEachIndexed { i, it ->

                        val option = BitmapFactory.Options()
                        option.inScaled = true;
                        option.inSampleSize = 4;
                        val b = BitmapFactory.decodeFile(it, option)
                        val added = gifEncoder.addFrame(b)
                        log("encode Frame[$i]: $added")
                    }
                    val finished = gifEncoder.finish()
                    if (finished) {
                        log("finished")
                        uiThread {
                            gifView.setImageURI(Uri.fromFile(targetFile))
                        }
                    } else {
                        log("finish error")
                    }
                }
            }
        }
    }

    private fun getFilePathFromUri(uri: Uri): String {
        log("image uri: $uri")
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, filePathColumn, null, null, null)
        cursor.moveToFirst()
        val path = cursor.getString(cursor.getColumnIndex(filePathColumn[0]))
        log("image path: $path")
        cursor.close()

        return path
    }

    private var mUris: List<Uri>? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SELECT_PICS) {
            if (resultCode == Activity.RESULT_OK) {
                val itemCount = data?.clipData?.itemCount ?: 0
                mUris = (0 until itemCount).map { data?.clipData?.getItemAt(it) }.map { it?.uri!! }
            }
        }
    }

    fun log(msg: String) {
        runOnUiThread {
            logMsg.append("$msg\n")
        }
        info { msg }
    }
}