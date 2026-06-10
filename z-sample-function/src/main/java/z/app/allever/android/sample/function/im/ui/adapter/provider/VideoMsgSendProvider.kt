package z.app.allever.android.sample.function.im.ui.adapter.provider

import z.app.allever.android.sample.function.R
import z.app.allever.android.sample.function.im.ui.adapter.ItemType

class VideoMsgSendProvider : VideoMsgProvider() {
    override val itemViewType = ItemType.VIDEO_MSG_SEND
    override val layoutId = R.layout.rv_video_msg_send
}