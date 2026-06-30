package com.allever.video.editor.function.editor.action

import android.text.TextUtils
import android.view.Gravity
import com.allever.video.editor.function.Ratio
import com.allever.video.editor.function.editor.bean.TextBean
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class ActionController {
    private val actions: Vector<Action> = Vector()
    private var currentActionIndex = AtomicInteger(-1)

    fun makeTips(action: Action) {
        when (action) {
            is SingleEffectAction -> {
                val prevObj = action.prevObj
                val obj = action.obj
                when (action.type) {
                    ActionType.TYPE_REPLACE -> {
                        val effectBean = action.effectBean
                        if (effectBean != null) {
                            action.revertTips =
                                ActionTips.makeTipsByMediaType(
                                    effectBean.type,
                                    ActionTips.resId_action_tips_suffix_replace
                                )
                            action.restoreTips = action.revertTips
                        }
                    }
                    ActionType.TYPE_LAYOUT_BACKGROUND -> {
                        action.revertTips =
                            ActionTips.makeTips(
                                ActionTips.resId_action_tips_prefix_clip,
                                ActionTips.resId_action_tips_suffix_background
                            )
                        action.restoreTips = action.revertTips
                    }
                    ActionType.TYPE_FORMAT -> {
                        action.revertTips =
                            ActionTips.makeTips(
                                ActionTips.resId_action_tips_prefix_format,
                                null,
                                "${(prevObj as? Ratio)?.scaleFactorString}"
                            )
                        action.restoreTips =
                            ActionTips.makeTips(
                                ActionTips.resId_action_tips_prefix_format,
                                null,
                                "${(obj as? Ratio)?.scaleFactorString}"
                            )
                    }
                    ActionType.TYPE_TEXT_INPUT -> {
                        action.restoreTips =
                            ActionTips.makeTips(
                                ActionTips.resId_action_tips_prefix_text,
                                ActionTips.resId_action_tips_suffix_input
                            )
                        action.revertTips = action.restoreTips
                    }
                    ActionType.TYPE_VOLUME_CHANGE -> {
                        action.restoreTips =
                            ActionTips.makeTipsByMediaType(
                                ActionTips.resId_action_tips_prefix_music_volume,
                                ActionTips.resId_action_tips_suffix_change
                            )
                        action.revertTips = action.restoreTips
                    }
                    ActionType.TYPE_COLOR -> {
                        if (action.effectBean is TextBean) {
                            action.restoreTips =
                                ActionTips.makeTips(
                                    ActionTips.resId_action_tips_prefix_text,
                                    ActionTips.resId_action_tips_suffix_color
                                )
                            action.revertTips = action.restoreTips
                        }
                    }
                    ActionType.TYPE_OPACITY -> {
                        if (action.effectBean is TextBean) {
                            action.restoreTips =
                                ActionTips.makeTips(
                                    ActionTips.resId_action_tips_prefix_opacity,
                                    null, "${obj as Int}"
                                )
                            action.revertTips =
                                ActionTips.makeTips(
                                    ActionTips.resId_action_tips_prefix_opacity,
                                    null, "${prevObj as Int}"
                                )
                        }
                    }
                    ActionType.TYPE_TEXT_ALIGN -> {
                        if (action.effectBean is TextBean) {
                            val getResId: (Int) -> Int = { gravity ->
                                when (gravity) {
                                    Gravity.START -> {
                                        ActionTips.resId_action_tips_suffix_left
                                    }
                                    Gravity.CENTER -> {
                                        ActionTips.resId_action_tips_suffix_center
                                    }
                                    Gravity.END -> {
                                        ActionTips.resId_action_tips_suffix_right
                                    }
                                    else -> {
                                        ActionTips.resId_action_tips_suffix_unknown
                                    }
                                }
                            }
                            action.restoreTips =
                                ActionTips.makeTips(
                                    ActionTips.resId_action_tips_prefix_align,
                                    getResId(obj as Int)
                                )
                            action.revertTips =
                                ActionTips.makeTips(
                                    ActionTips.resId_action_tips_prefix_align,
                                    getResId(prevObj as Int)
                                )
                        }
                    }
                    ActionType.TYPE_TEXT_BACKGROUND -> {
                        when (action.effectBean) {
                            is TextBean -> {
                                action.restoreTips =
                                    ActionTips.makeTips(
                                        ActionTips.resId_action_tips_prefix_text,
                                        ActionTips.resId_action_tips_suffix_background
                                    )
                                action.revertTips = action.restoreTips
                            }
                        }
                    }
                }
            }
            is MultiEffectAction -> {
                val prevObj = action.prevObj
                val obj = action.obj
                when (action.type) {
                    ActionType.TYPE_FONT_CHANGE -> {
                        val prevLocalePath = prevObj?.get(0) as? String
                        val prevFontName = prevObj?.get(1) as? String
                        val currentLocalePath = obj?.get(0) as? String
                        val currentFontName = obj?.get(1) as? String
                        var prevInfo = prevLocalePath ?: prevFontName ?: "unknown"
                        var currentInfo = currentLocalePath ?: currentFontName ?: "unknown"
                        if (prevInfo.isEmpty()) {
                            prevInfo = "Default"
                        }
                        if (currentInfo.isEmpty()) {
                            currentInfo = "Default"
                        }
                        action.restoreTips =
                            ActionTips.makeTips(
                                ActionTips.resId_action_tips_prefix_font,
                                null, currentInfo
                            )
                        action.revertTips =
                            ActionTips.makeTips(
                                ActionTips.resId_action_tips_prefix_text,
                                null, prevInfo
                            )
                    }
                }
            }
            is ViewAction -> {
                val bean = action.effectBean
                if (bean != null) {
                    val suffixResId = if (action.type == ActionType.TYPE_MOVE) {
                        ActionTips.resId_action_tips_suffix_move
                    } else if (action.type == ActionType.TYPE_ROTATE) {
                        ActionTips.resId_action_tips_suffix_rotate
                    } else {
                        ActionTips.resId_action_tips_suffix_move
                    }
                    action.revertTips =
                        ActionTips.makeTipsByMediaType(
                            bean.type,
                            suffixResId
                        )
                    action.restoreTips = action.revertTips
                }
            }
            is AddEffectAction -> {
                val bean = action.effectBean
                if (bean != null) {
                    action.restoreTips =
                        ActionTips.makeTipsByMediaType(
                            bean.type,
                            ActionTips.resId_action_tips_suffix_add
                        )
                    action.revertTips = action.restoreTips
                }
            }
            is DeleteEffectAction -> {
                val bean = action.effectBean
                if (bean != null) {
                    action.restoreTips =
                        ActionTips.makeTipsByMediaType(
                            bean.type,
                            ActionTips.resId_action_tips_suffix_remove
                        )
                    action.revertTips = action.restoreTips
                }
            }
            is SwapEffectAction -> {
                action.restoreTips =
                    ActionTips.makeTips(
                        ActionTips.resId_action_tips_prefix_clip,
                        ActionTips.resId_action_tips_suffix_exchange
                    )
                action.revertTips = action.restoreTips
            }
            is CropEffectAction -> {
                val bean = action.effectBean
                if (bean != null) {
                    action.restoreTips =
                        ActionTips.makeTipsByMediaType(
                            bean.type,
                            ActionTips.resId_action_tips_suffix_trim
                        )
                    action.revertTips = action.restoreTips
                }
            }
            is EffectAction -> {
            }
        }
    }

    fun action(action: Action) {
        val index = currentActionIndex.get()
        for (i in actions.size - 1 downTo index + 1) {
            val removeAction = actions.removeAt(i)
            when (removeAction) {
            }
        }
        for (i in actions.size - 1 downTo 0) {
            val prevAction = actions[i]
            if (action.equalsByAction(prevAction)) {
                action.prevAction = prevAction
                break
            }
        }
        makeTips(action)
        actions.add(action)
        currentActionIndex.incrementAndGet()
    }

    /**
     * 恢复
     */
    fun restore(): Action? {
        var index = currentActionIndex.get()
        if (index >= actions.size) {
            return null
        }
        index = currentActionIndex.incrementAndGet()
        val action = actions[index]
        return action
    }

    /**
     * 还原
     */
    fun revert(): Action? {
        val index = currentActionIndex.get()
        if (index !in 0 until actions.size) {
            return null
        }
        val action = actions[index]
        currentActionIndex.decrementAndGet()
        return action
    }

    /**
     * 可以前进
     */
    fun canForward(): Boolean {
        return currentActionIndex.get() + 1 < actions.size
    }

    /**
     * 可以后退
     */
    fun canBackward(): Boolean {
        return currentActionIndex.get() in 0 until actions.size
    }

    fun destroy() {
        actions.clear()
    }

    interface OnActionListener {
        fun onActionStateChange()
    }

    interface OnAddActionListener {
        fun addAction(action: Action)
    }
}