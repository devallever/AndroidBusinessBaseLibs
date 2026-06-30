package app.android.allever.gp.quick.project.ui.adapter

import app.allever.android.lib.core.helper.TimeHelper
import app.android.allever.gp.quick.project.R
import app.android.allever.gp.quick.project.core.Record
import app.android.allever.gp.quick.project.databinding.RvHistoryBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class HistoryAdapter :
    BaseQuickAdapter<Record, BaseViewHolder>(R.layout.rv_history) {
    override fun convert(holder: BaseViewHolder, item: Record) {
        val binding = RvHistoryBinding.bind(holder.itemView)
        binding.apply {
            tvTime.text = TimeHelper.formatDateTime(item.time).replace(" ", "\n")
            tvNetworkType.text = "${item.networkType}-${item.operator}"
            tvDownloadSpeed.text = item.downloadSpeed.toString()
            tvUploadSpeed.text = item.uploadSpeed.toString()
        }
    }
}