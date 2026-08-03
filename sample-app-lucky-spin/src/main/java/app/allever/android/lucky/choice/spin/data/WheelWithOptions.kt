package app.allever.android.lucky.choice.spin.data

import androidx.recyclerview.widget.DiffUtil
import androidx.room.Embedded
import androidx.room.Relation

data class WheelWithOptions(
    @Embedded
    val wheel: Wheel,
    @Relation(
        parentColumn = "id",
        entityColumn = "wheel_id"
    )
    val options: List<Option>
) {
    companion object {
        val DIFF_CALLBACK by lazy {
            object : DiffUtil.ItemCallback<WheelWithOptions>() {
                override fun areItemsTheSame(
                    oldItem: WheelWithOptions,
                    newItem: WheelWithOptions
                ): Boolean = oldItem.wheel.id == newItem.wheel.id

                override fun areContentsTheSame(
                    oldItem: WheelWithOptions,
                    newItem: WheelWithOptions
                ): Boolean =
                    (oldItem.wheel.name == newItem.wheel.name) && (oldItem.options.size == newItem.options.size)
            }
        }
    }
}
