package z.app.allever.android.sample.function.im.ui.adapter.provider

import z.app.allever.android.sample.function.R
import z.app.allever.android.sample.function.im.ui.adapter.ItemType

class ImageMsgReceiveProvider : ImageMsgProvider() {
    override val itemViewType = ItemType.IMAGE_MSG_RECEIVE
    override val layoutId = R.layout.rv_image_msg_receive
}