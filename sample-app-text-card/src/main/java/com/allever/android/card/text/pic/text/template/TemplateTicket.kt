package com.allever.android.card.text.pic.text.template

import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.allever.android.card.text.pic.text.App
import com.allever.android.card.text.pic.text.R
import com.allever.android.card.text.pic.text.model.ColorData
import com.allever.android.card.text.pic.text.model.TemplateBgColor
import com.allever.android.card.text.pic.text.model.TemplateModel
import com.allever.android.card.text.pic.text.databinding.TcTemplateTicketBinding
import com.allever.android.card.text.pic.text.util.log
import com.allever.android.card.text.pic.text.view.widget.GradientBackgroundDrawable
import com.allever.android.card.text.pic.text.view.widget.GradientBackgroundRoundDrawable

class TemplateTicket : TemplateModel<TcTemplateTicketBinding>() {
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

    override fun inflateView() = TcTemplateTicketBinding.inflate(LayoutInflater.from(App.context))
    override fun getTemplateName(): String {
        return "Ticket"
    }

    override fun getTemplateCover(): Int {
        return R.drawable.icon_template_ticket
    }


    override fun getTemplateBgColor(): MutableList<TemplateBgColor> {
        //dark & light
        log("Ticket Bento create getTemplateBgColor")
        return colorList
    }

    override fun showOrHideIcon(show: Boolean) {
        mBinding?.ivIcon?.isVisible = show
        mBinding?.iconContainer?.isVisible = show
        mBinding?.iconBottomLine?.isVisible = show
    }

    override fun showOrHideDate(show: Boolean) {
        mBinding?.tvDate?.isVisible = show
        checkShowMainContainer()
    }

    override fun showOrHideTitle(show: Boolean) {
        mBinding?.etTitle?.isVisible = show
        checkShowContentBottomLine()
        checkShowMainContainer()
    }

    override fun showOrHideContent(show: Boolean) {
        mBinding?.etContent?.isVisible = show
        checkShowContentBottomLine()
        checkShowMainContainer()
    }

    private fun checkShowContentBottomLine() {
        mBinding?.apply {
            mainBottomLine.isVisible = etTitle.isVisible || etContent.isVisible
        }
    }

    private fun checkShowMainContainer() {
        mBinding?.apply {
            mainContainer.isVisible = etTitle.isVisible || etContent.isVisible || tvDate.isVisible
        }
    }

    override fun showOrHideAuthor(show: Boolean) {
        mBinding?.etAuthor?.isVisible = show
        checkShowAuthorContainer()
        checkShowAuthorBottomLine()
    }

    override fun showOrHideWordCount(show: Boolean) {
        mBinding?.tvWordCount?.isVisible = show
        checkShowAuthorContainer()
        checkShowAuthorBottomLine()
    }

    override fun showOrHideQrCode(show: Boolean) {
        mBinding?.qrCode?.isVisible = show
        checkShowAuthorBottomLine()
    }

    private fun checkShowAuthorContainer() {
        mBinding?.apply {
            authorContainer.isVisible = etAuthor.isVisible || tvWordCount.isVisible
        }
    }

    private fun checkShowAuthorBottomLine() {
        mBinding?.apply {
            authorLine.isVisible = qrCode.isVisible && authorContainer.isVisible
        }
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
//            val cardBgColor = ContextCompat.getColor(
//                App.context,
//                if (isDark) R.color.color_A0000000 else R.color.white
//            )
//            cardView.setCardBackgroundColor(cardBgColor)
            topBg.setBackgroundResource( if (isDark) R.drawable.tc_shape_ticket_top_bg_dark else R.drawable.tc_shape_ticket_top_bg)
            iconContainer.setBackgroundColor(App.getColor(if (isDark) R.color.template_ticket_card_bg_color_dark else R.color.template_ticket_card_bg_color))
            mainContainer.setBackgroundColor(App.getColor(if (isDark) R.color.template_ticket_card_bg_color_dark else R.color.template_ticket_card_bg_color))
            authorContainer.setBackgroundColor(App.getColor(if (isDark) R.color.template_ticket_card_bg_color_dark else R.color.template_ticket_card_bg_color))
            authorLine.setBackgroundColor(App.getColor(if (isDark) R.color.template_ticket_card_bg_color_dark else R.color.template_ticket_card_bg_color))
            qrCode.setBackgroundColor(App.getColor(if (isDark) R.color.template_ticket_card_bg_color_dark else R.color.template_ticket_card_bg_color))
            bottomBg.setBackgroundResource( if (isDark) R.drawable.tc_shape_ticket_bottom_bg_dark else R.drawable.tc_shape_ticket_bottom_bg)
            iconBottomLine.setImageResource(if (isDark) R.drawable.icon_ticket_bg_line_dark else R.drawable.icon_ticket_bg_line)
            mainBottomLine.setImageResource(if (isDark) R.drawable.icon_ticket_bg_line_dark else R.drawable.icon_ticket_bg_line)

            //icon
//            ivIcon.setBackgroundResource(if (isDark) R.drawable.shape100f17_r45 else R.drawable.shape_999999_r45)
            ivIcon.setColorFilter(
                ContextCompat.getColor(
                    App.context,
                    if (isDark) R.color.white else R.color.white
                )
            )

            //iconBg
            iconGradientBg.background = GradientBackgroundRoundDrawable().apply {
                setColorList(data.colorValue, iconGradientBg.width.toFloat(), iconGradientBg.height.toFloat())
            }

            //Date
            tvDate.setTextColor(App.getColor(if (isDark) R.color.color_60ffffff else R.color.color_60000000))

            //title
            etTitle.setTextColor(App.getColor(if (isDark) R.color.white else R.color.black))
            etTitle.setHintTextColor(App.getColor(if (isDark) R.color.color_60ffffff else R.color.color_80000000))
            //content
            etContent.setTextColor(App.getColor(if (isDark) R.color.white else R.color.black))
            etContent.setHintTextColor(App.getColor(if (isDark) R.color.color_60ffffff else R.color.color_60000000))

            //author
            etAuthor.setTextColor(App.getColor(if (isDark) R.color.color_60ffffff else R.color.color_60000000))
            etAuthor.setHintTextColor(App.getColor(if (isDark) R.color.color_60ffffff else R.color.color_60000000))
            //word
            tvWordCount.setTextColor(App.getColor(if (isDark) R.color.color_60ffffff else R.color.color_60000000))

            //qrcode
            ivQrCode.setColorFilter(App.getColor(if (isDark) R.color.color_60ffffff else R.color.color_80ffffff))
            tvQrCodeTitle.setTextColor(App.getColor(if (isDark) R.color.white else R.color.black))
            tvQrCodeDesc.setTextColor(App.getColor(if (isDark) R.color.color_60ffffff else R.color.color_60000000))

            //water mark
            tvWater.setTextColor(App.getColor(if (isDark) R.color.color_60ffffff else R.color.color_B0000000))

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