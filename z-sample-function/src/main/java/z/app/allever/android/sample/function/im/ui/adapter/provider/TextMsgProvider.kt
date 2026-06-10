package z.app.allever.android.sample.function.im.ui.adapter.provider

import android.widget.TextView
import z.app.allever.android.sample.function.R
import z.app.allever.android.sample.function.im.message.BaseMessage
import z.app.allever.android.sample.function.im.message.TextMessage
import com.chad.library.adapter.base.viewholder.BaseViewHolder

abstract class TextMsgProvider : BaseMsgProvider() {
    override fun convert(helper: BaseViewHolder, item: BaseMessage) {
        if (item !is TextMessage) return
        setUserInfo(helper, item)
        helper.getView<TextView>(R.id.tvContent).text = item.content
    }

}