package com.allever.video.editor.function.editor.action

import android.content.Context
import com.android.absbase.utils.DebugUtil
import com.android.absbase.utils.ResourcesUtils
import com.android.absbase.utils.ToastUtils
import com.allever.video.editor.R
import com.allever.video.editor.utils.MediaTypeUtil
import java.lang.ref.SoftReference

object ActionTips {
    val  resId_action_tips_prefix_clip = R.string.action_tips_prefix_clip
    val  resId_action_tips_prefix_music = R.string.action_tips_prefix_music
    val  resId_action_tips_prefix_music_volume = R.string.action_tips_prefix_music_volume
    val  resId_action_tips_prefix_format = R.string.action_tips_prefix_format
    val  resId_action_tips_prefix_text = R.string.action_tips_prefix_text
    val  resId_action_tips_prefix_font = R.string.action_tips_prefix_font
    val  resId_action_tips_prefix_opacity = R.string.action_tips_prefix_opacity
    val  resId_action_tips_prefix_align = R.string.action_tips_prefix_align
    val  resId_action_tips_prefix_sticker = R.string.action_tips_prefix_sticker
    val  resId_action_tips_suffix_add = R.string.action_tips_suffix_add
    val  resId_action_tips_suffix_move = R.string.action_tips_suffix_move
    val  resId_action_tips_suffix_rotate = R.string.action_tips_suffix_rotate
    val  resId_action_tips_suffix_trim = R.string.action_tips_suffix_trim
    val  resId_action_tips_suffix_remove = R.string.action_tips_suffix_remove
    val  resId_action_tips_suffix_replace = R.string.action_tips_suffix_replace
    val  resId_action_tips_suffix_change = R.string.action_tips_suffix_change
    val  resId_action_tips_suffix_input = R.string.action_tips_suffix_input
    val  resId_action_tips_suffix_color = R.string.action_tips_suffix_color
    val  resId_action_tips_suffix_left = R.string.action_tips_suffix_left
    val  resId_action_tips_suffix_center = R.string.action_tips_suffix_center
    val  resId_action_tips_suffix_right = R.string.action_tips_suffix_right
    val  resId_action_tips_suffix_background = R.string.action_tips_suffix_background
    val  resId_action_tips_suffix_exchange = R.string.action_tips_suffix_exchange
    val  resId_action_tips_suffix_unknown = R.string.action_tips_suffix_unknown


    private var stringSoftMaps = SoftReference<HashMap<Int, String>>(HashMap())

    private fun getString(resId: Int): String {
        var maps = stringSoftMaps.get()
        var tips: String? = null
        if (maps == null) {
            maps = HashMap()
            stringSoftMaps = SoftReference(maps)
        } else {
            tips = maps[resId]
        }
        if (tips == null) {
            tips = ResourcesUtils.getString(resId)
            maps[resId] = tips
        }
        return tips
    }

    /**
     * "{prefix}[{suffix}][{otherInfo}]"
     */
    fun makeTips(prefixResId: Int, suffixResId: Int? = null, otherInfo: String? = null): String {
        val prefix =
            getString(prefixResId)
        val suffix = if (suffixResId != null) {
            getString(suffixResId)
        } else ""
        val otherInfo = otherInfo ?: ""

        return "$prefix$suffix$otherInfo"
    }

    fun makeTipsByMediaType(mediaType: Int, suffixResId: Int? = null, otherInfo: String? = null): String? {
        val prefixResId = when (mediaType) {
            MediaTypeUtil.TYPE_VIDEO,
            MediaTypeUtil.TYPE_OTHER_IMAGE,
            MediaTypeUtil.TYPE_GIF,
            MediaTypeUtil.TYPE_PNG,
            MediaTypeUtil.TYPE_JPG
            -> {
                resId_action_tips_prefix_clip
            }
            MediaTypeUtil.TYPE_AUDIO -> {
                resId_action_tips_prefix_music
            }
            MediaTypeUtil.TYPE_TEXT -> {
                resId_action_tips_prefix_text
            }
            MediaTypeUtil.TYPE_STICKER -> {
                resId_action_tips_prefix_sticker
            }
            else -> {
                if (DebugUtil.isDebuggable()) {
                    return "Debug: mediaType: $mediaType, suffixResId: $suffixResId: otherInfo:$otherInfo"
                }
                null
            }
        }
        if (prefixResId == null) {
            return ""
        }
        return makeTips(
            prefixResId,
            suffixResId,
            otherInfo
        )
    }

    /**
     * 恢复
     */
//    fun toastRestore(action: Action) {
//        var tips = ""
//        when (action) {
//            is LayoutAction -> {
//                val ratio = action.ratio
//                if (ratio != null) {
//                    val format = getString(R.string.action_tips_prefix_format)
//                    tips = "$format${ratio.scaleFactorString}"
//                }
//            }
//            is ViewAction -> {
//
//            }
//            is AddEffectAction -> {
//
//            }
//            is DeleteEffectAction -> {
//
//            }
//            is SwapEffectAction -> {
//
//            }
//            is CropEffectAction -> {
//
//            }
//            is TextEffectAction -> {
//
//            }
//            is EffectAction -> {
//
//            }
//            else -> {
//
//            }
//        }
//    }

    /**
     * 还原
     */
//    fun toastRevert(action: Action) {
//
//    }

    fun toast(context: Context, tips: String?) {
        if (tips != null) {
            ToastUtils.show(context, tips)
        } else if (DebugUtil.isDebuggable()) {
            ToastUtils.show(context, "test: no tips")
        }
    }

}