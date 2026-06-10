package z.app.allever.android.sample.function.im.ui.adapter

import android.widget.ImageView
import android.widget.TextView
import app.allever.android.lib.imageloader.core.load
import z.app.allever.android.lib.widget.recycler.RefreshRVAdapter
import z.app.allever.android.sample.function.R
import z.app.allever.android.sample.function.im.message.BaseMessage
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class ConversationAdapter :
    RefreshRVAdapter<BaseMessage, BaseViewHolder>(R.layout.rv_conversation_item) {

    override fun convert(holder: BaseViewHolder, item: BaseMessage) {
        holder.getView<TextView>(R.id.tvNickName).text = "${item.user?.id}.${item.user?.nickname}"
        holder.getView<ImageView>(R.id.ivAvatar).load(item.user?.avatar ?: "")
    }
}