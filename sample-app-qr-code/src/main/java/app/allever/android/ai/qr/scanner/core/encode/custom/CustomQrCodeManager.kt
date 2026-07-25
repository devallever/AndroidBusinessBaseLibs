package app.allever.android.ai.qr.scanner.core.encode.custom

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.android.absbase.utils.ResourcesUtils
import com.google.zxing.common.BitMatrix
import com.allever.app.qr.code.scaner.R
import app.allever.android.ai.qr.scanner.bean.ShareItem

object CustomQrCodeManager {
    private const val PRE_PACKETNAME = "com.qrcode.scanner.plugins.customqrcode"
    private const val WHITE = Color.WHITE
    private const val BLACK = Color.BLACK
    val DEFAULT_PACKET_NAME = "$PRE_PACKETNAME.white"
    val PACKET_NAMES = arrayOf<String>(
            "$PRE_PACKETNAME.white",
            "$PRE_PACKETNAME.beach",
            "$PRE_PACKETNAME.bread",
            "$PRE_PACKETNAME.grass",
            "$PRE_PACKETNAME.melon",
            "$PRE_PACKETNAME.prince",
            "$PRE_PACKETNAME.yellow",

            "$PRE_PACKETNAME.instagram",
            "$PRE_PACKETNAME.facebook",
            "$PRE_PACKETNAME.whatsapp",
            "$PRE_PACKETNAME.youtube",
            "$PRE_PACKETNAME.twitter",
            "$PRE_PACKETNAME.spotify",
            "$PRE_PACKETNAME.viber"
    )
    val Social_PACKET_NAMES = mapOf<Int, String>(
            Pair(ShareItem.TYPE_FACEBOOK, "$PRE_PACKETNAME.facebook"),
            Pair(ShareItem.TYPE_INSTAGRAM, "$PRE_PACKETNAME.instagram"),
            Pair(ShareItem.TYPE_SPOTIFY, "$PRE_PACKETNAME.spotify"),
            Pair(ShareItem.TYPE_TWITTER, "$PRE_PACKETNAME.twitter"),
            Pair(ShareItem.TYPE_VIBER, "$PRE_PACKETNAME.viber"),
            Pair(ShareItem.TYPE_WHATSAPP, "$PRE_PACKETNAME.whatsapp"),
            Pair(ShareItem.TYPE_YOUTUBE, "$PRE_PACKETNAME.youtube")
    )

    val Social_ICONS = mapOf<Int, Int>(
            Pair(ShareItem.TYPE_FACEBOOK, R.drawable.icon_facebook),
            Pair(ShareItem.TYPE_INSTAGRAM, R.drawable.icon_instagram),
            Pair(ShareItem.TYPE_SPOTIFY, R.drawable.icon_spotify),
            Pair(ShareItem.TYPE_TWITTER, R.drawable.icon_twitter),
            Pair(ShareItem.TYPE_VIBER, R.drawable.icon_viber),
            Pair(ShareItem.TYPE_WHATSAPP, R.drawable.icon_whatsapp),
            Pair(ShareItem.TYPE_YOUTUBE, R.drawable.icon_youtube)
    )
    var mCustomStyleQrCodeMap = mutableMapOf<String, CustomEncodeParamsBean>()
    var mCustomStyleQrCodes = mutableListOf<CustomEncodeParamsBean>()

    private data class DefaultCustomQrData(val packageName: String, val foreColor: Int, val backgroundResId: Int)

