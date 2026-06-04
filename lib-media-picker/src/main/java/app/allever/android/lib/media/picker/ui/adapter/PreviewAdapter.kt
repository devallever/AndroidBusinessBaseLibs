package app.allever.android.lib.media.picker.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import app.allever.android.lib.media.core.model.MediaItem
import app.allever.android.lib.media.picker.R
import app.allever.android.lib.media.picker.databinding.ItemPreviewImageBinding
import com.bumptech.glide.Glide
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * 图片/视频预览 ViewPager2 Adapter
 * - 图片：PhotoView 手势缩放
 * - 视频：VideoView 播放 + 控制栏（进度条、播放/暂停、上下切换）
 */
class PreviewAdapter(
    private val items: List<MediaItem>,
    private val lifecycleOwner: LifecycleOwner,
    private val onItemClick: (Int) -> Unit,
    private val onNavigateTo: (Int) -> Unit, // 切换到指定 position
) : RecyclerView.Adapter<PreviewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPreviewImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(parent.context, binding, lifecycleOwner, onItemClick, onNavigateTo)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount() = items.size

    /** 停止所有正在播放的视频 */
    fun releaseAll() {
        // RecyclerView 会自动回收不可见的 ViewHolder，ViewHolder.onDetached 中会停止播放
    }

    inner class ViewHolder(
        private val context: Context,
        private val binding: ItemPreviewImageBinding,
        private val lifecycleOwner: LifecycleOwner,
        private val onItemClick: (Int) -> Unit,
        private val onNavigateTo: (Int) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        private var isVideoPlaying = false
        private var isUserSeeking = false
        private var progressJob: Job? = null
        private var currentVideoItem: MediaItem.Video? = null

        init {
            // 单击图片区域返回
            itemView.setOnClickListener {
                if (binding.layoutVideoContainer.visibility != android.view.View.VISIBLE) {
                    val pos = bindingAdapterPosition
                    if (pos >= 0 && pos < items.size) {
                        onItemClick(pos)
                    }
                }
            }

            // 视频控制按钮
            setupVideoControls()
        }

        fun bind(item: MediaItem, position: Int) {
            stopVideo()

            when (item) {
                is MediaItem.Image -> {
                    showImageMode()
                    Glide.with(binding.root)
                        .load(item.uri)
                        .placeholder(R.color.media_picker_placeholder)
                        .centerCrop()
                        .into(binding.photoView)
                    binding.photoView.setScale(1f, false)
                }
                is MediaItem.Video -> {
                    showVideoCoverMode(item)
                }
                else -> {
                    binding.photoView.visibility = android.view.View.GONE
                    binding.layoutVideoContainer.visibility = android.view.View.GONE
                }
            }
        }

        // ==================== 视频相关 ====================

        /** 显示视频封面模式（未播放状态） */
        private fun showVideoCoverMode(item: MediaItem.Video) {
            binding.photoView.visibility = android.view.View.VISIBLE
            binding.layoutVideoContainer.visibility = android.view.View.VISIBLE
            binding.videoPlayer.visibility = android.view.View.GONE
            binding.layoutVideoControls.visibility = android.view.View.GONE
            binding.btnVideoPlayPause.visibility = android.view.View.VISIBLE

            Glide.with(binding.root)
                .load(item.uri)
                .placeholder(R.color.media_picker_placeholder)
                .centerCrop()
                .into(binding.photoView)

            binding.tvVideoTotalTime.text = formatDurationMs(item.duration)
            binding.tvVideoCurrentTime.text = "00:00"
            binding.seekBarVideo.progress = 0
            currentVideoItem = item
        }

        /** 开始播放视频 */
        private fun startVideoPlayback() {
            val item = currentVideoItem ?: return
            try {
                binding.photoView.visibility = android.view.View.GONE
                binding.videoPlayer.visibility = android.view.View.VISIBLE
                binding.layoutVideoControls.visibility = android.view.View.VISIBLE
                binding.videoPlayer.setVideoURI(item.uri)

                binding.videoPlayer.setOnPreparedListener { mp ->
                    mp.isLooping = false
                    if (mp.duration > 0) {
                        binding.tvVideoTotalTime.text = formatDurationMs(mp.duration.toLong())
                    }
                    mp.start()
                    isVideoPlaying = true
                    updateVideoButton()
                    startProgressUpdate()
                }

                binding.videoPlayer.setOnCompletionListener {
                    isVideoPlaying = false
                    updateVideoButton()
                    stopProgressUpdate()
                    binding.btnVideoPlayPause.visibility = android.view.View.VISIBLE
                }

                binding.videoPlayer.setOnErrorListener { _, _, _ ->
                    isVideoPlaying = false
                    updateVideoButton()
                    stopProgressUpdate()
                    true
                }
            } catch (e: Exception) {
                app.allever.android.lib.core.ext.logE("PreviewAdapter", "播放视频异常: ${e.message}")
            }
        }

        /** 停止视频播放 */
        private fun stopVideo() {
            stopProgressUpdate()
            if (binding.videoPlayer.isPlaying) {
                binding.videoPlayer.stopPlayback()
            }
            isVideoPlaying = false
            isUserSeeking = false
            currentVideoItem = null
        }

        /** 切换播放/暂停 */
        private fun toggleVideoPlayPause() {
            if (isVideoPlaying) {
                binding.videoPlayer.pause()
                isVideoPlaying = false
                binding.btnVideoPlayPause.visibility = android.view.View.VISIBLE
                stopProgressUpdate()
            } else {
                if (binding.videoPlayer.visibility == android.view.View.GONE) {
                    // 从封面模式开始播放
                    startVideoPlayback()
                } else {
                    // 继续播放
                    binding.videoPlayer.start()
                    isVideoPlaying = true
                    binding.btnVideoPlayPause.visibility = android.view.View.GONE
                    startProgressUpdate()
                }
            }
            updateVideoButton()
        }

        private fun updateVideoButton() {
            if (!isVideoPlaying && !binding.videoPlayer.isPlaying) {
                binding.btnVideoPlayPause.visibility = android.view.View.VISIBLE
                binding.btnVideoPlayPause.setImageResource(R.drawable.ic_media_picker_play_video)
            } else {
                binding.btnVideoPlayPause.visibility = android.view.View.GONE
            }
        }

        private fun startProgressUpdate() {
            stopProgressUpdate()
            progressJob = lifecycleOwner.lifecycleScope.launch {
                while (isActive && isVideoPlaying) {
                    if (!isUserSeeking) {
                        val currentPos = binding.videoPlayer.currentPosition
                        val duration = binding.videoPlayer.duration
                        if (duration > 0) {
                            binding.seekBarVideo.progress = (currentPos * 1000 / duration)
                            binding.tvVideoCurrentTime.text = formatDurationMs(currentPos.toLong())
                        }
                    }
                    delay(200)
                }
            }
        }

        private fun stopProgressUpdate() {
            progressJob?.cancel()
            progressJob = null
        }

        /** 设置视频控制事件 */
        private fun setupVideoControls() {
            binding.btnVideoPlayPause.setOnClickListener { toggleVideoPlayPause() }

            // 单击视频画面切换播放/暂停
            binding.videoPlayer.setOnClickListener { toggleVideoPlayPause() }

            // 进度条拖动
            binding.seekBarVideo.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        val duration = binding.videoPlayer.duration
                        if (duration > 0) {
                            val pos = (progress * duration / 1000f).toInt()
                            binding.tvVideoCurrentTime.text = formatDurationMs(pos.toLong())
                        }
                    }
                }

                override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {
                    isUserSeeking = true
                }

                override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {
                    isUserSeeking = false
                    val duration = binding.videoPlayer.duration
                    if (duration > 0) {
                        val pos = (seekBar?.progress ?: 0) * duration / 1000
                        binding.videoPlayer.seekTo(pos)
                    }
                }
            })
        }

        // ==================== 模式切换 ====================

        private fun showImageMode() {
            binding.photoView.visibility = android.view.View.VISIBLE
            binding.layoutVideoContainer.visibility = android.view.View.GONE
        }

        // ==================== 工具方法 ====================

        private fun formatDurationMs(durationMs: Long): String {
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%02d:%02d".format(minutes, seconds)
        }
    }
}
