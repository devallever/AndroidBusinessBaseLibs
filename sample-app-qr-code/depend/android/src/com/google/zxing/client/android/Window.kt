package com.google.zxing.client.android

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.BitArray
import java.io.ByteArrayOutputStream
import android.R.attr.bitmap
import android.R.attr.data
import android.graphics.*
import com.google.zxing.LuminanceSource


class Window(val context: Context) {

    private val mWindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var mView: View? = null


    init {

    }


    private fun getLayoutParams(x: Int, y: Int, w: Int, h: Int): WindowManager.LayoutParams {
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.flags = (WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                or WindowManager.LayoutParams.FLAG_FULLSCREEN
                or WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        // 这里增加硬件加速，在自定义view中使用ViewDragHelper处理滑动时发现不自动调用computeScroll
        layoutParams.flags = layoutParams.flags or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED

        layoutParams.format = PixelFormat.TRANSLUCENT
        layoutParams.gravity = Gravity.TOP
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT <= 24) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_TOAST
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE
        }

        layoutParams.width = w
        layoutParams.height = h
        layoutParams.x = x
        layoutParams.y = y
        return layoutParams
    }

    fun showBitmap(data: ByteArray, w: Int, h: Int) {
        var view: ImageView? = null
        if (mView == null) {
            view = ImageView(context)
            view.setBackgroundColor(Color.RED)
            mView = view
            val lp = getLayoutParams(0, 0, w, h)
            show(mView!!, lp)
        } else {
            view = mView as? ImageView
        }
        if (view != null) {
//            val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
            val newOpts = BitmapFactory.Options()
            newOpts.inJustDecodeBounds = true
            val yuvimage = YuvImage(
                    data,
                    ImageFormat.NV21,
                    w,
                    h,
                    null)
            var baos = ByteArrayOutputStream()
            yuvimage.compressToJpeg(Rect(0, 0, w, h), 100, baos)// 80--JPG图片的质量[0-100],100最高
            var rawImage = baos.toByteArray()
            //将rawImage转换成bitmap
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.RGB_565
            val matrix = Matrix()
            // 缩放原图
            matrix.postScale(1f, 1f)
            // 向左旋转45度，参数为正则向右旋转
            matrix.postRotate(90f)
            val bitmap = BitmapFactory.decodeByteArray(rawImage, 0, rawImage.size, options)
            view.setImageBitmap(bitmap)
        }
    }

    fun showSource(source: LuminanceSource, w: Int, h: Int) {
        var view: ImageView? = null
        if (mView == null) {
            view = ImageView(context)
            view.setBackgroundColor(Color.RED)
            mView = view
            val lp = getLayoutParams(0, 0, w, h)
            show(mView!!, lp)
        } else {
            view = mView as? ImageView
        }
        if (view != null) {
            var bitmap: Bitmap? = null
            if (source is PlanarYUVLuminanceSource) {
                val pixels = source.renderThumbnail()
                val width = source.thumbnailWidth
                val height = source.thumbnailHeight
                bitmap = Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.ARGB_8888)
            }
            view.setImageBitmap(bitmap)
        }
    }


    fun show(view: View, lp: WindowManager.LayoutParams) {

        try {
            mView = view
            mWindowManager.addView(view, lp)
        } catch (e: Exception) {
            return  // 已经添加过
        }
    }

    fun hide() {
        try {
            if (mView != null) {
                mWindowManager.removeView(mView)
            }
        } catch (e: Exception) {
        }
    }
}