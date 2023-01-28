package com.kej.recodedemoapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.max

class WaveFormView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val redPaint = Paint().apply {
        color = Color.RED
    }
    private val ampList = mutableListOf<Float>()
    private val rectList = mutableListOf<RectF>()
    private val rectWidth = 10f
    private var tick = 0


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        for (rectF in rectList) {
            canvas?.drawRect(rectF, redPaint)

        }
    }

    fun addAmplitude(maxAmplitude: Float) {
        ampList.add(maxAmplitude)
        rectList.clear()

        val maxWidth = this.width / rectWidth

        val amps = ampList.takeLast(maxWidth.toInt())

        for ((i, amp) in amps.withIndex()) {
            val rectF = RectF()
            rectF.top = (this.height / 2) - amp / 2 + 5
            rectF.bottom = rectF.top + amp + 5
            rectF.left = i * rectWidth
            rectF.right = rectF.left + (rectWidth - 5f)
            rectList.add(rectF)
        }

        invalidate()
    }

    fun replayAmplitude(duration: Int) {
        rectList.clear()
        val maxWidth = (this.width / rectWidth).toInt()
        val amps = ampList.take(tick).takeLast(maxWidth)

        for ((i, amp) in amps.withIndex()) {
            val rectF = RectF()
            rectF.top = (this.height / 2) - amp / 2 + 5
            rectF.bottom = rectF.top + amp + 5
            rectF.left = i * rectWidth
            rectF.right = rectF.left + (rectWidth - 5f)
            rectList.add(rectF)
        }
        tick++
        invalidate()
    }

    fun clearData(){
        ampList.clear()
    }

    fun clearWave() {
        rectList.clear()
        tick = 0
        invalidate()
    }
}