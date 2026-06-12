package app.allever.android.sample.cleaner.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import app.allever.android.sample.cleaner.databinding.ItemProcessBinding
import app.allever.android.sample.cleaner.memory.ProcessManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 * 进程列表适配器
 */
class ProcessAdapter :
    BaseQuickAdapter<ProcessManager.ProcessInfo, BaseViewHolder>(0) {

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding = ItemProcessBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BaseViewHolder(binding.root)
    }

    override fun convert(holder: BaseViewHolder, item: ProcessManager.ProcessInfo) {
        val binding = ItemProcessBinding.bind(holder.itemView)

        // 进程名（取包名最后一段）
        val name = item.processName.substringAfterLast(".")
        binding.tvProcessName.text =
            if (name.length > 30) name.take(27) + "..." else name

        binding.tvProcessPid.text = "PID: ${item.pid}"
        binding.tvProcessMemory.text = item.importanceDesc
    }
}
