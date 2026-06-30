package com.allever.video.editor.function.editor.bean

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import com.android.absbase.utils.ResourcesUtils
import com.allever.video.editor.R
import com.allever.video.editor.function.editor.VideoEditorManager
import com.allever.video.editor.ui.widget.EffectTextView
import com.allever.video.editor.ui.widget.gesture.IContentView
import com.allever.video.editor.utils.KeyboardUtils
import com.allever.video.editor.utils.MediaTypeUtil
import kotlin.random.Random

class TextBean : EffectBean() {
    /**
     * 默认高度
     */
    var textHeight =  ResourcesUtils.getDimension(R.dimen.video_edit_frame_text_item_height).toInt()
    var padding =  ResourcesUtils.getDimension(R.dimen.video_edit_frame_text_item_padding).toInt()

    var text: String = ""
    var hint: String = ""
    /**
     * 字体, 考虑是strnig还是其他格式, 字体可以是本地字体文件/系统字体/网络字体等
     */
    var typeface: String = ""

    var textColor: Int = ResourcesUtils.getColor(R.color.video_edit_frame_add_effect_text_color)

    var hintColor: Int = ResourcesUtils.getColor(R.color.video_edit_frame_add_effect_text_hint_color)

    /**
     * 光标颜色
     */
    var cursorColor: Int = ResourcesUtils.getColor(R.color.video_edit_frame_add_effect_text_cursor_color)



    var backgroundType: Int = TEXT_BACKGROUND_TYPE_NOMAL

    /**
     * 是否是粗体
     */
    var fakeBoldText: Boolean = true

    var textSize: Float = ResourcesUtils.getDimension(R.dimen.video_edit_frame_text_item_font_size)

    /**
     * 透明度, 0 - 100
     */
    var alpha: Int = 100

    /**
     * 字体对齐默认值
     */
    var gravity: Int = Gravity.START
    /**
     * 字体对齐
     */
    var allGravities = arrayOf(Gravity.START,Gravity.CENTER,Gravity.END)
    var allGravityDrawable = arrayOf(R.drawable.icon_edit_font_left,R.drawable.icon_edit_font_middle,R.drawable.icon_edit_font_right)
    /**
     * 字体
     */
    var fontName: String = ""

    /**
     * 字体路径
     */
    var localFontPath: String = ""

    override fun clone(action: EffectBean): EffectBean {
        super.clone(action)
        (action as? TextBean)?.let {
            it.textHeight = textHeight
            it.padding = padding
            it.text = text
            it.hint = hint
            it.typeface = typeface
            it.textColor = textColor
            it.hintColor = hintColor
            it.cursorColor = cursorColor
            it.backgroundType = backgroundType
            it.fakeBoldText = fakeBoldText
            it.textSize = textSize
            it.alpha = alpha
            it.gravity = gravity
            it.allGravities = allGravities
            it.fontName = fontName
            it.localFontPath = localFontPath
            it
        }
        return action
    }

    override fun clone(): EffectBean {
        return clone(TextBean())
    }

    override fun set(eb: EffectBean) {
        super.set(eb)
        (eb as? TextBean)?.let {
            textHeight = it.textHeight
            padding = it.padding
            text = it.text
            hint = it.hint
            typeface = it.typeface
            textColor = it.textColor
            hintColor = it.hintColor
            cursorColor = it.cursorColor
            backgroundType = it.backgroundType
            fakeBoldText = it.fakeBoldText
            textSize = it.textSize
            alpha = it.alpha
            gravity = it.gravity
            allGravities = it.allGravities
            fontName = it.fontName
            localFontPath = it.localFontPath
            it
        }
    }


