package com.allever.video.editor.ui.widget

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.android.absbase.utils.DeviceUtils
import com.android.absbase.utils.ResourcesUtils
import com.allever.video.editor.R
import com.allever.video.editor.function.editor.bean.EffectBean
import com.allever.video.editor.function.editor.bean.TextBean
import com.allever.video.editor.utils.MediaTypeUtil

object TimeLineViewFactory {

    /**
     * 更新显示内容
     */
    fun updateView(timeLineView: TimeLineView, effectBean: EffectBean, speed:Float = 0f, height:Int? = null) {
        timeLineView.effectBean = effectBean
        val view = getView(
            effectBean,
            timeLineView.context
        ) ?: return
        timeLineView.contentView = view
        timeLineView.interceptContentContainerEvent = true
        when (effectBean.type) {
            MediaTypeUtil.TYPE_JPG, MediaTypeUtil.TYPE_OTHER_IMAGE, MediaTypeUtil.TYPE_PNG,
            MediaTypeUtil.TYPE_VIDEO -> {
                if (view is BitmapContentView) {
                    view.setData(effectBean)
                }
            }
            MediaTypeUtil.TYPE_AUDIO,
            MediaTypeUtil.TYPE_STICKER,
            MediaTypeUtil.TYPE_TEXT -> {
                if (view is TextView && effectBean is TextBean) {
                    view.text = effectBean.text
                }
            }
            else -> {
            }
        }
        val width = (speed * effectBean.dstDuration).toInt()
        val timelinePadding = ResourcesUtils.getDimension(R.dimen.effect_edit_timeline_padding).toInt()
        val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams.setMargins(0, timelinePadding, 0, timelinePadding)
        view.layoutParams = layoutParams
        val endColor = effectBean.labelEndColor
        if(endColor != null){
            view.setBackgroundColor(endColor)
        }
        timeLineView.setContentViewSize(width, height, timelinePadding)
        timeLineView.showFrame(true, ResourcesUtils.getColor(R.color.main_color_tone))
    }

    /**
     * 获取view
     */
    private fun getView(effectBean: EffectBean, context: Context): View? {
        var stateView: View? = null
        when (effectBean.type) {
            MediaTypeUtil.TYPE_JPG, MediaTypeUtil.TYPE_OTHER_IMAGE, MediaTypeUtil.TYPE_PNG,
            MediaTypeUtil.TYPE_VIDEO -> {
                val view = BitmapContentView(context)
                stateView = view
            }
            MediaTypeUtil.TYPE_STICKER,
            MediaTypeUtil.TYPE_TEXT -> {
                val view = TextView(context)
                view.setTextColor(ResourcesUtils.getColor(R.color.effect_edit_timeline_text_bean_color))
                view.setTextSize(TypedValue.COMPLEX_UNIT_PX, ResourcesUtils.getDimension(R.dimen.time_line_view_item_fun_effect_text_size))
                stateView = view
            }
            MediaTypeUtil.TYPE_AUDIO -> {
                val soundTrimView = SoundTrimView(context)
                soundTrimView.setSelectTimeColor(ResourcesUtils.getColor(R.color.video_edit_frame_add_effect_music_select_time_color))
                soundTrimView.selectHeight = DeviceUtils.dip2pxF(36f)
                soundTrimView.needEdit = false
                soundTrimView.duration = effectBean.dstDuration
                soundTrimView.startTime = effectBean.videoTime.srcStartTime
                soundTrimView.endTime = effectBean.videoTime.srcEndTime
                stateView = soundTrimView
            }
            else -> {
            }
        }
        if (stateView != null) {
            val padding = ResourcesUtils.getDimension(R.dimen.time_line_view_item_fun_effect_text_padding).toInt()
            stateView.setPadding(padding, padding, padding, padding)
        }
        return stateView
    }
}