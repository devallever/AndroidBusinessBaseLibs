package app.android.gp.ai.translator.function

import android.media.MediaPlayer
import app.android.gp.ai.translator.event.PlayAudioFinishEventB
import app.allever.android.lib.core.ext.log
import org.greenrobot.eventbus.EventBus

object MediaHelper {

    private var mPlayer: MediaPlayer? = null

    private var mIsPlaying = false
    fun playFile(path: String) {
        
        try {
            if (mPlayer?.isPlaying == true) {
                mPlayer?.stop()

            }
            
            mPlayer?.release()
            mPlayer = null
            
            mPlayer = MediaPlayer()
            
            mPlayer?.setDataSource(path)
            
            mPlayer?.setOnPreparedListener {
                it.start()
                mIsPlaying = true
                log("play start")
            }
            mPlayer?.prepare()
            
            mPlayer?.setOnCompletionListener {
                log("play finish")
                mIsPlaying = false
                
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