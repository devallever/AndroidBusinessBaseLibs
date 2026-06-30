package app.android.allever.gp.quick.project.ui.adapter

import app.android.allever.gp.quick.project.R
import app.android.allever.gp.quick.project.core.ToolsItem
import app.android.allever.gp.quick.project.databinding.RvToolsBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class ToolsItemAdapter : BaseQuickAdapter<ToolsItem, BaseViewHolder>(R.layout.rv_tools) {
    override fun convert(holder: BaseViewHolder, item: ToolsItem) {
        val binding = RvToolsBinding.bind(holder.itemView)
        binding.apply {
            ivIcon.setImageResource(item.icon)
            tvTitle.text = item.title
        }
    }
}