    init {
        val defaultCustomDatas = arrayOf(
                DefaultCustomQrData("$PRE_PACKETNAME.white", 0xFF000000.toInt(), R.drawable.code_white),
                DefaultCustomQrData("$PRE_PACKETNAME.beach", 0xFF3F52C2.toInt(), R.drawable.code_beach),
                DefaultCustomQrData("$PRE_PACKETNAME.bread", 0xFF5d2f0b.toInt(), R.drawable.code_bread),
                DefaultCustomQrData("$PRE_PACKETNAME.grass", 0xFF36cfa1.toInt(), R.drawable.code_grass),
                DefaultCustomQrData("$PRE_PACKETNAME.melon", 0xFF2d0e08.toInt(), R.drawable.code_melon),
                DefaultCustomQrData("$PRE_PACKETNAME.prince", 0xFF21263b.toInt(), R.drawable.code_prince),
                DefaultCustomQrData("$PRE_PACKETNAME.yellow", 0xFF181616.toInt(), R.drawable.code_yellow),

                // Instagram > Facebook >  Whatsapp > Youtube > Twitter > Spotify > Viber
                DefaultCustomQrData("$PRE_PACKETNAME.instagram", 0xFF2A062C.toInt(), R.drawable.code_instagram),
                DefaultCustomQrData("$PRE_PACKETNAME.facebook", 0xFF030A1A.toInt(), R.drawable.code_facebook),
                DefaultCustomQrData("$PRE_PACKETNAME.whatsapp", 0xFF192308.toInt(), R.drawable.code_whatsapp),
                DefaultCustomQrData("$PRE_PACKETNAME.youtube", 0xFF2E070B.toInt(), R.drawable.code_youtube),
                DefaultCustomQrData("$PRE_PACKETNAME.twitter", 0xFF151B42.toInt(), R.drawable.code_twitter),
                DefaultCustomQrData("$PRE_PACKETNAME.spotify", 0xFF0B2603.toInt(), R.drawable.code_spotify),
                DefaultCustomQrData("$PRE_PACKETNAME.viber", 0xFF1F0B26.toInt(), R.drawable.code_viber)

        )

        defaultCustomDatas.mapIndexed { index, defaultCustomQrData ->

            if (index == 0) {
                mCustomStyleQrCodeMap[defaultCustomQrData.packageName] =
                    CustomEncodeParamsBean.getDefaultParamsBean()
            } else {
                mCustomStyleQrCodeMap[defaultCustomQrData.packageName] =
                    CustomEncodeParamsBean.getCustomParamsBean(
                        defaultCustomQrData.foreColor,
                        defaultCustomQrData.backgroundResId,
                        null
                    )
            }
        }
    }

