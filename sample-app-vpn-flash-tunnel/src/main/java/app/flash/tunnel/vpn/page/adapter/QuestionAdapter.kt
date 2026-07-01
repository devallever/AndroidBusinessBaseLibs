package app.flash.tunnel.vpn.page.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import app.flash.tunnel.vpn.R
import app.flash.tunnel.vpn.data.QaItem
import app.flash.tunnel.vpn.databinding.ItemQuestionBinding

class QuestionAdapter(val data: MutableList<QaItem>) :
    RecyclerView.Adapter<QuestionAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemQuestionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemQuestionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount() = data.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            val item = data[position]

            root.setOnClickListener {
                item.expend = !item.expend
                notifyItemChanged(position, position)
            }

            tvQuestion.text = item.q
            tvAnswer.text = item.a
            tvAnswer.isVisible = item.expend
            if (item.expend) {
                ivExpand.setImageResource(R.drawable.icon_arrow_up_24)
            } else {
                ivExpand.setImageResource(R.drawable.icon_arrow_down_24)
            }
        }
    }
}