package app.allever.android.sample.im.business.adapter

import android.view.ViewGroup
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.sample.im.R
import app.allever.android.sample.im.business.data.ServerImageItem
import app.allever.android.sample.im.databinding.ImRvServerImageBinding
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class ServerImageAdapter: BaseQuickAdapter<ServerImageItem, BaseViewHolder>(R.layout.im_rv_server_image) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        setOnItemClickListener { adapter, view, position ->
            val item = (adapter.data[position] as? TextClickItem)
            item?.itemClick?.invoke(item)
        }
        return super.onCreateViewHolder(parent, viewType)
    }

    override fun convert(
        holder: BaseViewHolder,
        item: ServerImageItem
    ) {
        val binding = ImRvServerImageBinding.bind(holder.itemView)
        Glide.with(holder.itemView.context).load(item.url).into(binding.ivImage)
    }
}