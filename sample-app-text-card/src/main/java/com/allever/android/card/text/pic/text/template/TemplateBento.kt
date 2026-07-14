package com.allever.android.card.text.pic.text.template

import android.graphics.Color
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.allever.android.card.text.pic.text.App
import com.allever.android.card.text.pic.text.R
import com.allever.android.card.text.pic.text.model.ColorData
import com.allever.android.card.text.pic.text.model.TemplateBgColor
import com.allever.android.card.text.pic.text.model.TemplateModel
import com.allever.android.card.text.pic.text.model.TextCardCore
import com.allever.android.card.text.pic.text.databinding.TcTemplateBentoBinding
import com.allever.android.card.text.pic.text.util.log
import com.allever.android.card.text.pic.text.view.widget.GradientBackgroundDrawable

class TemplateBento : TemplateModel<TcTemplateBentoBinding>() {
    private val colorList = mutableListOf(
        TemplateBgColor(
            //light color
            mutableListOf(
                ColorData.BENTO_BLUE_CYAN_GRADIENT,
                ColorData.BENTO_BLUE_PINK_GRADIENT,
                ColorData.BENTO_BLUE_LIME_GRADIENT,
                ColorData.BENTO_PINK_BLUE_GRADIENT,
                ColorData.BENTO_BLUE_VIOLET_GRADIENT,
                ColorData.BENTO_SKY_ORANGE_GRADIENT,
                ColorData.BENTO_PURPLE_YELLOW_GRADIENT,
                ColorData.BENTO_PINK_GRADIENT,
                ColorData.BENTO_PINK_RED_GRADIENT,
                ColorData.BENTO_RED_ORANGE_GRADIENT,
                ColorData.BENTO_LIGHT_DARK_ORANGE_GRADIENT,
                ColorData.BENTO_LIGHT_BLUE_PURPLE_GRADIENT,
                ColorData.BENTO_YELLOW_ORANGE_GRADIENT,
                ColorData.BENTO_GREEN_GRADIENT
            ),
            TemplateBgColor.COLOR_LIGHT,
        ),
        //dark color
        TemplateBgColor(
            mutableListOf(
                ColorData.BENTO_BLUE_CYAN_GRADIENT,
                ColorData.BENTO_BLUE_PINK_GRADIENT,
                ColorData.BENTO_BLUE_LIME_GRADIENT,
                ColorData.BENTO_PINK_BLUE_GRADIENT,
                ColorData.BENTO_BLUE_VIOLET_GRADIENT,
                ColorData.BENTO_SKY_ORANGE_GRADIENT,
                ColorData.BENTO_PURPLE_YELLOW_GRADIENT,
                ColorData.BENTO_PINK_GRADIENT,
                ColorData.BENTO_PINK_RED_GRADIENT,
                ColorData.BENTO_RED_ORANGE_GRADIENT,
                ColorData.BENTO_LIGHT_DARK_ORANGE_GRADIENT,
                ColorData.BENTO_LIGHT_BLUE_PURPLE_GRADIENT,
                ColorData.BENTO_YELLOW_ORANGE_GRADIENT,
                ColorData.BENTO_GREEN_GRADIENT,
                ColorData.BENTO_GRAY_GRADIENT
            ),
            TemplateBgColor.COLOR_DARK,
        )
    )

    override fun inflateView() = TcTemplateBentoBinding.inflate(LayoutInflater.from(App.context))
    override fun getTemplateName(): String {
        return "Bento"
    }

    override fun getTemplateCover(): Int {
        return R.drawable.icon_template_bento
    }


    override fun getTemplateBgColor(): MutableList<TemplateBgColor> {
        //dark & light
        log("Bento create getTemplateBgColor")
        return colorList
    }

    override fun showOrHideIcon(show: Boolean) {
        mBinding?.ivIcon?.isVisible = show
    }

    override fun showOrHideDate(show: Boolean) {
        mBinding?.tvDate?.isVisible = show
    }

    override fun showOrHideTitle(show: Boolean) {
        mBinding?.etTitle?.isVisible = show
    }

    override fun showOrHideContent(show: Boolean) {
        mBinding?.etContent?.isVisible = show
    }

    override fun showOrHideAuthor(show: Boolean) {
        mBinding?.etAuthor?.isVisible = show
    }

