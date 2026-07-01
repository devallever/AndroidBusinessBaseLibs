package com.allever.video.editor.function.editor.bean

import android.graphics.*
import com.allever.video.editor.ui.widget.gesture.IContentView
import java.util.*
import kotlin.math.max

/**
 * 特效列表, 记录了多个特效
 * 比如对视频进行裁剪或者移动后,需要同时调整多个bean的内容, 这时可以把多个bean记录到一起放入动作里进行回溯
 */
class EffectListBean : EffectBean() {
    val beans = arrayListOf<EffectBean>()

    /**
     * 最后一个视频播放完成不用回到视频开始位置
     */
    var lastWhetherEnd2Start = false

    /**
     * 总时长
     */
    val totalDuration: Long
        get() {
            var temp = 0L
            for (bean in beans) {
                if (bean.primary && bean.state == EffectBean.STATE_VALID) {
                    temp += bean.dstDuration
                }
            }
            return temp + secondExtraTime
        }
    /**
     * 副特效超出时长
     */
    var secondExtraTime: Long = 0L
    /**
     * 获取副特效超出总时长的时间戳
     * @param totalDuration 主特效的总时长
     */
    fun getExtraTime(totalDuration: Long): Long{
        var extraTime = 0L
        beans.map {
            if(it.type == EffectBean.STATE_DELETE || it.primary){
                return@map
            }
            val subExtraTime = it.videoTime.dstEndTime - totalDuration
            extraTime = max(subExtraTime,extraTime)
        }
        return extraTime
    }

    fun update() {
        var offset = 0L
        beans.map {
            if (it.primary && it.state == EffectBean.STATE_VALID) {
                it.whetherEnd2Start = true
                it.moveDst(offset)
                offset += it.dstDuration + 1
            }
        }
        beans.lastOrNull()?.whetherEnd2Start = lastWhetherEnd2Start
    }

    /**
     * 查找bean的下标
     */
    fun findBeanIndex(bean: EffectBean): Int {
        return findBeanIndex(bean.id)
    }

    fun findBeanIndex(id: Int): Int {
        for ((index, bean) in beans.withIndex()) {
            if (bean.id == id) {
                return index
            }
        }
        return -1
    }

    fun contains(bean: EffectBean): Boolean {
        return -1 != findBeanIndex(bean)
    }

    fun getSize(): Int {
        return beans.size
    }

    fun getEffectBean(id: Int): EffectBean? {
        for (bean in beans) {
            if (bean.id == id) {
                return bean
            }
        }
        return null
    }

    fun getIds(): List<Int> {
        return beans.map { it.id }
    }

    override fun getCropTotalWidth(singleWidth: Int, centerPadding: Int): Int {
        var temp = 0
        for (bean in beans) {
            if (bean.primary && bean.state != EffectBean.STATE_DELETE) {
                temp += bean.getCropTotalWidth(singleWidth, centerPadding)
            }
        }
        return temp
    }
    override fun getOriginalTotalWidth(singleWidth: Int, centerPadding: Int): Int {
        var temp = 0
        for (bean in beans) {
            if (bean.primary && bean.state != EffectBean.STATE_DELETE) {
                temp += bean.getOriginalTotalWidth(singleWidth, centerPadding)
            }
        }
        return temp
    }

    fun addAll(item: List<EffectBean>?) {
        item?.let {
            beans.addAll(it)
            update()
        }
    }

    /**
     * 添加一个特效, 默认添加到结尾
     */
    fun add(bean: EffectBean) {
        if (!beans.contains(bean)) {
            beans.add(bean)
            if (bean.primary) {
                update()
            }
        }
    }

    fun add(bean: EffectBean, index: Int) {
        if (!beans.contains(bean)) {
            beans.add(index, bean)
            if (bean.primary) {
                update()
            }
        }
    }

    /**
     * 删除一个特效, 默认从尾部开始删除
     */
    fun remove() {
        if (beans.size > 0) {
            val bean = beans.removeAt(beans.size - 1)
            if (bean.primary) {
                update()
            }
        }
    }

    fun remove(bean: EffectBean) {
        if (beans.contains(bean)) {
            beans.remove(bean)
            if (bean.primary) {
                update()
            }
        }
    }

    fun clear() {
        beans.clear()
        update()
    }

    override fun play(timeOffset: Long, view: IContentView?, isPlaying: Boolean) {
        for (bean in beans) {
            bean.play(timeOffset, view, isPlaying)
        }
    }

