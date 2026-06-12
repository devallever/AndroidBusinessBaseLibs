package app.allever.android.sample.cleaner.ui.fragment

import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import app.allever.android.lib.common.BaseFragment
import app.allever.android.sample.cleaner.CleanerViewModel
import app.allever.android.sample.cleaner.databinding.FragmentMemoryCleanBinding
import app.allever.android.sample.cleaner.memory.MemoryCleaner
import app.allever.android.sample.cleaner.memory.MemoryMonitor
import app.allever.android.sample.cleaner.memory.ProcessManager
import app.allever.android.sample.cleaner.ui.adapter.ProcessAdapter
import kotlinx.coroutines.launch

/**
 * 内存清理 Fragment
 *
 * 展示内存使用状态、后台进程列表，
 * 提供一键加速功能。
 */
class MemoryCleanFragment :
    BaseFragment<FragmentMemoryCleanBinding, CleanerViewModel>() {

    private lateinit var processAdapter: ProcessAdapter

    override fun inflate(): FragmentMemoryCleanBinding =
        FragmentMemoryCleanBinding.inflate(layoutInflater)

    override fun init() {
        initRecyclerView()
        initClickListeners()
        loadMemoryInfo()
        loadProcesses()
    }

    private fun initRecyclerView() {
        processAdapter = ProcessAdapter()
        mBinding.rvProcesses.adapter = processAdapter
        mBinding.rvProcesses.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(context)
    }

    private fun initClickListeners() {
        mBinding.btnBoost.setOnClickListener { performBoost() }
    }

    private fun loadMemoryInfo() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val info = MemoryMonitor.getMemoryInfo()
                displayMemoryInfo(info)
            }
        }
    }

    private fun loadProcesses() {
        lifecycleScope.launch {
            val processes = ProcessManager.getRunningProcesses()
                .filter { !it.processName.contains(requireContext().packageName) }
                .filter { it.importance > 100 }

            processAdapter.setList(processes)
        }
    }

    private fun performBoost() {
        mBinding.btnBoost.isEnabled = false
        mBinding.btnBoost.text = "正在加速..."

        lifecycleScope.launch {
            val result = MemoryCleaner.releaseMemory()

            val info = MemoryMonitor.getMemoryInfo()
            displayMemoryInfo(info)

            mBinding.btnBoost.isEnabled = true
            mBinding.btnBoost.text = "一键加速"

            if (result.cleanedCount > 0) {
                Toast.makeText(
                    context,
                    "已释放 ${app.allever.android.sample.cleaner.core.CleanEngine.formatSize(result.cleanedSize)}，关闭 ${result.cleanedCount} 个进程",
                    Toast.LENGTH_SHORT
                ).show()
            }

            loadProcesses()
        }
    }

    private fun displayMemoryInfo(info: MemoryMonitor.MemoryInfo) {
        mBinding.progressMemoryUsage.progress = info.usagePercent.toInt()
        mBinding.tvUsedMem.text = info.formattedUsed
        mBinding.tvAvailMem.text = info.formattedAvail
        mBinding.tvTotalMem.text = "总内存：${info.formattedTotal}"

        if (info.lowMemory) {
            setVisibility(mBinding.tvLowMemoryStatus, true)
            mBinding.tvLowMemoryStatus.text = "⚠ 内存不足，建议清理"
        } else {
            setVisibility(mBinding.tvLowMemoryStatus, false)
        }
    }
}