    override fun refresh(view: IContentView) {
        val effectView = view.getEffectView(this) as? EffectTextView
        if (effectView != null) {
            effectView.setText(this.text)
            if(this.text.isEmpty()){
                effectView.hint = this.hint
            }
            effectView.setHintTextColor(this.hintColor)
            effectView.paint.isFakeBoldText = this.fakeBoldText
            effectView.setTextSize(TypedValue.COMPLEX_UNIT_PX, this.textSize)
            if(effectView.gravity != this.gravity){
                effectView.reset()
                effectView.gravity = this.gravity
            }
            effectView.setPadding(this.padding, this.padding, this.padding, this.padding)
            effectView.setCursorColor(this.cursorColor)
            effectView.alpha = EffectBean.range(this.alpha, 0.0f, 1.0f)

            when(this.backgroundType) {
                TEXT_BACKGROUND_TYPE_NOMAL -> {
                    effectView.setHollowMode(false)
                    effectView.setTextColor(this.textColor)
                    effectView.setHintTextColor(this.hintColor)
                    effectView.setBackgroundColor(Color.TRANSPARENT)
                }
                TEXT_BACKGROUND_TYPE_INVERTED -> {
                    effectView.setHollowMode(true)
//                    effectView.setTextColor(Color.TRANSPARENT)
                    effectView.setBackgroundColor(this.textColor)
                }
                TEXT_BACKGROUND_TYPE_GRAY_BOTTOM -> {
                    effectView.setHollowMode(false)
                    effectView.setTextColor(this.textColor)
                    effectView.setHintTextColor(this.hintColor)
                    effectView.setBackgroundColor(ResourcesUtils.getColor(R.color.video_edit_frame_add_effect_text_background_gray_color))
                }
            }
            //将光标移至文字末尾
            effectView.setSelection(text.length)
            if (!TextUtils.isEmpty(this.localFontPath)) {
                effectView.setFontName(this.localFontPath, true)
            } else {
                effectView.setFontName(this.fontName)
            }
        }
    }

    override fun seekTo(timeOffset: Long, view: IContentView?, isPlaying: Boolean) {
        val effectView = view?.getEffectView(this)
        effectView?.let {
            if(it is EffectTextView) {
                if (inTimeLine(timeOffset) && state != EffectBean.STATE_DELETE) {
                    it.visibility = View.VISIBLE
                } else {
                    // 这里如果使用GONE,会出现获取坐标问题
                    // bug: 选择2个图片进入编辑, 直接点击save,第二个图片无法save,原因为GONE后获取的坐标错误
                    it.visibility = View.INVISIBLE
                    view?.invalidateSelf()
                    it.isCursorVisible = false
                    KeyboardUtils.hideKeyboard(it)
                    it
                }
                it.prevVisibility = it.visibility
            }
        }
    }
    private fun viewConversionBitmap(view: View): Bitmap {
        val w = view.width
        val h = view.height
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        view.layout(0, 0, w, h)
        view.draw(canvas)
        return bmp
    }

    override fun getOriginalBitmap(view: IContentView?, timeOffset: Long): Bitmap? {
        val effectView = view?.getEffectView(this)
        if (effectView != null) {
            return viewConversionBitmap(effectView)
        }
        return null
    }


    companion object {
        /**
         * 默认, 字色透明底
         */
        const val TEXT_BACKGROUND_TYPE_NOMAL = 0
        /**
         * 反色, 背景色,透明字
         */
        const val TEXT_BACKGROUND_TYPE_INVERTED = 1
        /**
         * 字色灰底
         */
        const val TEXT_BACKGROUND_TYPE_GRAY_BOTTOM = 2


        fun getBean(text: String): TextBean {
            return getBean(text, 0)
        }

        fun getBean(text: String, timeLineOffset: Long): TextBean {
            val textBean = TextBean()
            textBean.text = text
            textBean.hint = ResourcesUtils.getString(R.string.effect_text_bean_default_content_tip)
            textBean.primary = false
            val duration = VideoEditorManager.staticImageDuration
            val size = textBean.textHeight.toFloat()
            textBean.position.set(0f, 0f, size, size)
            textBean.type = MediaTypeUtil.TYPE_TEXT
            textBean.videoTime.dstStartTime = timeLineOffset
            textBean.videoTime.dstEndTime = timeLineOffset + duration
            textBean.videoTime.srcStartTime = 0
            textBean.videoTime.srcEndTime = duration
            textBean.allowExpand = true
            textBean.duration = duration
            textBean.smallIcon = ResourcesUtils.getDrawable(R.drawable.icon_edit_font_s)
            textBean.labelStartColor = Color.parseColor("#4e9cf3")
            textBean.labelEndColor = Color.parseColor("#3c8ae1")
            val random = Random(System.currentTimeMillis())
            return textBean
        }
    }
}