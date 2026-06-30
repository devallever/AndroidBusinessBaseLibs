package app.android.allever.gp.quick.project.ui

import androidx.recyclerview.widget.GridLayoutManager
import app.allever.android.lib.core.helper.ActivityHelper
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.android.allever.gp.quick.project.R
import app.android.allever.gp.quick.project.base.AppFragment
import app.android.allever.gp.quick.project.core.ToolsItem
import app.android.allever.gp.quick.project.databinding.FragmentToolsBinding
import app.android.allever.gp.quick.project.ui.adapter.ToolsItemAdapter

class ToolsFragment: AppFragment<FragmentToolsBinding, BaseViewModel>() {
    private val mAdapter = ToolsItemAdapter()
    override fun inflate() = FragmentToolsBinding.inflate(layoutInflater)

    override fun init() {
        mBinding.apply {
            recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
            recyclerView.adapter = mAdapter
            mAdapter.setOnItemClickListener { adapter, view, position ->
                when(position) {
                    0 -> {
                        ActivityHelper.startActivity<WifiSignalActivity>(requireActivity()) {  }
                    }
                    1 -> {
                        ActivityHelper.startActivity<WifiSafeActivity>(requireActivity()) {  }
                    }
                    2 -> {
                        ActivityHelper.startActivity<NetworkCheckActivity>(requireActivity()) {  }
                    }
                    3 -> {
                        ActivityHelper.startActivity<PingActivity>(requireActivity()) {  }
                    }
                    4 -> {
                        ActivityHelper.startActivity<RankActivity>(requireActivity()) {  }
                    }
                    5 -> {
                        ActivityHelper.startActivity<DeviceInfoActivity>(requireActivity()) {  }
                    }
                }
            }

            val data = mutableListOf<ToolsItem>().apply {
                add(ToolsItem(R.drawable.ic_fun_wifi_signal, "WiFi信号测试"))
                add(ToolsItem(R.drawable.ic_fun_wifi_safe, "WiFi安全体检"))
                add(ToolsItem(R.drawable.ic_fun_net_check, "网络诊断"))
                add(ToolsItem(R.drawable.ic_fun_ping, "ping测试"))
                add(ToolsItem(R.drawable.ic_fun_rank, "测速排行"))
                add(ToolsItem(R.drawable.ic_fun_device_info, "设备详情"))
            }
            mAdapter.data.clear()
            mAdapter.data.addAll(data)
            mAdapter.notifyDataSetChanged()
        }


    }
}