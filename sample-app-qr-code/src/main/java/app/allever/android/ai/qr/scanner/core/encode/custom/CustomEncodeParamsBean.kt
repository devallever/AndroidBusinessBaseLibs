package app.allever.android.ai.qr.scanner.core.encode.custom

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import com.android.absbase.App
import com.android.absbase.utils.DeviceUtils
import com.allever.app.qr.code.scaner.R
import java.lang.ref.SoftReference

class CustomEncodeParamsBean {
    companion object {
        private val cacheBitmapMaps = mutableMapOf<Int,SoftReference<Bitmap>>()
        private var __defaultParamsBean: CustomEncodeParamsBean? = null
        @JvmStatic
        fun getDefaultParamsBean(): CustomEncodeParamsBean {
            val defaultParamsBean = __defaultParamsBean
            if (defaultParamsBean != null) {
                return defaultParamsBean
            }
            val customBean = CustomEncodeParamsBean()
            //外边框背景
            val bitmap = BitmapFactory.decodeResource(App.getContext().resources, R.drawable.qr_code_white)
            customBean.backgroundBitmap = bitmap
            customBean.outRingWidth = DeviceUtils.dip2px(268f)
            customBean.outRingHeight = DeviceUtils.dip2px(268f)
            //前景色
            customBean.qrCodeForegroundColor = 0x000000
            //背景色
            customBean.qrCodeBackgroundColor = 0x00FFFFFF
            customBean.qrCodeLeftTopPoint = Point(DeviceUtils.dip2px(46f), DeviceUtils.dip2px(46f))
            customBean.qrCodeRightBottomPoint = Point(DeviceUtils.dip2px(222f), DeviceUtils.dip2px(222f))
            //中心图片
            customBean.bitmapPath = null
            customBean.bitmapLeftTopPoint = Point(DeviceUtils.dip2px(118f), DeviceUtils.dip2px(118f))
            customBean.bitmapRightBottomPoint = Point(DeviceUtils.dip2px(150f), DeviceUtils.dip2px(150f))
            //中心图片边距
            customBean.bitmapOutRingWidth = DeviceUtils.dip2px(1f)
            customBean.bitmapOutRingBackgroundColor = -0x1
            customBean.bitmapOutRingRadius = DeviceUtils.dip2px(3f)
            customBean.selectOrNot = true
            __defaultParamsBean = customBean
            return customBean;
        }
        @JvmStatic
        fun getCustom(): CustomEncodeParamsBean {
            val customBean = CustomEncodeParamsBean()
            //外边框背景
            val bitmap = null
            customBean.backgroundBitmap = bitmap
            customBean.outRingWidth = DeviceUtils.dip2px(268f)
            customBean.outRingHeight = DeviceUtils.dip2px(268f)
            //前景色
            customBean.qrCodeForegroundColor = 0
            //背景色
            customBean.qrCodeBackgroundColor = 0x00FFFFFF
            customBean.qrCodeLeftTopPoint = Point(DeviceUtils.dip2px(55f), DeviceUtils.dip2px(69f))
            customBean.qrCodeRightBottomPoint = Point(DeviceUtils.dip2px(209f), DeviceUtils.dip2px(223f))
            //中心图片
            customBean.bitmapPath = null
            customBean.bitmapLeftTopPoint = Point(DeviceUtils.dip2px(118f), DeviceUtils.dip2px(132f))
            customBean.bitmapRightBottomPoint = Point(DeviceUtils.dip2px(146f), DeviceUtils.dip2px(161f))
            //中心图片边距
            customBean.bitmapOutRingWidth = DeviceUtils.dip2px(1f)
            customBean.bitmapOutRingBackgroundColor = -0x1
            customBean.bitmapOutRingRadius = DeviceUtils.dip2px(3f)
            return customBean;
        }

        /**
         * 从内存缓存获取bitmap
         */
        private fun decodeBitmapBackgroundFromResId(backgroundBitmapResId: Int):Bitmap{
            val reference = cacheBitmapMaps.get(backgroundBitmapResId)
            if(reference != null){
                val bitmap = reference.get()
                if(bitmap != null){
                    return bitmap
                }
            }
            val decodeResource = BitmapFactory.decodeResource(App.getContext().resources, backgroundBitmapResId)
            cacheBitmapMaps[backgroundBitmapResId] = SoftReference(decodeResource)
            return decodeResource
        }
        @JvmOverloads
        @JvmStatic
        fun getCustomParamsBean(foregroundColor: Int?, backgroundBitmapResId: Int?, centerBitmapPath: String? = null): CustomEncodeParamsBean {
            val customBean = getCustom()
            //外边框背景
            if(backgroundBitmapResId != null){
                val bitmap =  decodeBitmapBackgroundFromResId(backgroundBitmapResId)
                customBean.backgroundBitmap = bitmap
            }

            //前景色
            customBean.qrCodeForegroundColor = foregroundColor
            //中心图片
            customBean.bitmapPath = centerBitmapPath
            return customBean;
        }
    }

    /**
     * 自定义二维码参数
     * @param outRingWidth     外边框宽度
     * @param outRingHeight     外边框高度
     * @param backgroundBitmap        外边框背景
     * @param qrCodeLeftTopPoint    二维码左上角坐标
     * @param qrCodeLeftBottomPoint
     * @param qrCodeRightTopPoint
     * @param qrCodeRightBottomPoint
     * @param qrCodeForegroundColor     二维码前景色
     * @param qrCodeBackgroundColor     二维码背景色
     * @param bitmapPath                二维码中心点图片
     * @param bitmapLeftTopPoint      二维码中心点图片左上角坐标
     * @param bitmapLeftBottomPoint
     * @param bitmapRightTopPoint
     * @param bitmapRightBottomPoint
     * @param bitmapOutRingBackgroundColor  二维码中心点图片外边框背景色
     * @param bitmapOutRingRadius       二维码中心点图片外边框弧度
     * @param bitmapOutRingWidth        二维码中心点图片外边框宽度
     */
    var outRingWidth = 0
    var outRingHeight = 0
    var qrCodeLeftTopPoint: Point? = null
    var qrCodeLeftBottomPoint: Point? = null
    var qrCodeRightTopPoint: Point? = null
    var qrCodeRightBottomPoint: Point? = null
    var qrCodeForegroundColor:Int? = 0
    var qrCodeBackgroundColor:Int? = 0
    var bitmapPath: String? = null
    var iconResId: Int? = null
    var bitmapLeftTopPoint: Point? = null
    var bitmapLeftBottomPoint: Point? = null
    var bitmapRightTopPoint: Point? = null
    var bitmapRightBottomPoint: Point? = null
    var bitmapOutRingBackgroundColor = 0
    var bitmapOutRingRadius = 0
    var bitmapOutRingWidth = 0
    var backgroundBitmap: Bitmap? = null
    var selectOrNot = false
}

