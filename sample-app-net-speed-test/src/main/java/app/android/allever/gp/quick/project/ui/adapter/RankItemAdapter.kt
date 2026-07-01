package app.android.allever.gp.quick.project.ui.adapter

import app.android.allever.gp.quick.project.R
import app.android.allever.gp.quick.project.core.RankItem
import app.android.allever.gp.quick.project.databinding.NstRvRankBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class RankItemAdapter: BaseQuickAdapter<RankItem, BaseViewHolder>(R.layout.nst_rv_rank) {
    override fun convert(holder: BaseViewHolder, item: RankItem) {
        val binding = NstRvRankBinding.bind(holder.itemView)
        binding.apply {
            tvRank.text = "${data.indexOf(item) + 1}"
            tvDownloadSpeed.text = "${item.downloadSpeed.toInt()} Mbps"
            tvUploadSpeed.text = "${item.uploadSpeed.toInt()} Mbps"
            tvNetworkType.text = item.networkType
            tvModel.text = item.model
            tvTime.text = item.time
        }
    }
}