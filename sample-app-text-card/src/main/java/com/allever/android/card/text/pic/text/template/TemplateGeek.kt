package com.allever.android.card.text.pic.text.template

import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.allever.android.card.text.pic.text.App
import com.allever.android.card.text.pic.text.R
import com.allever.android.card.text.pic.text.model.ColorData
import com.allever.android.card.text.pic.text.model.TemplateBgColor
import com.allever.android.card.text.pic.text.model.TemplateModel
import com.allever.android.card.text.pic.text.model.TextCardCore
import com.allever.android.card.text.pic.text.databinding.TcTemplateGeekBinding
import com.allever.android.card.text.pic.text.util.log
import com.allever.android.card.text.pic.text.view.widget.GradientBackgroundDrawable

class TemplateGeek : TemplateModel<TcTemplateGeekBinding>() {
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

    override fun inflateView() = TcTemplateGeekBinding.inflate(LayoutInflater.from(App.context))
    override fun getTemplateName(): String {
        return "Geek"
    }

    override fun getTemplateCover(): Int {
        return R.drawable.icon_template_geek
    }


    override fun getTemplateBgColor(): MutableList<TemplateBgColor> {
        //dark & light
        log("Bento create getTemplateBgColor")
        return colorList
    }

    override fun showOrHideIcon(show: Boolean) {
        mBinding?.ivIcon?.isVisible = show
        mBinding?.iconContainer?.isVisible = show
    }

    override fun showOrHideDate(show: Boolean) {
        mBinding?.tvDate?.isVisible = show
        checkHideMainContentContainer()
    }

    override fun showOrHideTitle(show: Boolean) {
        mBinding?.etTitle?.isVisible = show
        checkHideMainContentContainer()
    }

    override fun showOrHideContent(show: Boolean) {
        mBinding?.etContent?.isVisible = show
        checkHideMainContentContainer()
    }

    override fun showOrHideAuthor(show: Boolean) {
        mBinding?.etAuthor?.isVisible = show
        checkHideMainContentContainer()
    }

    override fun showOrHideWordCount(show: Boolean) {
        mBinding?.tvWordCount?.isVisible = show
        checkHideMainContentContainer()
    }

    private fun checkHideMainContentContainer() {
        mBinding?.mainContainer?.isVisible =
            mBinding?.tvDate?.isVisible == true ||
                    mBinding?.etTitle?.isVisible == true ||
                    mBinding?.etContent?.isVisible == true ||
                    mBinding?.etAuthor?.isVisible == true ||
                    mBinding?.tvWordCount?.isVisible == true

    }

    override fun showOrHideQrCode(show: Boolean) {
        mBinding?.qrCode?.isVisible = show
    }

    override fun showOrHideMark(show: Boolean) {
        mBinding?.waterMark?.isVisible = show
        mBinding?.waterMarkContainer?.isVisible = show
    }

    override fun updateCardBg(isDark: Boolean, data: ColorData) {


        mBinding?.apply {

            //background
            templateBg.background = GradientBackgroundDrawable().apply {
                setColorListGradient(
                    data.colorValue,
                    templateBg.width.toFloat(),
                    templateBg.height.toFloat()
                )
            }

            //cardView
            val cardBgColor = ContextCompat.getColor(
                App.context,
                if (isDark) R.color.color_A0000000 else R.color.color_A0ffffff
            )
            cardView.setCardBackgroundColor(cardBgColor)

            //icon
            iconContainer.setBackgroundResource(if (isDark) R.drawable.tc_shape_geek_item_bg_dark else R.drawable.tc_shape_geek_item_bg)
            ivIcon.setColorFilter(
                ContextCompat.getColor(
                    App.context,
                    if (isDark) R.color.color_40ffffff else R.color.color_80ffffff
                )
            )
            TextCardCore.cardData.getBgColorName()

            iconEmpty1.setBackgroundResource(if (isDark) R.drawable.tc_shape_geek_empty_text_dark else R.drawable.tc_shape_geek_empty_text)
            iconEmpty2.setBackgroundResource(if (isDark) R.drawable.tc_shape_geek_empty_text_dark else R.drawable.tc_shape_geek_empty_text)


            mainContainer.setBackgroundResource(if (isDark) R.drawable.tc_shape_geek_item_bg_dark else R.drawable.tc_shape_geek_item_bg)
            //Date
            tvDate.setTextColor(App.getColor(if (isDark) R.color.color_40ffffff else R.color.color_60000000))
//            tvDate.setBackgroundResource(if (isDark) R.drawable.shape_bento_item_bg_dark else R.drawable.shape_geek_item_bg)

            //title
            etTitle.setTextColor(App.getColor(if (isDark) R.color.white else R.color.black))
            etTitle.setHintTextColor(App.getColor(if (isDark) R.color.color_40ffffff else R.color.color_60000000))
//            etTitle.setBackgroundResource(if (isDark) R.drawable.shape_bento_item_bg_dark else R.drawable.shape_bento_item_bg)
            //content
            etContent.setTextColor(App.getColor(if (isDark) R.color.color_A0ffffff else R.color.black))
            etContent.setHintTextColor(App.getColor(if (isDark) R.color.color_40ffffff else R.color.color_60000000))
//            etContent.setBackgroundResource(if (isDark) R.drawable.shape_bento_item_bg_dark else R.drawable.shape_bento_item_bg)



            //author
            etAuthor.setTextColor(App.getColor(if (isDark) R.color.color_40ffffff else R.color.color_60000000))
            etAuthor.setHintTextColor(App.getColor(if (isDark) R.color.color_40ffffff else R.color.color_60000000))
//            etAuthor.setBackgroundResource(if (isDark) R.drawable.shape_bento_item_bg_dark else R.drawable.shape_bento_item_bg)
            //word
            tvWordCount.setTextColor(App.getColor(if (isDark) R.color.color_40ffffff else R.color.color_60000000))
//            tvWordCount.setBackgroundResource(if (isDark) R.drawable.shape_bento_item_bg_dark else R.drawable.shape_geek_item_bg)

            //qrcode
            qrCode.setBackgroundResource(if (isDark) R.drawable.tc_shape_geek_item_bg_dark else R.drawable.tc_shape_geek_item_bg)
            ivQrCode.setColorFilter(App.getColor(if (isDark) R.color.color_60ffffff else R.color.color_60000000))
            tvQrCodeTitle.setTextColor(App.getColor(if (isDark) R.color.color_60ffffff else R.color.color_60000000))
            tvQrCodeDesc.setTextColor(App.getColor(if (isDark) R.color.color_40ffffff else R.color.color_40000000))

            //water mark
            tvWater.setTextColor(App.getColor(if (isDark) R.color.color_60ffffff else R.color.color_60000000))
            waterMarkContainer.setBackgroundResource(if (isDark) R.drawable.tc_shape_geek_item_bg_dark else R.drawable.tc_shape_geek_item_bg)
            markEmpty1.setBackgroundResource(if (isDark) R.drawable.tc_shape_geek_empty_text_dark else R.drawable.tc_shape_geek_empty_text)
            markEmpty2.setBackgroundResource(if (isDark) R.drawable.tc_shape_geek_empty_text_dark else R.drawable.tc_shape_geek_empty_text)

            //bottom gradient
            val gradientEnd = TextCardCore.cardData.getBgColor().replace("#", "#")
            val gradientStart = "#00000000"
            bottomGradient.background =  GradientBackgroundDrawable().apply {
                setColorListGradient(
                    listOf(gradientStart, gradientEnd),
                    bottomGradient.width.toFloat(),
                    bottomGradient.height.toFloat()
                )
            }
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