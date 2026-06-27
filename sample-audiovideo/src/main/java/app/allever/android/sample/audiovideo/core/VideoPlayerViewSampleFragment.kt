package app.allever.android.sample.audiovideo.core

import android.graphics.Color
import android.view.View
import android.widget.ScrollView
import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.media.core.model.MediaItem
import app.allever.android.lib.media.picker.MediaPickerCore
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.lib.player.core.PlayerState
import app.allever.android.lib.player.core.engine.MediaPlayerEngine
import app.allever.android.lib.player.core.engine.ijk.IjkPlayerEngine
import app.allever.android.lib.player.core.engine.media3.ExoPlayerViewRender
import app.allever.android.lib.player.core.engine.media3.Media3PlayerEngine
import app.allever.android.lib.player.core.player.IVideoPlayerViewListener
import app.allever.android.lib.player.core.render.RenderRegistry
import app.allever.android.lib.player.core.render.SurfaceViewRender
import app.allever.android.lib.player.core.render.TextureViewRender
import app.allever.android.lib.player.core.render.VideoViewRender
import app.allever.android.sample.audiovideo.VideoSource
import app.allever.android.sample.audiovideo.databinding.FragmentVideoPlayerViewSampleBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VideoPlayerViewSampleFragment :
    BaseFragment<FragmentVideoPlayerViewSampleBinding, BaseViewModel>() {
    /** 视频选择器 */
    private val videoPickerLauncher = MediaPickerCore.registerPickerLauncher(this) { items ->
        items.firstOrNull()?.let { mediaItem ->
            if (mediaItem is MediaItem.Video) {
                mBinding.etUrl.setText(mediaItem.uri.toString())
                appendLog("选择本地视频: ${mediaItem.name} (${mediaItem.uri})")
                autoPlayOnPrepared = true

                mBinding.videoPlayerView.setSource(mediaItem.uri)
            }
        }
    }

    /** 默认测试视频 URL */
    private val defaultTestUrl = "https://www.w3schools.com/html/mov_bbb.mp4"

    /** setSource 后是否自动调用 play() */
    private var autoPlayOnPrepared = true

    override fun inflate() = FragmentVideoPlayerViewSampleBinding.inflate(layoutInflater)

    override fun init() {
        initVideoPicker()
        initPlayer()
        initViews()
        updateStateUI(PlayerState.IDLE)
    }

    /**
     * 初始化视频选择器
     */
    private fun initVideoPicker() {
        // 视频选择器配置
    }

    /**
     * 初始化播放器（使用新架构的组合模式）
     */
    private fun initPlayer() {
        // 使用默认配置：MediaPlayerEngine + SurfaceViewRender
        mBinding.videoPlayerView.setListener(object : IVideoPlayerViewListener {
            override fun debugUpdateState() {
                updateStateUI(mBinding.videoPlayerView.videoPlayer.state)
                updateRenderButtonState()
                updateEngineButtonState()
                updateArchInfo()
            }

            override fun onLog(msg: String) {
                appendLog(msg)
            }

        })
        appendLog("初始化播放器: MediaPlayerEngine + SurfaceViewRender")
        updateArchInfo()
    }

    /**
     * 初始化视图和事件监听
     */
    private fun initViews() {
        // ==================== 播放控制按钮 ====================

        // 选择本地视频
        mBinding.btnPickLocal.setOnClickListener {
            MediaPickerCore.launchVideo(videoPickerLauncher)
        }

        // 播放网络视频
        mBinding.btnPlayNet.setOnClickListener {
            val url = mBinding.etUrl.text.toString().trim()
            val sourceUrl = url.ifEmpty {
                VideoSource.testUrlList.random()
            }
            autoPlayOnPrepared = true
            mBinding.videoPlayerView.setSource(sourceUrl)
            appendLog("设置数据源: ${mBinding.etUrl.text.ifEmpty { sourceUrl }}")
        }

        // 播放 Assets 文件
        mBinding.btnPlayAsset.setOnClickListener {
            val assetPath = mBinding.etAssetPath.text.toString().trim()
            if (assetPath.isNotEmpty()) {
                autoPlayOnPrepared = true
                mBinding.videoPlayerView.setAssetSource(assetPath)
                appendLog("播放 Assets 文件: $assetPath")
            } else {
                appendLog("请输入 Assets 文件路径")
            }
        }

        // ==================== 渲染器切换按钮（新架构特性）====================

        // 切换到 SurfaceView
        mBinding.btnSwitchSurfaceView.setOnClickListener {
            mBinding.videoPlayerView.switchRender(SurfaceViewRender.Companion.NAME)
        }

        // 切换到 TextureView
        mBinding.btnSwitchTextureView.setOnClickListener {
            mBinding.videoPlayerView.switchRender( TextureViewRender.Companion.NAME)
        }

        // 切换到 VideoView
        mBinding.btnSwitchVideoView.setOnClickListener {
            mBinding.videoPlayerView.switchRender( VideoViewRender.Companion.NAME)
        }

        // 切换到 PlayerView (ExoPlayer)
        mBinding.btnSwitchPlayerView.setOnClickListener {
            if (mBinding.videoPlayerView.currentEngineType != Media3PlayerEngine.Companion.NAME) {
                toast("请先切换到 ExoPlayer")
                return@setOnClickListener
            }
            mBinding.videoPlayerView.switchRender(ExoPlayerViewRender.Companion.NAME)
        }

        // ==================== 引擎切换按钮（新架构核心功能演示）====================

        // 切换到 MediaPlayer
        mBinding.btnSwitchMediaPlayer.setOnClickListener {
            if(mBinding.videoPlayerView.currentRenderName == ExoPlayerViewRender.Companion.NAME) {
                toast("请先切换到其他渲染")
                return@setOnClickListener
            }
            mBinding.videoPlayerView.switchEngine(MediaPlayerEngine.Companion.NAME)
        }

        // 切换到 Media3 (ExoPlayer)
        mBinding.btnSwitchMedia3.setOnClickListener {
            mBinding.videoPlayerView.switchEngine(Media3PlayerEngine.Companion.NAME)
        }

        // 切换到 IJKPlayer
        mBinding.btnSwitchIjkPlayer.setOnClickListener {
            if(mBinding.videoPlayerView.currentRenderName == ExoPlayerViewRender.Companion.NAME) {
                toast("请先切换到其他渲染")
                return@setOnClickListener
            }
            mBinding.videoPlayerView.switchEngine(IjkPlayerEngine.Companion.NAME)
        }

        // 清空日志
        mBinding.btnClearLog.setOnClickListener {
            mBinding.tvLog.text = ""
        }
    }

    /**
     * 更新渲染器切换按钮状态
     */
    private fun updateRenderButtonState() {
        mBinding.btnSwitchSurfaceView.isEnabled = mBinding.videoPlayerView.currentRenderName != SurfaceViewRender.Companion.NAME
        mBinding.btnSwitchTextureView.isEnabled = mBinding.videoPlayerView.currentRenderName != TextureViewRender.Companion.NAME
        mBinding.btnSwitchVideoView.isEnabled = mBinding.videoPlayerView.currentRenderName != VideoViewRender.Companion.NAME
        mBinding.btnSwitchPlayerView.isEnabled = mBinding.videoPlayerView.currentRenderName != ExoPlayerViewRender.Companion.NAME
    }

    /**
     * 更新引擎切换按钮状态
     */
    private fun updateEngineButtonState() {
        mBinding.btnSwitchMediaPlayer.isEnabled = mBinding.videoPlayerView.currentEngineType != MediaPlayerEngine.Companion.NAME
        mBinding.btnSwitchMedia3.isEnabled = mBinding.videoPlayerView.currentEngineType != Media3PlayerEngine.Companion.NAME
        mBinding.btnSwitchIjkPlayer.isEnabled = mBinding.videoPlayerView.currentEngineType != IjkPlayerEngine.Companion.NAME
    }

    /**
     * 更新架构信息显示
     */
    private fun updateArchInfo() {
        val engineName = mBinding.videoPlayerView.currentEngineType
        // 使用 RenderRegistry 获取渲染器的显示名称
        val renderDisplayName = try {
            RenderRegistry.create(mBinding.videoPlayerView.currentRenderName)?.renderName ?: mBinding.videoPlayerView.currentRenderName
        } catch (e: Exception) {
            mBinding.videoPlayerView.currentRenderName
        }
        mBinding.tvArchInfo.text = "当前组合: $engineName + $renderDisplayName"
    }

    // ==================== UI 更新方法 ====================

    /**
     * 更新状态显示
     */
    private fun updateStateUI(state: PlayerState) {
        mBinding.tvState.text = "状态: $state"

        // 根据状态更新 UI 颜色或图标
        when (state) {
            PlayerState.IDLE, PlayerState.STOPPED -> {
                mBinding.tvState.setTextColor(Color.GRAY)
            }
            PlayerState.PREPARING -> {
                mBinding.tvState.setTextColor(Color.YELLOW)
            }
            PlayerState.PREPARED, PlayerState.PAUSED -> {
                mBinding.tvState.setTextColor(Color.BLUE)
            }
            PlayerState.PLAYING -> {
                mBinding.tvState.setTextColor(Color.GREEN)
            }
            PlayerState.COMPLETED -> {
                mBinding.tvState.setTextColor(Color.CYAN)
            }
            PlayerState.ERROR, PlayerState.RELEASED -> {
                mBinding.tvState.setTextColor(Color.RED)
            }
        }
    }

    /**
     * 追加日志文本
     */
    private fun appendLog(message: String) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val logLine = "[$time] $message\n"

        mBinding.tvLog.let { tvLog ->
            val currentText = tvLog.text.toString()
            if (currentText.length > 5000) {
                tvLog.text = currentText.takeLast(3000)
            }
            tvLog.append(logLine)

            /// 自动滚动到底部
            val scrollView = mBinding.tvLog.parent as? ScrollView
            scrollView?.post {
                scrollView.fullScroll(View.FOCUS_DOWN)
            }
        }
    }

    // ==================== 生命周期管理 ====================

    override fun onPause() {
        super.onPause()
        if (mBinding.videoPlayerView.videoPlayer.isPlaying) {
            mBinding.videoPlayerView.videoPlayer.pause()
            appendLog("onPause: 暂停播放")
        }
    }

}