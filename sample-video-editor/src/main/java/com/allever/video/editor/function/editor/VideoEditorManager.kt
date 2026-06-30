package com.allever.video.editor.function.editor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.annotation.IntDef
import com.android.absbase.utils.DebugUtil
import com.allever.video.editor.ConfigManager
import com.allever.video.editor.function.Ratio
import com.allever.video.editor.function.editor.action.*
import com.allever.video.editor.function.editor.action.*
import com.allever.video.editor.function.editor.bean.EffectBean
import com.allever.video.editor.function.editor.bean.EffectListBean
import com.allever.video.editor.function.editor.bean.VideoBean
import com.allever.video.editor.function.media.MediaTypeUtil
import com.allever.video.editor.function.timeline.TimeLineController
import com.allever.video.editor.ui.widget.gesture.IContentView

class VideoEditorManager {


    /**
     * 生命周期状态
     */
    @LifeState
    private var mState: Int =
        ON_START
    /**
     * 上下文
     */
    private var mContext: Context? = null

    /**
     * 记录所有添加的bean, 包括已经删除的
     */
    private var id2beans = hashMapOf<Int, EffectBean>()

    /**
     * 当前特效, 用来save和play, 所有的编辑操作都需要调整该bean
     */
    var primaryBeans = EffectListBean()
        private set

    var secondaryBeans = EffectListBean()
        private set

    /**
     * 背景
     */
    var backgroundColor = Color.WHITE

    /**
     * 格子比例
     */
    var ratio: Ratio = Ratio.RATIO_ORIGINAL

    /**
     * 整个过程的时长
     */
    var duration: Long = 0

    /**
     * 当前时间轴的位置, 播放从该time开始
     */
    var timeOffset: Long = 0

    /**
     * 输出视频音量[ 0 - 255]
     */
    var volume: Int = 128

    /**
     * 时间轴控制器
     */
    var mTimeController: TimeLineController
        private set

    var actionManager: ActionController
        private set

    var onActionListener: ActionController.OnActionListener? = null

    constructor(context: Context) {
        /**
         * 初始化
         * 可以传入多个内容, 比如图片/视频等
         * 然后根据这些内容得到对应bean, duration根据传入的内容设定整个过程的时长
         * 如传入一个图片和1个9s的视频,则图片默认3s,总共12s
         */
        mContext = context
        mTimeController =
            TimeLineController(context, primaryBeans)
        actionManager = ActionController()
    }

    fun setEffectListBean(beans: EffectListBean) {
        mTimeController.setEffectListBean(beans)
        duration = beans.totalDuration
        beans.beans.map {
            id2beans[it.id] = it
        }
        primaryBeans.clear()
        primaryBeans.addAll(beans.beans)
        update()
    }

    fun update() {
        primaryBeans.update()
        secondaryBeans.update()
        val extraTime = secondaryBeans.getExtraTime(primaryBeans.totalDuration - primaryBeans.secondExtraTime)
        primaryBeans.secondExtraTime = extraTime
        mTimeController.update()
    }

    private fun addBeansInternal(bean: EffectBean, primary: Boolean) {
        if (primary) {
            addPrimaryBeansInternal(bean)
        } else {
            addSecondaryBeansInternal(bean)
        }
    }

    /**
     * 在时间轴的指定位置添加一个特效
     */
    private fun addPrimaryBeansInternal(bean: EffectBean) {
        val srcBean = id2beans[bean.id].let {
            if (it == null) {
                id2beans[bean.id] = bean
                bean
            } else {
                it.set(bean)
                it
            }
        }

        srcBean.primary = true
        primaryBeans.add(srcBean)

        update()
    }

    private fun addSecondaryBeansInternal(bean: EffectBean) {
        val srcBean = id2beans[bean.id].let {
            if (it == null) {
                id2beans[bean.id] = bean
                bean
            } else {
                it.set(bean)
                it
            }
        }

        srcBean.primary = false
        secondaryBeans.add(srcBean)

        update()
    }

    fun addBeans(bean: EffectBean, primary: Boolean) {
        if (primary) {
            addPrimaryBeans(bean)
        } else {
            addSecondaryBeans(bean)
        }
    }

    /**
     * 在时间轴的指定位置添加一个特效
     */
    fun addPrimaryBeans(bean: EffectBean) {
        addPrimaryBeansInternal(bean)

        val bean = bean.clone()
        bean.primary = true
        val effectAction = AddEffectAction(bean)
        action(effectAction)
    }

