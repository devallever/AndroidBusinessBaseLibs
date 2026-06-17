package z.app.allever.android.learning.audiovideo.ijkplayer

import android.net.Uri
import z.app.allever.android.learning.audiovideo.ijkplayer.widget.media.AndroidMediaController
import z.app.allever.android.sample.audiovideo.databinding.FragmentIjkBaseBinding
import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.mvvm.base.BaseViewModel
import androidx.core.net.toUri
import app.allever.android.lib.core.ext.log

class IJKBasePlayerFragment : BaseFragment<FragmentIjkBaseBinding, BaseViewModel>() {
    override fun inflate() = FragmentIjkBaseBinding.inflate(layoutInflater)

    override fun init() {
        val uri = arguments?.getParcelable<Uri>("uri")
        val path = arguments?.getString("path")
        log("uri = ${uri.toString()}" )
        log("path = $path")
        mBinding.ijkVideoView.setVideoURI(path?.toUri())
        mBinding.ijkVideoView.start()
        mBinding.ijkVideoView.setMediaController(AndroidMediaController(requireContext()))
    }

    override fun onPause() {
        super.onPause()
        mBinding.ijkVideoView.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding.ijkVideoView.release(true)
    }
}