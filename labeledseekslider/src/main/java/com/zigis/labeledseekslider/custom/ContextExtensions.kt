package com.zigis.labeledseekslider.custom

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

@Suppress("DEPRECATION")
fun Context.vibrate(duration: Long) {
    val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (Build.VERSION.SDK_INT >= 26) {
        vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        vibrator.vibrate(duration)
    }
}