    override fun pause(view: IContentView?) {
        for (bean in beans) {
            bean.pause(view)
        }
    }

    override fun stop(view: IContentView?) {
        for (bean in beans) {
            bean.stop(view)
        }
    }

    override fun seekTo(timeOffset: Long, view: IContentView?, isPlaying: Boolean) {
        for (bean in beans) {
            bean.seekTo(timeOffset, view,isPlaying )
        }
    }

    override fun snapshot(timeOffset: Long, view: IContentView?): Bitmap? {
        view ?: return null
        val videoRect = view.videoRect
        val width = videoRect.width()
        val height = videoRect.height()
        if (width == 0 || height == 0) {
            return null
        }
        //以视频大小创建位图
        val outBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outBitmap)
        val paint = Paint()
        val paintFilter = PaintFlagsDrawFilter(0, Paint.FILTER_BITMAP_FLAG)
        //抗锯齿
        paint.isAntiAlias = true
        paint.isFilterBitmap = true
        canvas.drawFilter = paintFilter
        for (bean in beans) {
            val snapshot = bean.snapshot(timeOffset, view)
            if(snapshot != null){
                canvas.drawBitmap(snapshot,0f,0f , paint)
            }
        }
        return outBitmap
    }
    override fun clone(action: EffectBean): EffectBean {
        super.clone(action)
        (action as? EffectListBean)?.let {
            it.beans.clear()
            beans.forEach { bean ->
                it.beans.add(bean.clone())
            }
            it.update()
            it
        }
        return action
    }

    override fun clone(): EffectBean {
        return clone(EffectListBean())
    }

    override fun set(eb: EffectBean) {
        super.set(eb)
        (eb as? EffectListBean)?.let {
            it.beans.mapIndexed { index, effectBean ->
                if (index in 0 until beans.size) {
                    beans[index].set(effectBean)
                } else {
                    beans.add(effectBean)
                }
            }
            beans.removeAll(beans.subList(it.beans.size, beans.size))
            update()
            it
        }
    }


    /**
     * 交换 bean
     */
    fun swapEffectBean(from: Int, to: Int) {
        val fromBean = beans[from]
        val toBean = beans[to]
        if (from > to) {
            fromBean.moveDst(toBean.videoTime.dstStartTime)
            toBean.moveDst(fromBean.videoTime.dstEndTime)
        } else {
            toBean.moveDst(fromBean.videoTime.dstStartTime)
            fromBean.moveDst(toBean.videoTime.dstEndTime)
        }
        Collections.swap(beans, from, to)
        update()
    }

    /**
     * 切换特效 eg:  123  -> 231 -> 312
     * @param order 默认 倒叙
     */
    fun switchEffect(order: Boolean = true) {
        val size = beans.size
        if (size <= 1) {
            return
        }
        val tempBeans = arrayOfNulls<EffectBean>(beans.size)
        if (order) {
            System.arraycopy(beans.toArray(), 1, tempBeans, 0, size - 1)
            tempBeans[size - 1] = beans[0]
        } else {
            System.arraycopy(beans.toArray(), 0, tempBeans, 1, size - 1)
            tempBeans[0] = beans[size - 1]
        }
        beans.clear()
        val list = tempBeans.mapNotNull { it }
        beans.addAll(list)
        update()
    }

    /**
     * 切换某个特效到最后一位（默认）或第一位
     */
    fun switchEffect(effectBean: EffectBean, last: Boolean = true) {
        val size = beans.size
        if (size <= 1) {
            return
        }
        val beanIndex = findBeanIndex(effectBean)
        if (beanIndex < 0) {
            return
        }
        val tempBeans = arrayOfNulls<EffectBean>(beans.size)
        if(last){
            System.arraycopy(beans.toArray(), 0, tempBeans, 0, beanIndex)
            System.arraycopy(beans.toArray(), beanIndex + 1, tempBeans, beanIndex, size - 1 - beanIndex)
            tempBeans[size - 1] = effectBean
        }else{
            System.arraycopy(beans.toArray(), 0, tempBeans, 1, beanIndex)
            System.arraycopy(beans.toArray(), beanIndex + 1, tempBeans, beanIndex, size - 1 - beanIndex)
            tempBeans[0] = effectBean
        }
        beans.clear()
        val list = tempBeans.mapNotNull { it }
        beans.addAll(list)
        update()
    }

    override fun destroy() {
        super.destroy()

        beans.map {
            it.destroy()
        }
        beans.clear()
    }
}