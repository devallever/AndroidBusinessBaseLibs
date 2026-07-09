package app.android.gp.ai.translator.function

import android.media.MediaPlayer
import app.android.gp.ai.translator.event.PlayAudioFinishEventB
import app.woejt.wwzdndgl.lib.util.log
import app.woejt.wwzdndgl.lib.util.logRandomString
import org.greenrobot.eventbus.EventBus

object MediaHelper {

    private var mPlayer: MediaPlayer? = null

    private var mIsPlaying = false
    fun playFile(path: String) {
        logRandomString()
        try {
            if (mPlayer?.isPlaying == true) {
                mPlayer?.stop()

            }
            logRandomString()
            mPlayer?.release()
            mPlayer = null
            logRandomString()
            mPlayer = MediaPlayer()
            logRandomString()
            mPlayer?.setDataSource(path)
            logRandomString()
            mPlayer?.setOnPreparedListener {
                it.start()
                mIsPlaying = true
                log("play start")
            }
            mPlayer?.prepare()
            logRandomString()
            mPlayer?.setOnCompletionListener {
                log("play finish")
                mIsPlaying = false
                logRandomString()
                EventBus.getDefault().post(PlayAudioFinishEventB())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isPlaying(): Boolean {
        return mPlayer?.isPlaying == true
    }
}