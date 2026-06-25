package app.allever.android.sample.audiovideo.core.player

import android.content.Context
import android.util.AttributeSet
import app.allever.android.lib.player.core.controller.IVideoUiController
import app.allever.android.lib.player.core.player.StdVideoPlayer

class CustomStdVideoPlayer(context: Context, attrs: AttributeSet? = null): StdVideoPlayer(context, attrs, 0) {

    override fun bindUiController(): IVideoUiController {
        return CustomStdVideoController(mContext)
    }

    override fun enableWidget() {

    }

    override fun initView() {
    }
}