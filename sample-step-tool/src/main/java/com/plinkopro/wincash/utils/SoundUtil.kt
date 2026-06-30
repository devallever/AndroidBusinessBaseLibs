package com.plinkopro.wincash.utils

import android.content.Context
import android.media.SoundPool
import androidx.annotation.RawRes
import com.plinkopro.wincash.R
import com.plinkopro.wincash.base.BaseApplication

object SoundUtil {

    var isOpenSound: Boolean = true
        set(value){
            if (field != value){
                field = value
                SpUtil.put(SpKey.IS_SOUND_OPEN, value)
            }
        }


    private val soundPool: SoundPool by lazy {  SoundPool.Builder().setMaxStreams(5).build() }

    private val appContext: Context = BaseApplication.instance

    private val soundMap = HashMap<Int, Int>()



    /**
     * @param repeatTime  循环模式：0表示循环一次，-1表示一直循环，其他表示数字+1表示当前数字对应的循环次数
     */
    fun play(@RawRes resId: Int, repeatTime: Int = 0) {
        if (!isOpenSound) return

        val soundID = soundPool.load(appContext, resId, 1)
        // 该方法防止sample not ready错误
        soundPool.setOnLoadCompleteListener { soundPool: SoundPool, sampleId: Int, status: Int ->
            val streamId = soundPool.play(
                soundID,  //声音id
                1f,  //左声道：0.0f ~ 1.0f
                1f,  //右声道：0.0f ~ 1.0f
                1,  //播放优先级：0表示最低优先级
                repeatTime,  //循环模式：0表示循环一次，-1表示一直循环，其他表示数字+1表示当前数字对应的循环次
                1f
            ) //播放速度：1是正常，范围从0~2
            soundMap[resId] = streamId
        }
    }


    fun pause(@RawRes resId: Int) {
        val mStreamID = soundMap[resId]
        if (mStreamID != null) {
            soundPool.pause(mStreamID)
        }
    }

    fun stop(@RawRes resId: Int) {
        val mStreamID = soundMap[resId]
        if (mStreamID != null) {
            soundPool.stop(mStreamID)
        }
    }


    fun resume(@RawRes resId: Int) {
        val mStreamID = soundMap[resId]
        if (mStreamID != null) {
            soundPool.resume(mStreamID)
        }
    }


//    fun release() {
//        soundPool.autoPause()
//        soundPool.release()
//    }

}


enum class SoundRawId(@RawRes val id:Int){
    CLICK(R.raw.click),
    NAIL(R.raw.nail),
    CATCH(R.raw.catch_coin),
    GET_REWARD(R.raw.get_reward),
    REWARD_DIALOG(R.raw.reward_dialog),
    LOTTO_MATCH_NUM(R.raw.lotto_match_num),
    LOTTO_SHAKE_BALL(R.raw.lotto_shake_ball)

}