    fun addSecondaryBeans(bean: EffectBean) {
        addSecondaryBeansInternal(bean)

        val bean = bean.clone()
        bean.primary = false
        val effectAction = AddEffectAction(bean)
        action(effectAction)
    }

    fun findBeanIndex(bean: EffectBean): Int {
        val listBean = if (bean.primary) {
            primaryBeans
        } else {
            secondaryBeans
        }
        return listBean.findBeanIndex(bean)
    }

    fun findBeanIndex(id: Int): Int {
        val bean = id2beans[id]
        if (bean != null) {
            val listBean = if (bean.primary) {
                primaryBeans
            } else {
                secondaryBeans
            }
            return listBean.findBeanIndex(bean)
        }
        return -1
    }

    /**
     * 移除一个特效
     */
    private fun removeInternal(bean: EffectBean) {
        // 根据特效的情况来定, 如果是原始特效, 则可能会修改所有特效的时间
        bean.state = EffectBean.STATE_DELETE
        primaryBeans.remove(bean)
        secondaryBeans.remove(bean)
        update()
    }

    /**
     * 移除一个特效, 使用特效的id
     */
    private fun removeInternal(effectId: Int) {
        // 根据特效的情况来定, 如果是原始特效, 则可能会修改所有特效的时间
        val effectBean = id2beans[effectId]
        if (effectBean != null) {
            removeInternal(effectBean)
        }
    }

    /**
     * 移除一个特效
     */
    fun remove(bean: EffectBean) {
        removeInternal(bean)

        val effectAction = DeleteEffectAction(bean)
        effectAction.index = findBeanIndex(bean)
        action(effectAction)
    }

    /**
     * 移除一个特效, 使用特效的id
     */
    fun remove(effectId: Int) {
        val effectBean = id2beans[effectId]
        if (effectBean != null) {
            remove(effectBean)
        }
    }

    /**
     * 移除一个特效,使用指定时间上的特效
     */
    fun remove(timeOffset: Long) {
        // 根据特效的情况来定, 如果是原始特效, 则可能会修改所有特效的时间
        // TODO:
        update()
    }

    /**
     * 移动特效,
     */
    fun move(bean: EffectBean, dstStartTime: Long, dstEndTime: Long? = null) {
        bean.moveDst(dstStartTime, dstEndTime)
        update()
    }

    fun action(action: Action) {
        actionManager.action(action)
        onActionListener?.onActionStateChange()
    }

    /**
     * 前进
     */
    fun actionRestore(context: Context, vararg hosts: IApplyAction?) {
        val action = actionManager.restore()
        ActionTips.toast(context, action?.restoreTips)
        val applyAction = when (action) {
            is AddEffectAction -> {
                val effectBean = action.effectBean
                if (effectBean != null) {
                    effectBean.state = EffectBean.STATE_VALID
                    addBeansInternal(effectBean, effectBean.primary)
                }
                action
            }
            is DeleteEffectAction -> {
                val effectBean = action.effectBean
                if (effectBean != null) {
                    effectBean.state = EffectBean.STATE_DELETE
                    removeInternal(effectBean.id)
                }
                action
            }
            is SingleEffectAction -> {
                action.currentObj = action.obj
                action
            }
            is MultiEffectAction -> {
                action.currentObj = action.obj
                action
            }
            is SwapEffectAction -> {
                swapBeansInternal(action.ids)
                action
            }
            is EffectAction -> {
                val effectBean = action.effectBean
                if (effectBean != null) {
                    val id = effectBean.id
                    val bean = getBean(id)
                    bean?.set(effectBean)
                }
                action
            }
            is ViewAction -> {
                action
            }
            else -> {
                action
            }
        }
        hosts.map {
            it?.applyAction(applyAction)
        }
        update()
        onActionListener?.onActionStateChange()
    }

    /**
     * 后退
     */
    fun actionRevert(context: Context, vararg hosts: IApplyAction?) {
        val action = actionManager.revert()
        ActionTips.toast(context, action?.revertTips)
        val applyAction = when (action) {
            is AddEffectAction -> {
                val effectBean = action.effectBean
                if (effectBean != null) {
                    effectBean.state = EffectBean.STATE_DELETE
                    removeInternal(effectBean.id)
                }
                action
            }
            is DeleteEffectAction -> {
                val effectBean = action.effectBean
                if (effectBean != null) {
                    effectBean.state = EffectBean.STATE_VALID
                    addBeansInternal(effectBean, effectBean.primary)
                }
                action
            }
            is SingleEffectAction -> {
                action.currentObj = action.prevObj
                action
            }
            is MultiEffectAction -> {
                action.currentObj = action.prevObj
                action
            }
            is SwapEffectAction -> {
                swapBeansInternal(action.prevIds)
                action
            }
            is EffectAction -> {
                val effectBean = action.effectBean
                if (effectBean != null) {
                    val id = effectBean.id
                    val bean = getBean(id)
                    bean?.set(effectBean)

                }
                action
            }
            is ViewAction -> {
                action.prevAction
            }
            else -> {
                action?.prevAction
            }
        }
        hosts.map {
            it?.applyAction(applyAction)
        }
        update()
        onActionListener?.onActionStateChange()
    }

