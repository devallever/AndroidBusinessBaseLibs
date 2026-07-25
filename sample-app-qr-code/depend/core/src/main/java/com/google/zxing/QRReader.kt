package com.google.zxing

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.zxing.common.HybridBinarizer
import kotlin.collections.ArrayList
import com.google.zxing.utils.BitmapUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


interface QRReaderListener {
    fun complete(result: Result?)
}

class QRReader {

    companion object {
        private const val THUMBNAIL_SCALE_FACTOR = 2
    }

    private var mMultiFormatReader: MultiFormatReader? = null
    private var mHints: Map<DecodeHintType, *>? = null
    private var mBitmap: Bitmap? = null

    init {

    }

    private fun check() {
        if (mMultiFormatReader == null) {
            mMultiFormatReader = MultiFormatReader()
        }
        if (mHints == null) {
            val format = defaultFormat()
            mHints = hashMapOf(
                    DecodeHintType.POSSIBLE_FORMATS to format,
                    DecodeHintType.CHARACTER_SET to "UTF8"
            )
        }
        mMultiFormatReader?.setHints(mHints)
    }

    fun defaultFormat(): ArrayList<BarcodeFormat> {
        return arrayListOf<BarcodeFormat>(
                BarcodeFormat.UPC_A,
                BarcodeFormat.UPC_E,
                BarcodeFormat.EAN_13,
                BarcodeFormat.EAN_8,
                BarcodeFormat.CODE_39,
                BarcodeFormat.CODE_93,
                BarcodeFormat.CODE_128,
                BarcodeFormat.ITF,
                BarcodeFormat.QR_CODE,
                BarcodeFormat.DATA_MATRIX
        )
    }

    fun setHints(hints: Map<DecodeHintType, *>) {
        mHints = hints
    }

    @JvmOverloads
    fun start(imgPath: String, listener: QRReaderListener, numberOfRetries: Int = 5) {
        check()
        val options = BitmapFactory.Options()
        options.inSampleSize = 1
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(imgPath, options)
        options.inJustDecodeBounds = false
        val scale = (options.outHeight.toFloat() / 400.0f).toInt()
        if (scale > 0) {
            options.inSampleSize = scale
        }
        val result = decode(imgPath, options, mMultiFormatReader!!, null, numberOfRetries)
        listener.complete(result)
    }

    @JvmOverloads
    fun startAsync(imgPath: String, listener: QRReaderListener, numberOfRetries: Int = 5) {
        CoroutineScope(Dispatchers.IO).launch {
            start(imgPath, listener, numberOfRetries)
        }
    }

    fun getBitmap(): Bitmap? {
        return mBitmap
    }

    fun destroy() {
        mBitmap?.recycle()
    }

    fun getThumbnailByteArray(): ByteArray? {
        val zoombitmap = getThumbnail()
        return if (zoombitmap != null) {
            BitmapUtils.getByteArray(zoombitmap)
        } else null
    }

    fun getThumbnail(): Bitmap? {
        val bitmap = mBitmap ?: return null
        return BitmapUtils.zoom(bitmap, THUMBNAIL_SCALE_FACTOR.toFloat())
    }

    fun getThumbnailScaleFactor(): Float {
        return THUMBNAIL_SCALE_FACTOR.toFloat()
    }

    fun getThumbnailWidth(): Int {
        return when (mBitmap) {
            null -> 0
            else -> mBitmap!!.width / THUMBNAIL_SCALE_FACTOR
        }
    }

    fun getThumbnailHeight(): Int {
        return when (mBitmap) {
            null -> 0
            else -> mBitmap!!.height / THUMBNAIL_SCALE_FACTOR
        }
    }

    private fun decode(str: String, options: BitmapFactory.Options, multiFormatReader: MultiFormatReader, result: Result?, numberOfRetries: Int): Result? {
        var numberOfRetries = numberOfRetries
        if (numberOfRetries <= 0) {
            return result
        }
        return try {
            mBitmap?.recycle()
            mBitmap = BitmapFactory.decodeFile(str, options)
            multiFormatReader.decode(BinaryBitmap(HybridBinarizer(BitmapLuminanceSource(mBitmap))))
        } catch (e: NotFoundException) {
            options.inSampleSize++
            decode(str, options, multiFormatReader, result, --numberOfRetries)
        }
    }
}