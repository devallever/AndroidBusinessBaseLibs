package com.allever.video.editor.function.editor.bean

import android.view.ViewGroup

class GifBean : EffectBean() {

    override fun clone(action: EffectBean): EffectBean {
        super.clone(action)
        (action as? GifBean)?.let {
            it
        }
        return action
    }

    override fun clone(): EffectBean {
        return clone(GifBean())
    }

    override fun set(eb: EffectBean) {
        super.set(eb)
        (eb as? GifBean)?.let {
            it
        }
    }

    companion object {
        fun getBean(obj: Any): GifBean {
            // 根据内容获取一个video bean
            // 比如传入一个视频文件路径,根据路径得到一个bean
            return GifBean()
        }
    }
}