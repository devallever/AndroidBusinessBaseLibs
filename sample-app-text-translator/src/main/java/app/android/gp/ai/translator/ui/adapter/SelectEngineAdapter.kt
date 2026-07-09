package app.android.gp.ai.translator.ui.adapter

import android.content.Context
import app.android.gp.ai.translator.R
import app.android.gp.ai.translator.ui.adapter.item.SelectEngineItem
import app.android.gp.ai.translator.translate.EngineType
import app.woejt.wwzdndgl.lib.recycler.BaseRecyclerViewAdapter
import app.woejt.wwzdndgl.lib.recycler.BaseViewHolder

class SelectEngineAdapter(context: Context, layoutResId: Int, data: MutableList<SelectEngineItem>) :
    BaseRecyclerViewAdapter<SelectEngineItem>(context, layoutResId, data) {
    override fun bindHolder(holder: BaseViewHolder, position: Int, item: SelectEngineItem) {
        holder.setText(R.id.tvLanguage, EngineType.getEngineName(item.value ?: 0))
    }
}