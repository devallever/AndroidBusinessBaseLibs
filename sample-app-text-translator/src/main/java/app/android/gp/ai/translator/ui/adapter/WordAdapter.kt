package app.android.gp.ai.translator.ui.adapter

import android.content.Context
import android.view.View
import android.widget.CheckBox
import app.android.gp.ai.translator.R
import app.android.gp.ai.translator.app.Global
import app.android.gp.ai.translator.ui.adapter.item.WordItem
import app.android.gp.ai.translator.bean.TranslateResult
import app.android.gp.ai.translator.util.JsonHelper
import app.woejt.wwzdndgl.lib.recycler.BaseRecyclerViewAdapter
import app.woejt.wwzdndgl.lib.recycler.BaseViewHolder

class WordAdapter(context: Context, layoutResId: Int, data: MutableList<WordItem>) :
    BaseRecyclerViewAdapter<WordItem>(context, layoutResId, data) {

    var itemOptionListener: OnItemOptionClick? = null
    var editMode = false
        set(value) {
            field = value
            notifyDataSetChanged()
            if (!editMode) {
                allMode = false
            }
        }
    var allMode = false
    var allCheck = false
        set(value) {
            field = value
            mData.map {
                it.checked = value
            }
            notifyDataSetChanged()
            if (value) {
                selectedItem.addAll(mData)
            } else {
                selectedItem.clear()
            }
        }
    var selectedItem = mutableSetOf<WordItem>()

    override fun bindHolder(holder: BaseViewHolder, position: Int, item: WordItem) {
        val history = item.history ?: return
        if (history.liked == 1) {
            holder.setImageResource(R.id.ivLiked, R.drawable.ic_star_full)
        } else {
            holder.setImageResource(R.id.ivLiked, R.drawable.ic_star_empty)
        }

        holder.setText(R.id.tvSrcText, history.srcText)
        val srcLang = Global.langCodeKeyMap[history.sl]
        val translateLang = Global.langCodeKeyMap[history.tl]
        holder.setText(R.id.tvLanguageDirection, "$srcLang -> $translateLang: ")

        val resultStr = history.result
        try {
            val bean = JsonHelper.json2Object(resultStr, TranslateResult::class.java)
            val translateText = bean?.translateText ?: ""
            holder.setText(R.id.tvTranslateText, translateText)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val cb = holder.getView<CheckBox>(R.id.cbSelect)
        cb?.visibility = if (editMode) View.VISIBLE else View.GONE
        if (allMode) {
            holder.setChecked(R.id.cbSelect, allCheck)
        } else {
            holder.setChecked(R.id.cbSelect, item.checked)
        }

        holder.itemView.setOnClickListener {
            if (editMode) {
                handleCheck(holder, item, cb)
            } else {
                itemOptionListener?.onItemClicked(position)
            }
        }

        holder.itemView.setOnLongClickListener {
            if (editMode) {
                return@setOnLongClickListener true
            } else {
                itemOptionListener?.onLongClick(position)
                return@setOnLongClickListener true
            }
        }

        holder.setOnClickListener(R.id.ivLiked, View.OnClickListener {
            if (editMode) {
                handleCheck(holder, item, cb)
            } else {
                itemOptionListener?.onLikeClicked(position)
            }
        })

        holder.setOnClickListener(R.id.ivRemove, View.OnClickListener {
            if (editMode) {
                handleCheck(holder, item, cb)
            } else {
                itemOptionListener?.onRemoveClicked(position)
            }
        })
        mData[position] = item
    }

    private fun handleCheck(holder: BaseViewHolder, item: WordItem, cb: CheckBox?) {
        allMode = false
        item.checked = !item.checked
        holder.setChecked(R.id.cbSelect, item.checked)
        if (cb?.isChecked == true) {
            selectedItem.add(item)
        } else {
            selectedItem.remove(item)
        }
    }

    public interface OnItemOptionClick {
        fun onItemClicked(position: Int)
        fun onLongClick(position: Int)
        fun onLikeClicked(position: Int)
        fun onRemoveClicked(position: Int)
    }
}