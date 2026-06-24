package app.allever.android.sample.audiovideo.core.player

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.view.isVisible
import app.allever.android.sample.audiovideo.R
import app.allever.android.sample.audiovideo.databinding.CustomStdUiControllerBinding
import app.allever.android.sample.audiovideo.lib.LoopMode
import app.allever.android.sample.audiovideo.lib.PlayerState
import app.allever.android.sample.audiovideo.lib.VideoScaleMode
import java.util.Locale

class CustomStdVideoController(val context: Context, ) : IVideoUiController {

    private val binding =
        CustomStdUiControllerBinding.inflate(LayoutInflater.from(context), null, false)
    override fun initView() {
        binding.ivPlayPauseCenter.setOnClickListener {
            binding.ivPlayPause.performClick()
        }

    }

    override fun getRootView(): View = binding.root

    override fun getTitleView(): TextView? {
        return binding.tvTitle
    }

    override fun getBackView(): View? {
        return binding.ivVPBack
    }

    override fun getPlayPauseView(): ImageView? {
        return binding.ivPlayPause
    }

    override fun getCurrentTimeView(): TextView? {
        return binding.tvCurrentTime
    }

    override fun getDurationView(): TextView? {
        return binding.tvDuration
    }

    override fun getSeekBarView(): SeekBar {
        return binding.seekBar
    }

    override fun getSpeedView(): TextView? {
        return binding.tvSpeed
    }

    override fun getLoopModeView(): ImageView? {
        return binding.ivVPLoopMode
    }

    override fun getScaleModeView(): ImageView? {
        return binding.ivVPScaleMode
    }

    override fun getVolumeOverlayView(): View? {
        return binding.gestureVolumeContainer
    }

    override fun getBrightnessOverlayView(): View? {
        return binding.gestureBrightnessContainer
    }

    override fun getSeekOverlayView(): View? {
        return binding.tvGestureSeekTime
    }

    override fun getVolumeSeekBarView(): ProgressBar? {
        return binding.pbVolume
    }

    override fun getBrightnessSeekBarView(): ProgressBar? {
        return binding.pbBrightness
    }

    override fun getSeekProgressTextView(): TextView? {
        return binding.tvGestureSeekTime
    }

    override fun getVolumeIconView(): ImageView? {
        return binding.ivVolumeIcon
    }

    override fun getBrightnessIconView(): ImageView? {
        return binding.ivBrightnessIcon
    }

    override fun getControlPannerView(): View? {
        return binding.controlPanel
    }

    override fun onTitleChanged(title: String) {
        binding.tvTitle.text = title
    }

    override fun onStateChanged(
        old: PlayerState,
        state: PlayerState
    ) {
    }

    override fun onProgressChanged(
        position: Long, duration: Long, progress: Int
    ) {
        binding.seekBar.progress = progress
        binding.tvCurrentTime.text = formatTime( position)
        binding.tvDuration.text = formatTime(duration)
    }

    @SuppressLint("SetTextI18n")
    override fun onSpeedChanged(speed: Float) {
        binding.tvSpeed.text  = "${speed}x"
    }

    override fun onScaleModeChanged(mode: VideoScaleMode) {
        val iconRes = when (mode) {
            VideoScaleMode.FIT_CENTER -> R.drawable.ic_scale_fit
            VideoScaleMode.CROP_CENTER -> R.drawable.ic_scale_crop
            VideoScaleMode.STRETCH -> R.drawable.ic_scale_stretch
        }
        binding.ivVPScaleMode.setImageResource(iconRes)
    }

    override fun onLoopModeChanged(mode: LoopMode) {
        val iconRes = when (mode) {
            LoopMode.NONE -> R.drawable.ic_loop_none
            LoopMode.SINGLE -> R.drawable.ic_loop_single
            LoopMode.ALL -> R.drawable.ic_loop_all
        }
        binding.ivVPLoopMode.setImageResource(iconRes)
    }

    override fun onShowOrHideControlPanner(show: Boolean) {
        binding.controlPanel.isVisible = show
    }

    override fun onHideAllGestureOverlays() {
        binding.gestureVolumeContainer.animate()?.alpha(0f)?.withEndAction {
            binding.gestureVolumeContainer.visibility = GONE
        }?.start()

        binding.gestureBrightnessContainer.animate()?.alpha(0f)?.withEndAction {
            binding.gestureBrightnessContainer.visibility = GONE
        }?.start()

        binding.tvGestureSeekTime.animate()?.alpha(0f)?.withEndAction {
            binding.tvGestureSeekTime.visibility = GONE
        }?.start()
    }

    override fun onGestureVolumeProgressChanged(volume: Float, progress: Int) {
        binding.gestureVolumeContainer.visibility = VISIBLE
        binding.gestureVolumeContainer.alpha = 1f
        binding.pbVolume.progress = progress
        // 根据音量更新图标
        val iconRes = when {
            volume <= 0f -> R.drawable.ic_volume_mute
            volume < 0.33f -> R.drawable.ic_volume_low
            volume < 0.66f -> R.drawable.ic_volume_medium
            else -> R.drawable.ic_volume_up
        }
        binding.ivVolumeIcon.setImageResource(iconRes)
    }

    override fun onGestureBrightnessProgressChanged(progress: Int) {
        binding.gestureBrightnessContainer.visibility = VISIBLE
        binding.gestureBrightnessContainer.alpha = 1f
        binding.pbBrightness.progress = progress
    }

    override fun onGestureProgressChanged(currentPosition: Long, startPosition: Long) {
        binding.tvGestureSeekTime.visibility = VISIBLE
        binding.tvGestureSeekTime.alpha = 1f


        // 显示格式：时间差\n当前时间
        binding.tvGestureSeekTime.text = formatTime(currentPosition)
    }

    override fun onPlayStateChanged(isPlaying: Boolean) {
        binding.ivPlayPauseCenter.setImageResource(if (isPlaying) R.drawable.ic_sample_video_player_view_pause else R.drawable.ic_sample_video_player_view_play)
    }

    private fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}