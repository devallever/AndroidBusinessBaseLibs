package com.allever.video.editor.function.save

import android.content.Context
import androidx.annotation.IntDef
import com.allever.video.editor.function.editor.bean.EffectListBean

class SaveController  {
    /**
     * 上下文
     */
    private var mContext: Context? = null
    /**
     * 保存视频的质量
     */
    @Quality
    private var quality = QUALITY_NORMAL

    /**
     * 开始编辑时的beans, 一把情况不需要进行修改
     */
    private var originalBeans: EffectListBean = EffectListBean()


    /**
     * 当前特效, 用来save和play, 所有的编辑操作都需要调整该bean
     */
    private var currentBeans = EffectListBean()




    constructor(context: Context) {
        /**
         * 初始化
         * 可以传入多个内容, 比如图片/视频等
         * 然后根据这些内容得到对应bean, duration根据传入的内容设定整个过程的时长
         * 如传入一个图片和1个9s的视频,则图片默认3s,总共12s
         */
        mContext = context
    }

    fun setEffectListBean( beans: EffectListBean){
        originalBeans.addAll(beans.beans)
        currentBeans.addAll(beans.beans)
    }

    companion object {
        @IntDef(QUALITY_NORMAL, QUALITY_HD, QUALITY_1080P)
        @Retention(AnnotationRetention.SOURCE)
        annotation class Quality
        const val QUALITY_NORMAL = 0
        const val QUALITY_HD = 1
        const val QUALITY_1080P = 2
    }
}
