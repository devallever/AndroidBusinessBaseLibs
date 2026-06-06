package app.allever.android.learning.audiovideo

import android.graphics.Matrix
import app.allever.android.learning.audiovideo.kernel.AndroidPlayerFactory
import app.allever.android.learning.audiovideo.kernel.IJKPlayerFactory
import app.allever.android.learning.audiovideo.kernel.internal.AbsPlayer
import app.allever.android.learning.audiovideo.kernel.internal.AbsPlayerFactory
import app.allever.android.learning.audiovideo.kernel.internal.PlayerStatusListener
import app.allever.android.learning.audiovideo.render.internal.IRenderView
import app.allever.android.learning.audiovideo.render.TextureRenderView
import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.logE
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.core.helper.FragmentHelper
import app.allever.android.lib.core.helper.ViewHelper
import app.allever.android.lib.media.picker.MediaPickerCore
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.audiovideo.databinding.FragmentRenderKernelBinding

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
                if (mRender is TextureRenderView) {
                    mBinding.textureRenderView.post {
                        if (mBinding.textureRenderView.width > 0 && mBinding.textureRenderView.height > 0) {
                            handleTextureSize(mBinding.textureRenderView.width.toFloat(), mBinding.textureRenderView.height.toFloat())
                        }
                    }
                }
            }

            override fun onVideoSizeChanged(width: Int, height: Int) {
                log("onVideoSizeChanged: width = $width , height = $height")
                //不处理就是默认视频占满布局

//                handleSurfaceSize(width, height)
//                mRender?.setVideoSize(width, height)
//                mRender?.setScaleType(ConstantKeys.PlayerScreenScaleType.SCREEN_SCALE_16_9)
//                mBinding.surfaceRenderView.setScaleType(ConstantKeys.PlayerScreenScaleType.SCREEN_SCALE_16_9)
//                mBinding.surfaceRenderView.setVideoSize(width, height)

                if (mRender is TextureRenderView) {
                    handleTextureSize(width.toFloat(), height.toFloat())
                }
            }

        }
    }

    private fun selectVideo() {
        MediaPickerCore.launchVideo(videoPickerLauncher)
    }

    private fun handleSurfaceSize(width: Int, height: Int) {
        val w: Float = width.toFloat()
        val h: Float = height.toFloat()
        val sw: Float = mBinding.renderViewContainer.width.toFloat()
        val sh: Float = mBinding.renderViewContainer.height.toFloat()
        var displayW = 0
        var displayH = 0

        if (w > h) {
            //横向视频
            if (w > sw) {
                //超宽视频
            } else {
                //
                displayH = sh.toInt()
                displayW = (w * sh / h).toInt()
            }
        } else {
            //纵向视频
            displayH = sh.toInt()
            displayW = (w * sh / h).toInt()
        }

        log("surface size = $displayW x $displayH")

        //无法直接设置视频尺寸，将计算出的视频尺寸设置到surfaceView 让视频自动填充。
        val params = mBinding.surfaceView.layoutParams
        params.width = displayW
        params.height = displayH
        mBinding.surfaceRenderView.layoutParams = params
        mBinding.textureRenderView.layoutParams = params
    }

    private fun handleTextureSize(mtextureViewWidth: Float, mtextureViewHeight: Float) {
        log("视频拉伸: $mtextureViewWidth x $mtextureViewHeight")
        mBinding.textureRenderView.post {
            //mtextureViewWidth为textureView宽，mtextureViewHeight为textureView高
            //mtextureViewWidth宽高，为什么需要用传入的，因为全屏显示时宽高不会及时更新
            val matrix = Matrix();
            //videoView为new MediaPlayer()
            val mVideoWidth = player?.getVideoWidth()?.toFloat() ?: 0f
            val mVideoHeight = player?.getVideoHeight()?.toFloat() ?: 0f
            log("视频宽高: $mVideoWidth x $mVideoHeight")

            if (mVideoWidth == 0f || mVideoHeight == 0f) {
                logE("视频宽高为0")
                return@post
            }

            //得到缩放比，从而获得最佳缩放比
            val sx = mtextureViewWidth / mVideoWidth;
            val sy = mtextureViewHeight / mVideoHeight;
            //先将视频变回原来的大小
            val sx1 = mVideoWidth / mtextureViewWidth;
            val sy1 = mVideoHeight / mtextureViewHeight;
            matrix.preScale(sx1, sy1);
//            log("mat", matrix.toString());
            //然后判断最佳比例，满足一边能够填满
            if (sx >= sy) {
                matrix.preScale(sy, sy);
                //然后判断出左右偏移，实现居中，进入到这个判断，证明y轴是填满了的
                val leftX = (mtextureViewWidth - mVideoWidth * sy) / 2;
                matrix.postTranslate(leftX, 0f);
            } else {
                matrix.preScale(sx, sx);
                val leftY = (mtextureViewHeight - mVideoHeight * sx) / 2;
                matrix.postTranslate(0f, leftY);
            }

            mBinding.textureRenderView.setTransform(matrix);//将矩阵添加到textureView
            mBinding.textureRenderView.postInvalidate();//重绘视图
        }

    }
}