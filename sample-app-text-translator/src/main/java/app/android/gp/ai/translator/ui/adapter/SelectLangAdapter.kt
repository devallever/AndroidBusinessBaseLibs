package app.android.gp.ai.translator.ui.adapter

import android.content.Context
import app.android.gp.ai.translator.R
import app.android.gp.ai.translator.language.Lang
import app.android.gp.ai.translator.ui.adapter.item.SelectLangItem
import app.woejt.wwzdndgl.lib.recycler.BaseRecyclerViewAdapter
import app.woejt.wwzdndgl.lib.recycler.BaseViewHolder

class SelectLangAdapter(context: Context, layoutResId: Int, data: MutableList<SelectLangItem>) :
    BaseRecyclerViewAdapter<SelectLangItem>(context, layoutResId, data) {
    override fun bindHolder(holder: BaseViewHolder, position: Int, item: SelectLangItem) {
        holder.setText(R.id.tvLanguage, item.lang?.KEY ?: Lang.CHINESE.KEY)
    }
}