    /**
     * 可以前进
     */
    fun canActionRestore(): Boolean {
        return actionManager.canForward()
    }

    /**
     * 可以后退
     */
    fun canActionRevert(): Boolean {
        return actionManager.canBackward()
    }

    fun addListener(listener: TimeLineController.TimeDispatchEventByControllerListener? = null): VideoEditorManager {
        mTimeController.addListener(listener)
        return this
    }

    fun removeListener(listener: TimeLineController.TimeDispatchEventByControllerListener): VideoEditorManager {
        mTimeController.removeListener(listener)
        return this
    }

    fun isPlaying(): Boolean {
        return mTimeController.isPlaying
    }

    fun isNotPause(): Boolean {
        return mTimeController.isNotPause
    }
    /**
     * 在指定view上播放特效, 需要根据调研结果来决定怎么play
     */
    fun play(timeOffset: Long, view: IContentView, autoPlay: Boolean = true) {
        mTimeController.start(autoPlay)
        if (autoPlay) {
            primaryBeans.play(timeOffset, view,mTimeController.isPlaying )
            secondaryBeans.play(timeOffset, view,mTimeController.isPlaying )
        } else {
            primaryBeans.seekTo(timeOffset, view, mTimeController.isPlaying)
            secondaryBeans.seekTo(timeOffset, view, mTimeController.isPlaying)
        }
    }

    fun pause(view: IContentView) {
        if (isNotPause()) {
            mTimeController.pause()
            primaryBeans.pause(view)
            secondaryBeans.pause(view)
        }
    }

    fun stop(view: IContentView) {
        primaryBeans.stop(view)
        secondaryBeans.stop(view)
    }

    /**
     * 输出特效, 需要根据调研结果来决定怎么save
     */
    fun save() {

    }

    /**
     * 根据特效id获取对应bean, 如在对各个特效进行点击时,根据对应id来区分
     * 一般从currentBeans中就可以获取到
     */
    fun getBean(id: Int): EffectBean? {
        return primaryBeans.getEffectBean(id) ?: secondaryBeans.getEffectBean(id)
    }

    /**
     * 裁剪特效原始时间
     */
    fun cropSrcTimeByBean(bean: EffectBean, newStartTime: Long, newEndTime: Long?) {
        val prevAction =
            CropEffectAction(bean.clone())

        bean.moveSrc(newStartTime, newEndTime)
        bean.moveDst(newStartTime, newEndTime)
        update()

        val effectAction =
            CropEffectAction(bean.clone())
        effectAction.prevAction = prevAction
        action(effectAction)

    }

    /**
     * 移动特效到指定时间
     */
    fun moveDstByBean(bean: EffectBean, startTime: Long?, endTime: Long? = null) {
        bean.moveDst(startTime, endTime)
        update()
    }

    fun moveDstByBeanBy(bean: EffectBean, startOffset: Long? = null, endOffset: Long? = null) {
        bean.moveDstBy(startOffset, endOffset)
        update()
    }

    fun moveSrcByBean(bean: EffectBean, newStartTime: Long?, newEndTime: Long? = null) {
        bean.moveSrc(newStartTime, newEndTime)
        update()
    }

    fun moveSrcByBeanBy(bean: EffectBean, startOffset: Long? = null, endOffset: Long? = null) {
        bean.moveSrcBy(startOffset, endOffset)
        update()
    }

    private fun swapBeansInternal(ids: List<Int>): Boolean {
        if (primaryBeans.getSize() != ids.size) {
            if (DebugUtil.isDebuggable()) {
                throw RuntimeException("swap beans: different size")
            }
            return false
        }
        var different = false
        val srcIds = primaryBeans.getIds()

        srcIds.mapIndexed { index, id ->
            if (id != ids[index]) {
                different = true
            }
        }
        if (different) {
            val beans = ids.mapNotNull {
                id2beans[it]
            }
            val effectListBean = EffectListBean()
            effectListBean.addAll(beans)
            setEffectListBean(effectListBean)
        }
        return different
    }