    override fun showOrHideWordCount(show: Boolean) {
        mBinding?.tvWordCount?.isVisible = show
    }

    override fun showOrHideQrCode(show: Boolean) {
        mBinding?.qrCode?.isVisible = show
    }

    override fun showOrHideMark(show: Boolean) {
        mBinding?.waterMark?.isVisible = show
    }

    override fun updateCardBg(isDark: Boolean, data: ColorData) {


        mBinding?.apply {

            //background
            templateBg.background = GradientBackgroundDrawable().apply {
                setColorList(
                    data.colorValue,
                    templateBg.width.toFloat(),
                    templateBg.height.toFloat()
                )
            }

            //cardView
            val cardBgColor = ContextCompat.getColor(
                App.context,
                if (isDark) R.color.color_0A0A0A else R.color.color_F5F5F5
            )
            cardView.setCardBackgroundColor(cardBgColor)

            //icon
            ivIcon.setBackgroundResource(if (isDark) R.drawable.tc_shape_bento_item_bg_dark else R.drawable.tc_shape_bento_item_bg)
            ivIcon.setColorFilter(
                Color.parseColor(TextCardCore.cardData.getBgColor())
            )
            TextCardCore.cardData.getBgColorName()

            //Date
            tvDate.setTextColor(App.getColor(if (isDark) R.color.color_80ffffff else R.color.color_80000000))
            tvDate.setBackgroundResource(if (isDark) R.drawable.tc_shape_bento_item_bg_dark else R.drawable.tc_shape_bento_item_bg)


            //title
            etTitle.setTextColor(Color.parseColor(TextCardCore.cardData.getBgColor()))
            etTitle.setHintTextColor(App.getColor(if (isDark) R.color.color_40ffffff else R.color.color_40000000))
            etTitle.setBackgroundResource(if (isDark) R.drawable.tc_shape_bento_item_bg_dark else R.drawable.tc_shape_bento_item_bg)
            //content
            etContent.setTextColor(App.getColor(if (isDark) R.color.white else R.color.black))
            etContent.setHintTextColor(App.getColor(if (isDark) R.color.color_40ffffff else R.color.color_40000000))
            etContent.setBackgroundResource(if (isDark) R.drawable.tc_shape_bento_item_bg_dark else R.drawable.tc_shape_bento_item_bg)


            //author
            etAuthor.setTextColor(App.getColor(if (isDark) R.color.color_80ffffff else R.color.color_80000000))
            etAuthor.setHintTextColor(App.getColor(if (isDark) R.color.color_40ffffff else R.color.color_40000000))
            etAuthor.setBackgroundResource(if (isDark) R.drawable.tc_shape_bento_item_bg_dark else R.drawable.tc_shape_bento_item_bg)
            //word
            tvWordCount.setTextColor(App.getColor(if (isDark) R.color.color_80ffffff else R.color.color_80000000))
            tvWordCount.setBackgroundResource(if (isDark) R.drawable.tc_shape_bento_item_bg_dark else R.drawable.tc_shape_bento_item_bg)

            //qrcode
            qrCode.setBackgroundResource(if (isDark) R.drawable.tc_shape_bento_item_bg_dark else R.drawable.tc_shape_bento_item_bg)
            ivQrCode.setColorFilter(App.getColor(if (isDark) R.color.color_60ffffff else R.color.color_60000000))
            tvQrCodeTitle.setTextColor(App.getColor(if (isDark) R.color.color_80ffffff else R.color.color_80000000))
            tvQrCodeDesc.setTextColor(App.getColor(if (isDark) R.color.color_60ffffff else R.color.color_60000000))

            //water mark
            tvWater.setTextColor(App.getColor(if (isDark) R.color.color_80ffffff else R.color.color_80000000))
            waterMarkContainer.setBackgroundResource(if (isDark) R.drawable.tc_shape_bento_item_bg_dark else R.drawable.tc_shape_bento_item_bg)

        }
    }

    override fun getIconView() = mBinding?.ivIcon!!

    override fun getDateView() = mBinding?.tvDate!!

    override fun getTitleView() = mBinding?.etTitle!!

    override fun getContentView() = mBinding?.etContent!!

    override fun getAuthorView() = mBinding?.etAuthor!!

    override fun getWordCountView() = mBinding?.tvWordCount!!

    override fun getTemplateContentView() = mBinding?.templateBg!!
}