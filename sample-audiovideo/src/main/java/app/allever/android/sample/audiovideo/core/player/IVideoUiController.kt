package app.allever.android.sample.audiovideo.core.player

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import app.allever.android.sample.audiovideo.lib.PlayerState

interface IVideoUiController {
    fun initView()
    fun getRootView(): View

    fun getTitleView(): TextView
    fun getBackView(): View

    fun getPlayPauseView(): ImageView
    fun getCurrentTimeView(): TextView
    fun getDurationView(): TextView
    fun getSeekBarView(): SeekBar

    fun getSpeedView(): TextView
    fun getLoopModeView(): ImageView
    fun getScaleModeView(): ImageView

    //触摸相关
    fun getVolumeOverlayView(): View
    fun getBrightnessOverlayView(): View
    fun getSeekOverlayView(): View
    fun getVolumeSeekBarView(): ProgressBar
    fun getBrightnessSeekBarView(): ProgressBar
    fun getSeekProgressTextView(): TextView
    fun getVolumeIconView(): ImageView
    fun getBrightnessIconView(): ImageView

    fun getControlPannerView(): View

    fun onStateChanged(old: PlayerState, state: PlayerState)
    fun onProgressChanged(position: Long, duration: Long, progress: Int)
    fun onVolumeProgressChanged(progress: Int)
    fun onBrightnessProgressChanged(progress: Int)
    fun onPlayStateChanged(isPlaying: Boolean)

}