    /**
     * 交换主特效位置, 只对主特效有效
     */
    fun swapBeans(ids: List<Int>): Boolean {
        val srcIds = primaryBeans.getIds()
        val swap = swapBeansInternal(ids)
        if (swap) {
            val action = SwapEffectAction()
            action.prevIds = srcIds
            action.ids = ids
            action(action)
        }
        return swap
    }

    /**
     * 获取指定时间点的快照
     */
    fun snapshot(timeOffset: Long, view: IContentView?): Bitmap? {
        val effectListBean = EffectListBean()
        effectListBean.addAll(primaryBeans.beans)
        effectListBean.addAll(secondaryBeans.beans)
        val snapshot = effectListBean.snapshot(timeOffset, view)
        effectListBean.clear()
        return snapshot
    }

    fun onStart() {
        mState = ON_START
    }

    fun onResume() {
        mState = ON_RESUME
    }

    fun onPause() {
        mState = ON_PAUSE
    }

    fun onStop() {
        mState = ON_STOP
    }

    /**
     * 释放
     */
    fun onDestroy() {
        mState = ON_DESTROY
    }

    /**
     * 滚动的时候再启动时间轴
     */
    fun onTimelineStart() {
        mTimeController.onTimelineStart()
    }

    /**
     * 滚动时间轴
     */
    fun onTimelineOffset(timeOffset: Long) {
        mTimeController.onTimelineOffset(timeOffset)
    }

    /**
     * 滚动的时候暂停时间轴
     */
    fun onTimelinePause() {
        mTimeController.onTimelinePause()
    }

    /**
     * 当前时间轴的位置(从视频起点开始计算)
     */
    fun getCurrentTimelineIndex(): Long {
        return mTimeController.currentTimelineIndex
    }

    companion object {


        @IntDef(
            ON_START,
            ON_RESUME,
            ON_PAUSE,
            ON_STOP,
            ON_DESTROY
        )
        @Retention(AnnotationRetention.SOURCE)
        annotation class LifeState

        const val ON_START = 0
        const val ON_RESUME = 1
        const val ON_PAUSE = 2
        const val ON_STOP = 3
        const val ON_DESTROY = 4

        /**
         * 静态特效的默认时长
         */
        var staticEffectDuration
            get() = ConfigManager.staticEffectDuration
            set(value) {
                ConfigManager.staticEffectDuration = value
            }

        /**
         * 图片资源默认时长 3s
         */
        var staticImageDuration
            get() = ConfigManager.staticImageDuration
            set(value) {
                ConfigManager.staticImageDuration = value
            }

        fun formatTime(time: Long): String {
            var min = (time / (1000 * 60)).toString() + ""
            var sec = (time % (1000 * 60)).toString() + ""
            if (min.length < 2) {
                min = "0" + time / (1000 * 60) + ""
            } else {
                min = (time / (1000 * 60)).toString() + ""
            }
            if (sec.length == 4) {
                sec = "0" + time % (1000 * 60) + ""
            } else if (sec.length == 3) {
                sec = "00" + time % (1000 * 60) + ""
            } else if (sec.length == 2) {
                sec = "000" + time % (1000 * 60) + ""
            } else if (sec.length == 1) {
                sec = "0000" + time % (1000 * 60) + ""
            }
            return min + ":" + sec.trim { it <= ' ' }.substring(0, 2)
        }
    }

    /**
     * 返回有效视频的个数
     */
    fun getVideoCount(): Int{
        val primaryList = primaryBeans.beans.filter {
            it.state != EffectBean.STATE_DELETE && it.type == MediaTypeUtil.TYPE_VIDEO
        }
        val secondList = secondaryBeans.beans.filter {
            it.state != EffectBean.STATE_DELETE && it.type == MediaTypeUtil.TYPE_VIDEO
        }
        return primaryList.size + secondList.size
    }


    /**
     * 设置视频音量
     * @param volume [0 - 255]
     */
    fun setVideoVolume(volume: Int, view: IContentView?) {
        this.volume = volume
        for (bean in primaryBeans.beans) {
            if (bean is VideoBean) {
                bean.setVideoVolume(volume, view)
            }
        }
    }

    fun destroy() {
        id2beans.clear()
        primaryBeans.destroy()
        secondaryBeans.destroy()
        mTimeController.destroy()
        actionManager.destroy()
        onActionListener = null
    }

    /**
     * 分割
     */
    interface SpiteListListener {
        fun onStart()
        fun onSuccess()
    }
}