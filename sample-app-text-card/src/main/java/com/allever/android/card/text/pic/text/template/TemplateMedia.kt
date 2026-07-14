package com.allever.android.card.text.pic.text.template

import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.allever.android.card.text.pic.text.App
import com.allever.android.card.text.pic.text.R
import com.allever.android.card.text.pic.text.model.ColorData
import com.allever.android.card.text.pic.text.model.TemplateBgColor
import com.allever.android.card.text.pic.text.model.TemplateModel
import com.allever.android.card.text.pic.text.databinding.TcTemplateMediaBinding
import com.allever.android.card.text.pic.text.util.log
import com.allever.android.card.text.pic.text.view.widget.GradientBackgroundDrawable

class TemplateMedia : TemplateModel<TcTemplateMediaBinding>() {
    private val colorList = mutableListOf(
        TemplateBgColor(
            //light color
            mutableListOf(
                ColorData.MEDIA_BLUE_CYAN_GRADIENT,
                ColorData.MEDIA_BLUE_PINK_GRADIENT,
                ColorData.MEDIA_BLUE_LIME_GRADIENT,
                ColorData.MEDIA_PINK_BLUE_GRADIENT,
                ColorData.MEDIA_BLUE_VIOLET_GRADIENT,
                ColorData.MEDIA_SKY_ORANGE_GRADIENT,
                ColorData.MEDIA_PURPLE_YELLOW_GRADIENT,
                ColorData.MEDIA_PINK_GRADIENT,
                ColorData.MEDIA_PINK_RED_GRADIENT,
                ColorData.MEDIA_RED_ORANGE_GRADIENT,
                ColorData.MEDIA_LIGHT_DARK_ORANGE_GRADIENT,
                ColorData.MEDIA_LIGHT_BLUE_PURPLE_GRADIENT,
                ColorData.MEDIA_YELLOW_ORANGE_GRADIENT,
                ColorData.MEDIA_GREEN_GRADIENT,
                ColorData.MEDIA_GRAY_GRADIENT
            ),
            TemplateBgColor.COLOR_LIGHT,
        ),
        //dark color
        TemplateBgColor(
            mutableListOf(
                ColorData.MEDIA_DARK_BLUE_GRADIENT,
                ColorData.MEDIA_DARK_INDIGO_GRADIENT,
                ColorData.MEDIA_DARK_SKY_GRADIENT,
                ColorData.MEDIA_DARK_CYAN_GRADIENT,
                ColorData.MEDIA_DARK_VIOLET_GRADIENT,
                ColorData.MEDIA_DARK_FUSHSIA_GRADIENT,
                ColorData.MEDIA_DARK_ROSE_GRADIENT,
                ColorData.MEDIA_DARK_EMERALD_GRADIENT,
                ColorData.MEDIA_DARK_GRAY_GRADIENT,
                ColorData.MEDIA_DARK_SLATE_GRADIENT,
                ColorData.MEDIA_DARK_NEUTRAL_GRADIENT,
                ColorData.MEDIA_DARK_STONE_GRADIENT
            ),
            TemplateBgColor.COLOR_DARK,
        )
    )

    override fun inflateView() = TcTemplateMediaBinding.inflate(LayoutInflater.from(App.context))
    override fun getTemplateName(): String {
        return "Media"
    }

    override fun getTemplateCover(): Int {
        return R.drawable.icon_template_media
    }


    override fun getTemplateBgColor(): MutableList<TemplateBgColor> {
        //dark & light
        log("Media create getTemplateBgColor")
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
        mBinding?.qrCodeLine?.isVisible = show
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
                if (isDark) R.color.tc_card_drak_bg_color else R.color.white
            )
            cardView.setCardBackgroundColor(cardBgColor)

            //icon
            ivIcon.setBackgroundResource(if (isDark) R.drawable.shape_100f17_r45 else R.drawable.shape_999999_r45)
            ivIcon.setColorFilter(
                ContextCompat.getColor(
                    App.context,
                    if (isDark) R.color.white else R.color.black
                )
            )

            //Date
            tvDate.setTextColor(App.getColor(if (isDark) R.color.color_41404A else R.color.color_B0B5B9))

            //title
            etTitle.setTextColor(App.getColor(if (isDark) R.color.white else R.color.black))
            etTitle.setHintTextColor(App.getColor(if (isDark) R.color.color_41404A else R.color.color_999999))
            //content
            etContent.setTextColor(App.getColor(if (isDark) R.color.white else R.color.black))
            etContent.setHintTextColor(App.getColor(if (isDark) R.color.color_41404A else R.color.color_999999))

            //author
            etAuthor.setTextColor(App.getColor(if (isDark) R.color.color_41404A else R.color.color_B0B5B9))
            etAuthor.setHintTextColor(App.getColor(if (isDark) R.color.color_41404A else R.color.color_B0B5B9))
            //word
            tvWordCount.setTextColor(App.getColor(if (isDark) R.color.color_41404A else R.color.color_B0B5B9))

            //qrcode
            ivQrCode.setColorFilter(App.getColor(if (isDark) R.color.color_41404A else R.color.color_B0B5B9))
            tvQrCodeTitle.setTextColor(App.getColor(if (isDark) R.color.color_4C5159 else R.color.color_999999))
            tvQrCodeDesc.setTextColor(App.getColor(if (isDark) R.color.color_41404A else R.color.color_B0B5B9))

            //water mark
            tvWater.setTextColor(App.getColor(if (isDark) R.color.color_4D4E5D else R.color.color_80000000))
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