package app.allever.android.lucky.choice.spin

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.allever.android.lucky.choice.spin.data.WheelWithOptions
import app.allever.android.lucky.choice.spin.databinding.ItemWheel3Binding

class WheelAdapter(
    private val onWheelClick: (WheelWithOptions) -> Unit,
    private val onWheelEditClick: (WheelWithOptions) -> Unit,
) :
    ListAdapter<WheelWithOptions, WheelAdapter.ViewHolder>(WheelWithOptions.DIFF_CALLBACK) {

    inner class ViewHolder(private val binding: ItemWheel3Binding) :
        RecyclerView.ViewHolder(binding.root) {
            @SuppressLint("SetTextI18n")
            fun bind(wheelWithOptions: WheelWithOptions) {
            binding.textViewWheelName.text = wheelWithOptions.wheel.name
            binding.textViewOptionCount.text = "${wheelWithOptions.options.size} options"
            binding.root.setOnClickListener {
                onWheelClick(wheelWithOptions)
            }
            binding.buttonEditWheel.setOnClickListener {
                onWheelEditClick(wheelWithOptions)
            }
            binding.wheelView.data = mutableListOf<String>().apply {
                for (i in 0..<wheelWithOptions.options.size) {
                    add("")
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemWheel3Binding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }
}