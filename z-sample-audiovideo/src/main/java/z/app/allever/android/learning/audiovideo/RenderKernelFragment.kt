package z.app.allever.android.learning.audiovideo

import android.graphics.Matrix
import z.app.allever.android.learning.audiovideo.kernel.AndroidPlayerFactory
import z.app.allever.android.learning.audiovideo.kernel.IJKPlayerFactory
import z.app.allever.android.learning.audiovideo.kernel.internal.AbsPlayer
import z.app.allever.android.learning.audiovideo.kernel.internal.AbsPlayerFactory
import z.app.allever.android.learning.audiovideo.kernel.internal.PlayerStatusListener
import z.app.allever.android.learning.audiovideo.render.internal.IRenderView
import z.app.allever.android.learning.audiovideo.render.TextureRenderView
import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.logE
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.core.helper.FragmentHelper
import app.allever.android.lib.core.helper.ViewHelper
import app.allever.android.lib.media.core.model.MediaItem
import app.allever.android.lib.media.picker.MediaPickerCore
import app.allever.android.lib.mvvm.base.BaseViewModel
import z.app.allever.android.sample.audiovideo.databinding.FragmentRenderKernelBinding

class RenderKernelFragment : BaseFragment<FragmentRenderKernelBinding, BaseViewModel>() {

    private val videoPickerLauncher = MediaPickerCore.registerPickerLauncher( this) {
        if (it.isEmpty()) {
            toast("未选择任何文件")
            return@registerPickerLauncher
        }
        val item = it[0]
        player?.setDataSource(item.path)
    }

    private var player: AbsPlayer? = AbsPlayerFactory.Companion.create<AndroidPlayerFactory>().createPlayer()

    private var mRender: IRenderView? = null

    override fun inflate() = FragmentRenderKernelBinding.inflate(layoutInflater)

    override fun init() {
        val list = mutableListOf(
            TextClickItem("渲染(Texture)") {
                mRender = mBinding.textureRenderView
                ViewHelper.setVisible(mBinding.surfaceRenderView, false)
                ViewHelper.setVisible(mBinding.textureRenderView, true)
            },
            TextClickItem("渲染(Surface)") {
                mRender = mBinding.surfaceRenderView
                ViewHelper.setVisible(mBinding.surfaceRenderView, true)
                ViewHelper.setVisible(mBinding.textureRenderView, false)
            },
            TextClickItem("Android内核") {
                player = AbsPlayerFactory.Companion.create<AndroidPlayerFactory>().createPlayer()
                setPlayerListener()
                mRender?.attachToPlayer(player!!)
                toast(it.title)
            },
            TextClickItem("IJKPlayer内核") {
                player = AbsPlayerFactory.Companion.create<IJKPlayerFactory>().createPlayer()
                setPlayerListener()
                mRender?.attachToPlayer(player!!)
                toast(it.title)
            },
            TextClickItem("1.初始化播放器") {
                player?.initPlayer()
            },
            TextClickItem("2.选择视频") {
                selectVideo()
            },
            TextClickItem("4.准备") {
                player?.prepareAsync()
            },
            TextClickItem("播放") {
                player?.start()
            },
            TextClickItem("暂停") {
                player?.pause()
            },
            TextClickItem("停止(需要重新设置数据)") {
                player?.stop()
            }
        )
        FragmentHelper.addToContainer(
            childFragmentManager,
            MediaKernelFragment(list),
            mBinding.fragmentContainer.id
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        player?.stop()
        player?.release()
        player?.playerStatusListener = null
    }

    private fun setPlayerListener() {
        player?.playerStatusListener = object : PlayerStatusListener {
            override fun onError(type: Int, error: String?) {

            }

            override fun onCompletion() {
            }

            override fun onInfo(what: Int, extra: Int) {
            }

            override fun onPrepared() {
                log("onPrepared")
                changeVideoSize()
            }

            override fun onVideoSizeChanged(width: Int, height: Int) {
                log("onVideoSizeChanged: width = $width , height = $height")
                changeVideoSize()
            }

        }
    }

    private fun selectVideo() {
        MediaPickerCore.launchVideo(videoPickerLauncher)
    }

    //改变视频的尺寸自适应。
    private fun changeVideoSize() {

        val videoWidth = player?.getVideoWidth()?.toFloat() ?: 0f
        val videoHeight = player?.getVideoHeight()?.toFloat() ?: 0f
        log("显示视频尺寸: $videoWidth x $videoHeight")
        VideoViewHelper.autoFixContainerSize(
            mBinding.renderViewContainer,
            videoWidth.toInt(),
            videoHeight.toInt()
        ) { displayWidth, displayHeight ->
            //无法直接设置视频尺寸，将计算出的视频尺寸设置到surfaceView 让视频自动填充。
            val params = mRender?.getView()?.layoutParams
            params?.width = displayWidth
            params?.height = displayHeight
            mRender?.getView()?.layoutParams = params
        }
    }
}