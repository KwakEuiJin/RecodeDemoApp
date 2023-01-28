package com.kej.recodedemoapp

import android.os.Handler
import android.os.Looper
import android.util.Log

class Timer(onSoundCallBack: (Long) -> Unit) {
    private var duration = 0L
    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object :Runnable {
        override fun run() {
            duration += 40L
            handler.postDelayed(this, 40L)
            onSoundCallBack(duration)
        }
    }

    fun start() {
        handler.postDelayed(runnable, 40L)
    }

    fun stop() {
        handler.removeCallbacks(runnable)
    }

}