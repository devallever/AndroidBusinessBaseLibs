package z.app.allever.android.sample.ui.ui.dragclose

import app.allever.android.lib.imageloader.core.load
import z.app.allever.android.sample.ui.R
import z.app.allever.android.sample.ui.databinding.RvImageSquareBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class PhotoItemAdapter: BaseQuickAdapter<String, BaseViewHolder>(R.layout.rv_image_square) {

    override fun convert(holder: BaseViewHolder, item: String) {
        val binding = RvImageSquareBinding.bind(holder.itemView)
        binding.ivImage.load( item)
    }
}