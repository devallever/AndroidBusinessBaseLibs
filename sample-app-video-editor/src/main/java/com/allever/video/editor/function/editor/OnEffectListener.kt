package com.allever.video.editor.function.editor

import com.allever.video.editor.function.editor.bean.EffectBean

/**
 * 特效选择监听
 */
interface OnEffectSelectListener {
    /**
     * 选择主特效
     */
    fun onSelectPrimaryEffect(effectBean: EffectBean)

    /**
     * 选择副特效
     */
    fun onSelectSecondaryEffect(effectBean: EffectBean)

    /**
     * 点击空白
     */
    fun onNoSelect()

    /**
     * 拖动调整时间，手指松开刷新页面
     */
    fun onDragTimeLineViewEndUp()
}

/**
 * 特效编辑监听
 */
interface OnEffectEditListener {
    /**
     * 裁剪特效在时间轴上的时间
     */
    fun trimEffectInTimeLine(effectBean: EffectBean, startTime: Long, endTime: Long?)

    fun trimSecondEffectInTimeLine()
    /**
     * 裁剪特效自身的时间
     */
    fun trimEffectInSelf(effectBean: EffectBean, startTime: Long, endTime: Long)

    fun addEffect(effectBean: EffectBean, primary:Boolean = true)

    fun requestAddEffect()

    /**
     * 删除特效
     */
    fun deleteEffect(effectBean: EffectBean)

    /**
     * 改变特效位置, 一般针对主特效
     */
    fun changeEffectPosition(beans: List<EffectBean>)
}

interface OnEffectStateChangeListener {
    fun onTimelineStart()
    fun onTimelineOffset(timeOffset: Long)
    fun onTimelinePause()
}