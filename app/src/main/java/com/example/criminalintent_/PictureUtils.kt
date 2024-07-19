package com.example.criminalintent_

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.os.Build
import android.util.Size
import android.view.WindowInsets
import android.view.WindowMetrics


fun getScaledBitmap(path: String, activity: Activity): Bitmap {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val metrics: WindowMetrics = activity.windowManager.currentWindowMetrics

        // Gets all excluding insets
        val windowInsets = metrics.windowInsets
        val insets = windowInsets.getInsetsIgnoringVisibility(
            WindowInsets.Type.navigationBars() or WindowInsets.Type.displayCutout()
        )

        val insetsWidth: Int = insets.right + insets.left
        val insetsHeight: Int = insets.top + insets.bottom

        // Legacy size that Display#getSize reports
        val bounds = metrics.bounds
        val size = Size(
            bounds.width() - insetsWidth,
            bounds.height() - insetsHeight
        )
        return getScaledBitmap(path, size.width, size.height)
    } else {
        val size = Point()
        activity.windowManager.defaultDisplay.getSize(size)
        return getScaledBitmap(path, size.x, size.y)
    }
}

fun getScaledBitmap(path: String, destWidth: Int, destHeight: Int): Bitmap {

    var options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(path, options)
    val srcWidth = options.outWidth.toFloat()
    val srcHeight = options.outHeight.toFloat()

    var inSampleSize = 1
    if (srcHeight > destHeight || srcWidth > destWidth) {
        val heightScale = srcHeight / destHeight
        val widthScale = srcWidth / destWidth
        val sampleScale = if (heightScale > widthScale
        ) {
            heightScale
        } else {
            widthScale
        }
        inSampleSize = Math.round(sampleScale)
    }
    options = BitmapFactory.Options()
    options.inSampleSize = inSampleSize

    return BitmapFactory.decodeFile(path, options)
}




