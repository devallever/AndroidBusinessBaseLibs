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
import app.allever.android.sample.audiovideo.databinding.StdUiControllerBinding
import app.allever.android.sample.audiovideo.lib.LoopMode
import app.allever.android.sample.audiovideo.lib.PlayerState
import app.allever.android.sample.audiovideo.lib.VideoScaleMode
import java.util.Locale
import kotlin.math.abs

class StdVideoController(val context: Context) : IVideoUiController {

    private val binding =
        StdUiControllerBinding.inflate(LayoutInflater.from(context), null, false)

    override fun initView() {

    }

    override fun getRootView(): View {
        return binding.root
    }

    override fun getTitleView(): TextView? {
        return binding.tvVPTitle
    }

    override fun getBackView(): View? {
        return binding.ivVPBack
    }

    override fun getPlayPauseView(): ImageView? {
        return binding.ivVPPlayPause
    }

    override fun getCurrentTimeView(): TextView? {
        return binding.tvVPProgress
    }

    override fun getDurationView(): TextView? {
        return binding.tvVPDuration
    }

    override fun getSeekBarView(): SeekBar {
        return binding.seekBarVP
    }

    override fun getSpeedView(): TextView? {
        return binding.tvVPSpeed
    }

    override fun getLoopModeView(): ImageView? {
        return binding.ivVPLoopMode
    }

    override fun getScaleModeView(): ImageView? {
        return binding.ivVPScaleMode
    }

    override fun getFullscreenView(): ImageView? {
        return binding.ivVPFullscreen
    }

    override fun getVolumeOverlayView(): View? {
        return binding.gestureVolumeContainer
    }

    override fun getBrightnessOverlayView(): View? {
        return binding.gestureBrightnessContainer
    }

    override fun getSeekOverlayView(): View? {
        return binding.gestureSeekContainer
    }

    override fun getVolumeSeekBarView(): ProgressBar? {
        return binding.volumeProgressBar
    }

    override fun getBrightnessSeekBarView(): ProgressBar? {
        return binding.brightnessProgressBar
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
        binding.tvVPTitle.text = title
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

    @SuppressLint("SetTextI18n")
    override fun onSpeedChanged(speed: Float) {
        binding.tvVPSpeed.text = "${speed}x"
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

    override fun onFullscreenChanged(isFullscreen: Boolean) {
        val iconRes = if (isFullscreen) {
            R.drawable.ic_fullscreen_exit  // 横屏时显示退出全屏图标
        } else {
            R.drawable.ic_fullscreen_enter  // 竖屏时显示进入全屏图标
        }
        binding.ivVPFullscreen.setImageResource(iconRes)
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

        binding.gestureSeekContainer.animate()?.alpha(0f)?.withEndAction {
            binding.gestureSeekContainer.visibility = GONE
        }?.start()
    }

    override fun onGestureVolumeProgressChanged(volume: Float, progress: Int) {
        binding.gestureVolumeContainer.alpha = 1f
        binding.gestureVolumeContainer.visibility = VISIBLE
        binding.volumeProgressBar.progress = progress
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
        binding.gestureBrightnessContainer.alpha = 1f
        binding.gestureBrightnessContainer.visibility = VISIBLE
        binding.brightnessProgressBar.progress = progress
    }

    override fun onGestureProgressChanged(currentPosition: Long, startPosition: Long) {
        binding.gestureSeekContainer.visibility = VISIBLE
        binding.gestureSeekContainer.alpha = 1f

        // 计算时间差
        val diffMs = currentPosition - startPosition
        val diffText = if (diffMs >= 0) "+${formatTime(diffMs)}" else "-${formatTime(abs(diffMs))}"

        // 显示格式：时间差\n当前时间
        binding.tvGestureSeekTime.text = "$diffText\n${formatTime(currentPosition)}"
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