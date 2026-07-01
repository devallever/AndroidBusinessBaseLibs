package com.allever.video.editor.function


import androidx.annotation.DrawableRes
import androidx.annotation.IntDef

import com.allever.video.editor.R


/**
 * 定义宽高的比例
 */
class Ratio private constructor(@RatioId id: Int, @DrawableRes drawableId: Int, scaleFactor: Float, scaleFactorString: String) {

    @RatioId
    @get:RatioId
    var id: Int = 0
        internal set

    @DrawableRes
    @get:DrawableRes
    var drawableId: Int = 0
        internal set

    //宽高比
    var scaleFactor: Float = 0.toFloat()
        internal set

    //宽高比
    var scaleFactorString: String
        internal set

    @IntDef(value = [
        RatioId.RATIO_ORIGINAL,
        RatioId.RATIO_16x9,
        RatioId.RATIO_1x1,
        RatioId.RATIO_4x3,
        RatioId.RATIO_9x16,
        RatioId.RATIO_3x4,
        RatioId.RATIO_2x1,
        RatioId.RATIO_2x3,
        RatioId.RATIO_2d35x1,
        RatioId.RATIO_9x19d5,
        RatioId.RATIO_19d5x9
    ])
    @Retention(AnnotationRetention.SOURCE)
    annotation class RatioId {
        companion object {
            const val RATIO_ORIGINAL = 1
            const val RATIO_16x9 = 169
            const val RATIO_1x1 = 11
            const val RATIO_4x3 = 43
            const val RATIO_9x16 = 916
            const val RATIO_3x4 = 34
            const val RATIO_2x1 = 21
            const val RATIO_2x3 = 23
            const val RATIO_2d35x1 = 2351
            const val RATIO_9x19d5 = 9195
            const val RATIO_19d5x9 = 1959
        }
    }

    init {
        this.id = id
        this.drawableId = drawableId
        this.scaleFactor = scaleFactor
        this.scaleFactorString = scaleFactorString
    }

    override fun equals(obj: Any?): Boolean {
        return this === obj || obj is Ratio && this.id == obj.id
    }

    companion object {

        //  Original > 16:9（YouTube）> 1:1（Instagram） > 4:3(Facebook)
        //  > 9:16（TikTok/Musical.ly） > 3:4(Facebook) > 2:1(Twitter)
        //  > 2:3(Pinterest) > 2.35:1(Twitter) > 9:19.5 > 19.5:9

        val RATIO_ORIGINAL = Ratio(
            RatioId.RATIO_ORIGINAL,
            R.drawable.icon_edit_size_original,
            -1f,
            "Original"
        )
        // YouTube
        val RATIO_16x9 = Ratio(
            RatioId.RATIO_16x9,
            R.drawable.icon_edit_size_16x9,
            16f / 9,
            "16:9"
        )
        // Instagram
        val RATIO_1x1 = Ratio(
            RatioId.RATIO_1x1,
            R.drawable.icon_edit_size_1x1,
            1f / 1,
            "1:1"
        )
        // Facebook
        val RATIO_4x3 = Ratio(
            RatioId.RATIO_4x3,
            R.drawable.icon_edit_size_4x3,
            4f / 3,
            "4:3"
        )
        // TikTok/Musical.ly
        val RATIO_9x16 = Ratio(
            RatioId.RATIO_9x16,
            R.drawable.icon_edit_size_9x16,
            9f / 16,
            "9:16"
        )
        // Facebook
        val RATIO_3x4 = Ratio(
            RatioId.RATIO_3x4,
            R.drawable.icon_edit_size_3x4,
            3f / 4,
            "3:4"
        )
        // Twitter
        val RATIO_2x1 = Ratio(
            RatioId.RATIO_2x1,
            R.drawable.icon_edit_size_2x1,
            2f / 1,
            "2:1"
        )
        // Pinterest
        val RATIO_2x3 = Ratio(
            RatioId.RATIO_2x3,
            R.drawable.icon_edit_size_2x3,
            2f / 3,
            "2:3"
        )
        // Twitter
        val RATIO_2d35x1 = Ratio(
            RatioId.RATIO_2d35x1,
            R.drawable.icon_edit_size_2d35x1,
            2.35f / 1,
            "2.35:1"
        )
        val RATIO_9x19d5 = Ratio(
            RatioId.RATIO_9x19d5,
            R.drawable.icon_edit_size_9x19d5,
            9 / 19.5f,
            "9:19.5"
        )
        val RATIO_19d5x9 = Ratio(
            RatioId.RATIO_19d5x9,
            R.drawable.icon_edit_size_19d5x9,
            19.5f / 9,
            "19.5:9"
        )

        val RATIOS = arrayOf(
            RATIO_ORIGINAL,
            RATIO_16x9,
            RATIO_1x1,
            RATIO_4x3,
            RATIO_9x16,
            RATIO_3x4,
            RATIO_2x1,
            RATIO_2x3,
            RATIO_2d35x1,
            RATIO_9x19d5,
            RATIO_19d5x9
        )

        fun equals(ratio1: Ratio?, ratio2: Ratio?): Boolean {
            return ratio1?.id == ratio2?.id
        }

        fun newRatioOriginal(): Ratio {
            return Ratio(
                RATIO_ORIGINAL.id,
                RATIO_ORIGINAL.drawableId,
                RATIO_ORIGINAL.scaleFactor,
                RATIO_ORIGINAL.scaleFactorString
            )
        }

        fun getStatisticName(ratio: Ratio): String {
            return ratio.scaleFactorString
        }
    }
}
