package app.allever.android.sample.audiovideo.core.player

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import app.allever.android.sample.audiovideo.R
import app.allever.android.sample.audiovideo.databinding.CustomStdUiControllerBinding
import app.allever.android.sample.audiovideo.lib.PlayerState

class CustomStdVideoController(val context: Context, ) : IVideoUiController {

    private val binding =
        CustomStdUiControllerBinding.inflate(LayoutInflater.from(context), null, false)
    override fun initView() {
        binding.ivPlayPauseCenter.setOnClickListener {

        }

    }

    override fun getRootView(): View = binding.root

    override fun getTitleView(): TextView {
        return binding.tvTitle
    }

    override fun getBackView(): View {
        return binding.ivVPBack
    }

    override fun getPlayPauseView(): ImageView {
        return binding.ivPlayPause
    }

    override fun getCurrentTimeView(): TextView {
        return binding.tvCurrentTime
    }

    override fun getDurationView(): TextView {
        return binding.tvDuration
    }

    override fun getSeekBarView(): SeekBar {
        return binding.seekBar
    }

    override fun getSpeedView(): TextView {
        return binding.tvSpeed
    }

    override fun getLoopModeView(): ImageView {
        return binding.ivVPLoopMode
    }

    override fun getScaleModeView(): ImageView {
        return binding.ivVPScaleMode
    }

    override fun getVolumeOverlayView(): View {
        return binding.gestureVolumeContainer
    }

    override fun getBrightnessOverlayView(): View {
        return binding.gestureBrightnessContainer
    }

    override fun getSeekOverlayView(): View {
        return binding.tvGestureSeekTime
    }

    override fun getVolumeSeekBarView(): ProgressBar {
        return binding.pbVolume
    }

    override fun getBrightnessSeekBarView(): ProgressBar {
        return binding.pbBrightness
    }

    override fun getSeekProgressTextView(): TextView {
        return binding.tvGestureSeekTime
    }

    override fun getVolumeIconView(): ImageView {
        return binding.ivVolumeIcon
    }

    override fun getBrightnessIconView(): ImageView {
        return binding.ivBrightnessIcon
    }

    override fun getControlPannerView(): View {
        return binding.controlPanel
    }

    override fun onStateChanged(
        old: PlayerState,
        state: PlayerState
    ) {
    }

    override fun onProgressChanged(
        position: Long,
        duration: Long,
        progress: Int
    ) {
    }

    override fun onVolumeProgressChanged(progress: Int) {
    }

    override fun onBrightnessProgressChanged(progress: Int) {
    }

    override fun onPlayStateChanged(isPlaying: Boolean) {
        binding.ivPlayPauseCenter.setImageResource(if (isPlaying) R.drawable.ic_sample_video_player_view_pause else R.drawable.ic_sample_video_player_view_play)
    }
}