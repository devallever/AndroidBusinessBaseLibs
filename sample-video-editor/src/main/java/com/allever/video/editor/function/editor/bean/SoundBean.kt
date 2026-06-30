package com.allever.video.editor.function.editor.bean

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import com.android.absbase.utils.ResourcesUtils
import com.allever.video.editor.R
import com.allever.video.editor.function.music.SongInfo
import com.allever.video.editor.function.music.SongMediaPlayer
import com.allever.video.editor.ui.widget.gesture.IContentView
import com.allever.video.editor.utils.MediaTypeUtil
import kotlin.random.Random

class SoundBean : EffectBean(), SongMediaPlayer.OnPlayerListener {
    /**
     * 声音音量[ 0 - 255]
     */
    var volume: Int = 128
        set(value) {
            field = value
            player?.setVolume(value)
        }

    /**
     * 声音名字(歌名或文件名)
     */
    var name: String = ""

    /**
     * 音乐播放器
     */
    var player: SongMediaPlayer? = null

    var soundInfo: SongInfo? = null

    /**
     * 初始化并准备音乐
     */
    fun prepare(force: Boolean = false) {
        val needLoad = if (player == null) {
            player = SongMediaPlayer(this)
            true
        } else force
        if (needLoad) {
            path?.let { player?.load(it) }
        }
    }

    override fun clone(action: EffectBean): EffectBean {
        super.clone(action)
        (action as? SoundBean)?.let {
            it.volume = volume
            it.name = name
            it.player = player
            it
        }
        return action
    }

    override fun clone(): EffectBean {
        return clone(SoundBean())
    }

    override fun set(eb: EffectBean) {
        super.set(eb)
        (eb as? SoundBean)?.let {
            volume = it.volume
            name = it.name
            player = it.player
            it
        }
    }


    override fun play(timeOffset: Long, view: IContentView?, isPlaying: Boolean) {
        player?.let {
            if (inTimeLine(timeOffset) && state != EffectBean.STATE_DELETE && isPlaying) {
                val duration = videoTime.srcEndTime - videoTime.srcStartTime
                if (!it.isPlaying()) {
                    it.seekTo(((timeOffset.toInt() - videoTime.dstStartTime) % duration + videoTime.srcStartTime).toInt())
                    it.resume()
                } else {
                    val currentPosition = it.getCurrentPosition()
                    if (currentPosition >= videoTime.srcEndTime) {
                        it.seekTo(((timeOffset.toInt() - videoTime.dstStartTime) % duration + videoTime.srcStartTime).toInt())
                    }
                }
            } else {
                if (it.isPlaying()) {
                    it.pause()
                }
            }
        }
    }
    override fun pause(view: IContentView?) {
        player?.let {
            it.pause()
        }
    }

    override fun stop(view: IContentView?) {
        player?.let {
            it.stop()
        }
    }
    override fun seekTo(timeOffset: Long, view: IContentView?, isPlaying: Boolean) {
        player?.let {
            if (inTimeLine(timeOffset) && state != EffectBean.STATE_DELETE) {
                if (!it.isPlaying()) {
                    it.seekTo((timeOffset.toInt() - videoTime.dstStartTime).toInt())
                }
            } else {
                if (it.isPlaying()) {
                    it.pause()
                }
            }
        }
    }

    override fun release() {
        player?.release()
    }

    fun replace(songInfo: SongInfo) {
        soundInfo = songInfo
        uri = Uri.parse(songInfo.path)
        path = songInfo.path
        type = MediaTypeUtil.TYPE_AUDIO
        name = songInfo.title
        val duration = songInfo.duration
        videoTime.dstEndTime =  videoTime.dstStartTime + duration
        videoTime.srcStartTime = 0
        videoTime.srcEndTime = duration
        prepare(true)
    }

    override fun onPrepared() {
    }

    override fun onCompletion() {
    }

    override fun onError(err: String) {
    }

    override fun onProgress(time: Int) {
        // 下面会出现死循环
//        if (time >= videoTime.srcEndTime) {
//            val seekTo = ((time - videoTime.dstStartTime) % dstDuration + videoTime.srcStartTime).toInt()
//            player?.seekTo(seekTo)
//        }
    }
    override fun snapshot(timeOffset: Long, view: IContentView?): Bitmap? {
        return null
    }

    override fun destroy() {
        super.destroy()

        player?.release()
        player = null
        soundInfo = null

    }

    companion object {
        fun getBean(obj: Any): SoundBean {
            return getBean(obj,0)
        }

        fun getBean(obj: Any,timeLineOffset: Long): SoundBean {
            val soundBean = SoundBean()
            if(obj is SongInfo){
                soundBean.soundInfo = obj
                soundBean.primary = false
                soundBean.uri = Uri.parse(obj.path)
                soundBean.path = obj.path
                soundBean.type = MediaTypeUtil.TYPE_AUDIO
                soundBean.name = obj.title
                val duration = obj.duration
                soundBean.smallIcon = ResourcesUtils.getDrawable(R.drawable.icon_edit_music_s)
                soundBean.labelStartColor = Color.parseColor("#b38ef3")
                soundBean.labelEndColor = Color.parseColor("#9d77e4")
                val random = Random(System.currentTimeMillis())
                soundBean.videoTime.dstStartTime = timeLineOffset
                soundBean.videoTime.dstEndTime =  timeLineOffset + duration
                soundBean.videoTime.srcStartTime = 0
                soundBean.videoTime.srcEndTime = duration
                soundBean.duration = duration
                soundBean.allowExpand = false
                soundBean.prepare()
            }
            return soundBean
        }
    }
}