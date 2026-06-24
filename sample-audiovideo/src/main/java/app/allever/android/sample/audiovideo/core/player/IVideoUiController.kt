package app.allever.android.sample.audiovideo.core.player

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import app.allever.android.sample.audiovideo.lib.LoopMode
import app.allever.android.sample.audiovideo.lib.PlayerState
import app.allever.android.sample.audiovideo.lib.VideoScaleMode

interface IVideoUiController {
    fun initView()
    fun getRootView(): View

    fun getTitleView(): TextView?
    fun getBackView(): View?

    fun getPlayPauseView(): ImageView?
    fun getCurrentTimeView(): TextView?
    fun getDurationView(): TextView?
    fun getSeekBarView(): SeekBar?

    fun getSpeedView(): TextView?
    fun getLoopModeView(): ImageView?
    fun getScaleModeView(): ImageView?

    //触摸相关
    fun getVolumeOverlayView(): View?
    fun getBrightnessOverlayView(): View?
    fun getSeekOverlayView(): View?
    fun getVolumeSeekBarView(): ProgressBar?
    fun getBrightnessSeekBarView(): ProgressBar?
    fun getSeekProgressTextView(): TextView?
    fun getVolumeIconView(): ImageView?
    fun getBrightnessIconView(): ImageView?

    fun getControlPannerView(): View?

    fun onTitleChanged(title: String)
    fun onStateChanged(old: PlayerState, state: PlayerState)
    fun onPlayStateChanged(isPlaying: Boolean)
    fun onProgressChanged(position: Long, duration: Long, progress: Int)
    fun onSpeedChanged(speed: Float)
    fun onScaleModeChanged(mode: VideoScaleMode)
    fun onLoopModeChanged(mode: LoopMode)

    fun onShowOrHideControlPanner(show: Boolean)
    fun onHideAllGestureOverlays()

    fun onGestureVolumeProgressChanged(volume: Float, progress: Int)
    fun onGestureBrightnessProgressChanged(progress: Int)
    fun onGestureProgressChanged(currentPosition: Long, startPosition: Long)

}