package com.funny.gif.memes.func.maker

import android.content.Context
import android.graphics.*
import android.media.Image
import android.media.MediaCodec
import android.media.MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
import android.media.MediaExtractor
import android.media.MediaExtractor.SEEK_TO_CLOSEST_SYNC
import android.media.MediaFormat
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.logE
import app.allever.android.lib.core.util.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

object GifMakeHelper {

    const val TAG = "GifMakeHelper"
    var gifDir = "${App.context.externalCacheDir}${File.separator}gif"

    init {
        GlobalScope.launch {
            if (!FileUtils.checkExist(gifDir)) {
                FileUtils.createDir(gifDir)
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    suspend fun makeGif(
        context: Context,
        uri: Uri,
        toFile: String,
        fromPosition: Int,
        toPosition: Int,
        period: Int,
        progress: (current: Int, total: Int, percent: Int) -> Unit
    ) = withContext(Dispatchers.IO) {
        val maker = GifMaker(2)
        maker.setOnGifListener { current, total , percent ->
            progress(current, total, percent)
        }
        val start = System.currentTimeMillis()
        val result = maker.makeGifFromVideoDirectly(
            context,
            uri,
            fromPosition.toLong(),
            toPosition.toLong(),
            period.toLong(),
            toFile
        )
        val end = System.currentTimeMillis()
        logE("转换${(toPosition - fromPosition) / 1000f}秒耗时${(end - start) / 1000}秒")
        result
    }


    private var rotation = 0
    private var videoFormat: MediaFormat? = null
    private var duration = 0L
    private var mediaCodec: MediaCodec? = null
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    suspend fun createGif(
        context: Context,
        fromFile: String,
        toFile: String,
        fromPosition: Int,
        toPosition: Int,
        period: Int,
        progress: (current: Int, total: Int) -> Unit
    ) = withContext(Dispatchers.IO) {
        val bitmapList = mutableListOf<Bitmap>()
        val mediaExtraCreator = MediaExtractor()
        mediaExtraCreator.setDataSource(fromFile)
        var count = mediaExtraCreator.trackCount
        for (i in 0 until count) {
            Log.d(TAG, "createGif: i = $i")
            val trackFormat = mediaExtraCreator.getTrackFormat(i)
            if (trackFormat.getString(MediaFormat.KEY_MIME)?.contains("video") == true) {
                videoFormat = trackFormat
                mediaExtraCreator.selectTrack(i)
                break
            }
        }

        if (videoFormat == null) {
            throw RuntimeException("videoFormat == null")
        }

        videoFormat?.setInteger(MediaFormat.KEY_COLOR_FORMAT, COLOR_FormatYUV420Flexible)
        videoFormat?.setInteger(MediaFormat.KEY_WIDTH, videoFormat?.getInteger(MediaFormat.KEY_WIDTH)?:0)
        videoFormat?.setInteger(MediaFormat.KEY_HEIGHT, videoFormat?.getInteger(MediaFormat.KEY_HEIGHT)?:0)
        if (videoFormat?.containsKey(MediaFormat.KEY_ROTATION) == true) {
            rotation = videoFormat?.getInteger(MediaFormat.KEY_ROTATION)?:0
        }
        duration = videoFormat?.getLong(MediaFormat.KEY_DURATION)?:0L
        mediaCodec = MediaCodec.createDecoderByType(videoFormat?.getString(MediaFormat.KEY_MIME)?:"")
        mediaCodec?.configure(videoFormat, null, null, 0)
        mediaCodec?.start()



        try {
            val bufferInfo = MediaCodec.BufferInfo()
            val timeOut = 5 * 1000L
            val minDuration = Math.min(duration, toPosition.toLong())
            var time: Long = fromPosition.toLong()
            val inputBuffers = mediaCodec?.inputBuffers
            while (time < minDuration) {
                Log.d(TAG, "createGif: frameTime = $time")
                mediaExtraCreator.seekTo(time * 1000, SEEK_TO_CLOSEST_SYNC)
                val inputBufferIndex = mediaCodec?.dequeueInputBuffer(timeOut)?:0
                if (inputBufferIndex >= 0) {
                    val inputBuffer = mediaCodec?.getInputBuffer(inputBufferIndex)
                    val sampleData = mediaExtraCreator.readSampleData(inputBuffer!!, 0)
                    val sampleTime = mediaExtraCreator.sampleTime
                    mediaCodec?.queueInputBuffer(inputBufferIndex, 0, sampleData, sampleTime, 0)
                    mediaExtraCreator.advance()
                }
                time += period

                val status = mediaCodec?.dequeueOutputBuffer(bufferInfo, timeOut)
                Log.d(TAG, "createGif: status = $status")
                when(status) {
                    MediaCodec.INFO_TRY_AGAIN_LATER,
                        MediaCodec.INFO_OUTPUT_FORMAT_CHANGED,
                        MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {

                        }
                    else -> {
                        val doRender = bufferInfo.size != 0
                        val image = mediaCodec?.getOutputImage(status!!)


                        //dataFromImage();

                        val bitmap = generateBitmap(image!!)
                        bitmap?.let {
                            bitmapList.add(it)
                        }
                        mediaCodec?.releaseOutputBuffer(status!!, doRender)
                    }
                }
            }



        } catch (e: Exception) {
            e.printStackTrace()
        }

        val gifMaker = GifMaker(2)
        gifMaker.makeGif(bitmapList, toFile)
    }

    private fun generateBitmap(image: Image?): Bitmap? {
        val start = System.currentTimeMillis()
        var bitmap: Bitmap? = null
        image?.let {
            val width = it.width
            val height = it.height

            val i240Size = width * height * 3 / 2
            val plans = it.planes

            val remaining0 = plans[0].buffer.remaining()
            val remaining1 = plans[1].buffer.remaining()
            val remaining2 = plans[2].buffer.remaining()

            val pixelStride = plans[2].pixelStride
            val rowStride = plans[2].rowStride

            val nv21 = ByteArray(i240Size)

            val yRawSrcBytes = ByteArray(remaining0)
            val uRawSrcBytes = ByteArray(remaining1)
            val vRawSrcBytes = ByteArray(remaining2)

            plans[0].buffer[yRawSrcBytes]
            plans[1].buffer[uRawSrcBytes]
            plans[2].buffer[vRawSrcBytes]

            if (pixelStride == width) {
                System.arraycopy(yRawSrcBytes, 0, nv21, 0, rowStride * height)
                System.arraycopy(vRawSrcBytes, 0, nv21, rowStride * height, rowStride * height / 2 - 1)
            } else {
                val ySrcBytes = ByteArray(width * height)
                val uSrcBytes = ByteArray(width * height / 2 - 1)
                val vSrcBytes = ByteArray(width * height / 2 - 1)

                for (row in 0 until height) {
                    System.arraycopy(yRawSrcBytes, rowStride * row, ySrcBytes, width * row, width)

                    if (row % 2 == 0) {
                        if (row == height - 2) {
                            System.arraycopy(vRawSrcBytes, rowStride * row / 2, vSrcBytes, width * row / 2, width - 1)
                        } else {
                            System.arraycopy(vRawSrcBytes, rowStride * row / 2, vSrcBytes, width * row / 2, width)
                        }
                    }
                }

                System.arraycopy(ySrcBytes, 0, nv21, 0, width * height)
                System.arraycopy(vSrcBytes, 0, nv21, width * height, width * height / 2 - 1)
            }

            bitmap = getImageBitmap(nv21, width, height, rotation)

            it.close()

        }
        val end = System.currentTimeMillis()
        Log.d(TAG, "generateBitmap: 耗时 -> ${(end - start) / 1000f}")
        return bitmap
    }

    private fun getImageBitmap(data: ByteArray, width: Int, height: Int, rotation: Int): Bitmap? {
        var bitmap: Bitmap? = null

        val yuvImage = YuvImage(data, ImageFormat.NV21, width, height, null)
        val baos = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 20, baos)
        val jData = baos.toByteArray()
        val option = BitmapFactory.Options()
        option.inPreferredConfig = Bitmap.Config.ARGB_8888
//        option.inSampleSize = 4
        if (rotation == 0) {
            bitmap = BitmapFactory.decodeByteArray(jData, 0, jData.size, option)
        } else {
            val matrix = Matrix()
            matrix.postRotate(rotation.toFloat())
            val bmp = BitmapFactory.decodeByteArray(jData, 0, jData.size, option)
            bitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
        }

        return bitmap
    }

    private fun processByExtractor(intervalMs: Int) {
        try {
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}