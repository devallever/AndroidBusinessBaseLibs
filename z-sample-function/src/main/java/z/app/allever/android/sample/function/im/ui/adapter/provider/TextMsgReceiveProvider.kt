package z.app.allever.android.sample.function.im.ui.adapter.provider

import z.app.allever.android.sample.function.R
import z.app.allever.android.sample.function.im.ui.adapter.ItemType

class TextMsgReceiveProvider : TextMsgProvider() {
    override val itemViewType = ItemType.TEXT_MSG_RECEIVE
    override val layoutId = R.layout.rv_text_msg_receive
}