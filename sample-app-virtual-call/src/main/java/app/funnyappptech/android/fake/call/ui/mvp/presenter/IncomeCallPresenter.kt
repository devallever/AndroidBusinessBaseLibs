package org.xm.app.virtual.call.ui.mvp.presenter

import android.content.Context.VIBRATOR_SERVICE
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Vibrator
import app.allever.android.lib.core.app.App
import org.xm.app.virtual.call.mvp.BasePresenter
import org.xm.app.virtual.call.function.SettingHelper
import org.xm.app.virtual.call.ui.mvp.view.IncomeCallView

class IncomeCallPresenter : BasePresenter<IncomeCallView>() {
    private lateinit var mMediaPlayer: MediaPlayer
    private lateinit var mVibrator: Vibrator
    private val vibratorParameters = longArrayOf(1000L, 3000L)

    fun initInComeCall() {
        try {
            val uriString = SettingHelper.getRingtoneUri()
            val ringtoneUri = if (uriString?.isEmpty() == true) {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            } else {
                Uri.parse(uriString)
            }
            mMediaPlayer = MediaPlayer()
            mMediaPlayer.setDataSource(App.context, ringtoneUri)
            mMediaPlayer.prepare()
            mMediaPlayer.isLooping = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mVibrator = App.context.getSystemService(VIBRATOR_SERVICE) as Vibrator

    }

    fun play() {
        mMediaPlayer.start()
        if (SettingHelper.getVibrator()) {
            mVibrator.vibrate(vibratorParameters, 0)
        }
    }

    fun stop() {
        mMediaPlayer.stop()
        mVibrator.cancel()
    }

    fun destroy() {
        mMediaPlayer.release()
    }
}