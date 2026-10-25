package app.funnyappptech.android.fake.call.ui.adapter

import android.content.Context
import app.funnyappptech.android.fake.call.R
import com.allever.lib.common.ui.widget.recycler.BaseRecyclerViewAdapter
import com.allever.lib.common.ui.widget.recycler.BaseViewHolder
import app.funnyappptech.android.fake.call.data.WallPagerItem

class WallPagerAdapter(context: Context, resId: Int, data: MutableList<WallPagerItem>) :
    BaseRecyclerViewAdapter<WallPagerItem>(context, resId, data) {
    override fun bindHolder(holder: BaseViewHolder, position: Int, item: WallPagerItem) {
        holder.setImageResource(R.id.item_wall_pager_iv, item.resId!!)
        holder.setVisible(R.id.item_wall_pager_iv_check, item.checked)
    }
}