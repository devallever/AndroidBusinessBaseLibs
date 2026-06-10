package z.app.allever.android.sample.function.im.ui.adapter.provider

import z.app.allever.android.sample.function.R
import z.app.allever.android.sample.function.im.ui.adapter.ItemType

class VideoMsgReceiveProvider : VideoMsgProvider() {
    override val itemViewType = ItemType.VIDEO_MSG_RECEIVE
    override val layoutId = R.layout.rv_video_msg_receive
}