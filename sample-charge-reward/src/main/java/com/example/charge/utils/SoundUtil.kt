package com.example.charge.utils

import android.media.SoundPool
import androidx.annotation.RawRes
import com.example.charge.ChargeApp
import com.example.charge.R

object SoundUtil {

    // 从 SpUtil 读取&保存开关状态
    var isOpenSound: Boolean
        get() = SpUtil.get(SpKey.IS_SOUND_OPEN, true)
        set(value) {
            if (value != isOpenSound) {
                SpUtil.put(SpKey.IS_SOUND_OPEN, value)
            }
        }

    // 支持最多 10 路并发
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(10)
        .build()

    private val appContext = ChargeApp.instance

    /** resId -> soundId（音效资源只 load 一次） */
    private val resIdToSoundId = HashMap<Int, Int>()

    /** resId -> 所有正在/已播放的 streamId（方便 stop/pause/resume） */
    private val resIdToStreamIds = HashMap<Int, MutableList<Int>>()

    /** soundId -> 等待在 load 完成后自动播放的 loop 列表 */
    private val pendingPlayMap = HashMap<Int, MutableList<Int>>()

    init {
        // 统一处理异步 load 完成
        soundPool.setOnLoadCompleteListener { sp, sampleId, status ->
            if (status != 0) return@setOnLoadCompleteListener

            val loops = pendingPlayMap.remove(sampleId) ?: return@setOnLoadCompleteListener

            // 找到这个 soundId 对应的 resId（反向查表）
            val resId = resIdToSoundId.entries.firstOrNull { it.value == sampleId }?.key

            loops.forEach { loop ->
                val streamId = sp.play(
                    sampleId,
                    1f,
                    1f,
                    1,
                    loop,
                    1f
                )
                if (resId != null && streamId != 0) {
                    resIdToStreamIds.getOrPut(resId) { mutableListOf() }.add(streamId)
                }
            }
        }
    }

    /**
     * 播放音效（可以短时间多次调用，同一 resId 会并发多路播放）
     * @param repeatTime 循环模式：0 播一次，-1 一直循环，其他：数字+1 次
     */
    fun play(@RawRes resId: Int, repeatTime: Int = 0) {
        if (!isOpenSound) return

        val soundId = resIdToSoundId[resId]
        if (soundId != null) {
            // 已经 load 过，直接播放
            val streamId = soundPool.play(
                soundId,
                1f,
                1f,
                1,
                repeatTime,
                1f
            )
            if (streamId != 0) {
                resIdToStreamIds.getOrPut(resId) { mutableListOf() }.add(streamId)
            }
        } else {
            // 还没 load，先 load，再在 onLoadComplete 里自动播放
            val loadId = soundPool.load(appContext, resId, 1)
            resIdToSoundId[resId] = loadId
            pendingPlayMap.getOrPut(loadId) { mutableListOf() }.add(repeatTime)
        }
    }

    fun pause(@RawRes resId: Int) {
        resIdToStreamIds[resId]?.forEach { id ->
            soundPool.pause(id)
        }
    }

    fun stop(@RawRes resId: Int) {
        resIdToStreamIds[resId]?.forEach { id ->
            soundPool.stop(id)
        }
        resIdToStreamIds[resId]?.clear()
    }

    fun resume(@RawRes resId: Int) {
        resIdToStreamIds[resId]?.forEach { id ->
            soundPool.resume(id)
        }
    }

    // 如果需要全局释放，在退出游戏时可以调用
    fun release() {
        soundPool.autoPause()
        soundPool.release()
        resIdToSoundId.clear()
        resIdToStreamIds.clear()
        pendingPlayMap.clear()
    }
}

/** 你之前的枚举可以照用 */
enum class SoundRawId(@RawRes val id: Int) {
    CLICK(R.raw.click),
    GET_REWARD(R.raw.get_reward),
}
