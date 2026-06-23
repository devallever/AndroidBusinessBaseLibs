package app.allever.android.sample.audiovideo.core.player

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import app.allever.android.lib.core.app.App
import app.allever.android.sample.audiovideo.R
import app.allever.android.sample.audiovideo.databinding.StdUiControllerBinding
import app.allever.android.sample.audiovideo.lib.PlayerState
import java.util.Locale

class StdVideoController(val context: Context) : IVideoUiController {

    private val binding =
        StdUiControllerBinding.inflate(LayoutInflater.from(context), null, false)

    override fun inflateViews() {

    }

    override fun getRootView(): View {
        return binding.root
    }

    override fun getTitleView(): TextView {
        return binding.tvVPTitle
    }

    override fun getBackView(): View {
        return binding.ivVPBack
    }

    override fun getPlayPauseView(): ImageView {
        return binding.ivVPPlayPause
    }

    override fun getCurrentTimeView(): TextView {
        return binding.tvVPProgress
    }

    override fun getDurationView(): TextView {
        return binding.tvVPDuration
    }

    override fun getSeekBarView(): SeekBar {
        return binding.seekBarVP
    }

    override fun getSpeedView(): TextView {
        return binding.tvVPSpeed
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
        return binding.gestureSeekContainer
    }

    override fun getVolumeSeekBarView(): ProgressBar {
        return binding.volumeProgressBar
    }

    override fun getBrightnessSeekBarView(): ProgressBar {
        return binding.brightnessProgressBar
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

    override fun onStateChanged(old: PlayerState, state: PlayerState) {

    }

    override fun onProgressChanged(
        position: Long, duration: Long, progress: Int
    ) {
        binding.seekBarVP.progress = progress
        binding.tvVPProgress.text = formatTime( position)
        binding.tvVPDuration.text = formatTime(duration)
    }

    override fun onVolumeProgressChanged(progress: Int) {
        binding.volumeProgressBar.progress = progress
    }

    override fun onBrightnessProgressChanged(progress: Int) {
        binding.brightnessProgressBar.progress = progress
    }

    override fun onPlayStateChanged(isPlaying: Boolean) {
        binding.ivVPPlayPause.setImageResource(if (isPlaying) R.drawable.ic_sample_video_player_view_pause else R.drawable.ic_sample_video_player_view_play)
    }

    /**
     * 格式化时间显示
     */
    private fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

}