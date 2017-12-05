package com.example.jinux.videoencodedecodekotlin

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import org.jetbrains.anko.dip

/**
 * Created by Jinux on 2017/11/14 46 周.
 */

class ColorTableView : View {
    private val mPaint = Paint()
    private val mLinePaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = dip(1).toFloat()
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = mData?.size ?: 0
        if (size == 0) {
            return
        }

        val cellWidth = MeasureSpec.getSize(widthMeasureSpec) / COUNT_IN_LINE
        val lineCount = getColorTableLineCount(mData!!.size)
        val tableHeight = cellWidth * lineCount
        setMeasuredDimension(getDefaultSize(suggestedMinimumWidth, widthMeasureSpec),
                getDefaultSize(tableHeight, heightMeasureSpec))

    }

    val COUNT_IN_LINE = 16

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val size = mData?.size ?: 0
        if (size > 0) {
            val lineCount = getColorTableLineCount(size)
            val cellWidth = width / COUNT_IN_LINE
            for (rowNo in 0 until lineCount) {
                for (columnNo in 0 until COUNT_IN_LINE) {
                    mPaint.color = mData!![COUNT_IN_LINE * rowNo + columnNo]
                    canvas.drawRect(columnNo * cellWidth.toFloat(),
                            rowNo * cellWidth.toFloat(),
                            (columnNo + 1) * cellWidth.toFloat(),
                            (rowNo + 1) * cellWidth.toFloat(), mPaint)
                    canvas.drawRect(columnNo * cellWidth.toFloat(),
                            rowNo * cellWidth.toFloat(),
                            (columnNo + 1) * cellWidth.toFloat(),
                            (rowNo + 1) * cellWidth.toFloat(), mLinePaint)
                }
            }
        }
    }

    private fun getColorTableLineCount(size: Int) =
            size / COUNT_IN_LINE + (if (size % COUNT_IN_LINE == 0) 0 else 1)

    private var mData: List<Int>? = null

    fun setData(globalColorTableData: List<Int>) {
        mData = globalColorTableData
        invalidate()
    }
}