    @JvmStatic
    fun drawCustomQrCodeBitmap(result: BitMatrix, paramsBean: CustomEncodeParamsBean): Bitmap {
        val width = result.width
        val height = result.height
        //前景色
        val foregroundColor = if (paramsBean.qrCodeForegroundColor == 0) BLACK else paramsBean.qrCodeForegroundColor
                ?: 0
        //背景色
        val backgroundColor = if (paramsBean.qrCodeBackgroundColor == 0) WHITE else paramsBean.qrCodeBackgroundColor
                ?: 0
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (result.get(x, y)) foregroundColor else backgroundColor
            }
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)

        //创建画布
        var outRingWidth = paramsBean.outRingWidth
        var outRingHeight = paramsBean.outRingHeight
        if (outRingWidth == 0) {
            outRingWidth = width
        }
        if (outRingHeight == 0) {
            outRingHeight = height
        }
        val outBitmap = Bitmap.createBitmap(outRingWidth, outRingHeight, Bitmap.Config.ARGB_8888)
        val bitmapPaint = Paint()
        bitmapPaint.isAntiAlias = true
        bitmapPaint.isFilterBitmap = true
        val canvas = Canvas(outBitmap)
        //画背景
        val backgroundBitmap = paramsBean.backgroundBitmap
        if (backgroundBitmap != null) {
            canvas.drawBitmap(backgroundBitmap, 0f, 0f, bitmapPaint)
        } else {
            bitmapPaint.color = WHITE
            canvas.drawRect(Rect(0, 0, outRingWidth, outRingHeight), bitmapPaint)
        }
        //画二维码
        val qrCodeLeftTopPoint = paramsBean.qrCodeLeftTopPoint
        val qrCodeRightBottomPoint = paramsBean.qrCodeRightBottomPoint
        var srcRect = Rect(0, 0, bitmap.getWidth(), bitmap.getHeight())
        var qrCodeRect = Rect(0, 0, width, height)
        if (qrCodeLeftTopPoint != null && qrCodeRightBottomPoint != null) {
            qrCodeRect = Rect(qrCodeLeftTopPoint.x, qrCodeLeftTopPoint.y, qrCodeRightBottomPoint.x, qrCodeRightBottomPoint.y)
        }
        canvas.drawBitmap(bitmap, srcRect, qrCodeRect, bitmapPaint)
        //画中心点图片
        val bitmapPath = paramsBean.bitmapPath
        val iconResId = paramsBean.iconResId
        if (bitmapPath != null || iconResId != null) {
            val bitmapLeftTopPoint = paramsBean.bitmapLeftTopPoint
            val bitmapRightBottomPoint = paramsBean.bitmapRightBottomPoint
            if (bitmapLeftTopPoint != null && bitmapRightBottomPoint != null) {
                //画图片的边框
                val bitmapOutRingRecf = RectF(bitmapLeftTopPoint.x.toFloat(), bitmapLeftTopPoint.y.toFloat(), bitmapRightBottomPoint.x.toFloat(), bitmapRightBottomPoint.y.toFloat())
                bitmapPaint.color = paramsBean.bitmapOutRingBackgroundColor
                val bitmapOutRingRadius = paramsBean.bitmapOutRingRadius.toFloat()
                canvas.drawRoundRect(bitmapOutRingRecf, bitmapOutRingRadius, bitmapOutRingRadius, bitmapPaint)
                //边框的宽度
                val bitmapOutRingWidth = paramsBean.bitmapOutRingWidth
                val disRect = Rect(bitmapLeftTopPoint.x + bitmapOutRingWidth, bitmapLeftTopPoint.y + bitmapOutRingWidth, bitmapRightBottomPoint.x - bitmapOutRingWidth, bitmapRightBottomPoint.y - bitmapOutRingWidth)
                val scaleBitmap = if (bitmapPath != null) {
                    CustomQrCodeUtils.getBitmap(
                        bitmapPath,
                        disRect.right - disRect.left,
                        disRect.bottom - disRect.top
                    )
                } else if (iconResId != null) {
                    val drawable = ResourcesUtils.getDrawable(iconResId)
                    drawable2bitmap(drawable)
                } else {
                    null
                }
                if (scaleBitmap != null) {
                    // 绘制图片
                    srcRect = Rect(0, 0, scaleBitmap.width, scaleBitmap.height)
                    val roundedCornerBitmap = CustomQrCodeUtils.getRoundedCornerBitmap(
                        scaleBitmap,
                        bitmapOutRingRadius * 2
                    )
                    if (roundedCornerBitmap != null) {
                        canvas.drawBitmap(roundedCornerBitmap, srcRect, disRect, bitmapPaint)
                    }
                }
            }
        }
        return outBitmap
    }

    private fun drawable2bitmap(item: Drawable?): Bitmap? {
        if (item == null) {
            return null
        }
        if (item is BitmapDrawable) {
            return item.bitmap
        }
        try {
            val bitmap: Bitmap
            bitmap = Bitmap.createBitmap(item.intrinsicWidth, item.intrinsicHeight, Bitmap.Config.RGB_565)
            val canvas = Canvas(bitmap)
            item.setBounds(0, 0, canvas.width, canvas.height)
            item.draw(canvas)
            return bitmap
        } catch (e: OutOfMemoryError) {
            return null
        }

    }

//        @JvmStatic
//        fun getCustomStyleQrodeData(path: String?, iconId: Int?): List<CustomEncodeParamsBean> {
//            mCustomStyleQrCodes = PACKET_NAMES.map {
//                val bean = mCustomStyleQrCodeMap[it]
//                bean?.bitmapPath = path
//                bean?.iconResId = iconId
//                bean
//            }.filterNotNull().toMutableList()
//            return mCustomStyleQrCodes
//        }

    @JvmStatic
    fun setCustomStyleIcon(beans: List<CustomEncodeParamsBean>, path: String?, iconId: Int?) {
        for (bean in beans) {
            bean.bitmapPath = path
            bean.iconResId = iconId
        }
    }

    @JvmStatic
    fun getCustomStyleQrcodeData(shareType: Int): List<CustomEncodeParamsBean> {
        val beans = arrayListOf<CustomEncodeParamsBean>()
        val firstPackageName = Social_PACKET_NAMES[shareType]
        var firstBean: CustomEncodeParamsBean? = null
        for (packageName in PACKET_NAMES) {
            val bean = mCustomStyleQrCodeMap[packageName] ?: continue
            if (packageName == firstPackageName) {
                firstBean = bean
            } else {
                beans.add(bean)
            }
        }
        if (firstBean != null) {
            beans.add(0, firstBean)
        }
        return beans
    }
}