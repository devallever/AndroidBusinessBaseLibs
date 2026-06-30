package app.android.allever.gp.quick.project.ui

import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import app.allever.android.lib.core.helper.ActivityHelper
import app.android.allever.gp.quick.project.MyApp
import app.android.allever.gp.quick.project.R
import app.android.allever.gp.quick.project.SpeedTest
import app.android.allever.gp.quick.project.base.AppFragment
import app.android.allever.gp.quick.project.databinding.FragmentHistoryBinding
import app.android.allever.gp.quick.project.vm.HistoryViewModel

class HistoryFragment : AppFragment<FragmentHistoryBinding, HistoryViewModel>() {
    private val mSelectedTextColor = ContextCompat.getColor(
        MyApp.context,
        R.color.googleBleu
    )

    private val mUnSelectedTextColor = ContextCompat.getColor(
        MyApp.context,
        R.color.white
    )
    override fun inflate() = FragmentHistoryBinding.inflate(layoutInflater)

    override fun init() {

        mBinding.apply {
            tvTime.setTextColor(mSelectedTextColor)
            tvNetworkType.setTextColor(mUnSelectedTextColor)
            tvDownloadSpeed.setTextColor(mUnSelectedTextColor)
            tvUploadSpeed.setTextColor(mUnSelectedTextColor)

            tvTime.setOnClickListener {
                mViewModel.fetchDataByTimeOrder()

                tvTime.setTextColor(mSelectedTextColor)
                tvNetworkType.setTextColor(mUnSelectedTextColor)
                tvDownloadSpeed.setTextColor(mUnSelectedTextColor)
                tvUploadSpeed.setTextColor(mUnSelectedTextColor)
            }
            tvNetworkType.setOnClickListener {
                mViewModel.fetchDataByNetworkTypeOrder()

                tvTime.setTextColor(mUnSelectedTextColor)
                tvNetworkType.setTextColor(mSelectedTextColor)
                tvDownloadSpeed.setTextColor(mUnSelectedTextColor)
                tvUploadSpeed.setTextColor(mUnSelectedTextColor)
            }
            tvDownloadSpeed.setOnClickListener {
                mViewModel.fetchDataByDownloadSpeedOrder()

                tvTime.setTextColor(mUnSelectedTextColor)
                tvNetworkType.setTextColor(mUnSelectedTextColor)
                tvDownloadSpeed.setTextColor(mSelectedTextColor)
                tvUploadSpeed.setTextColor(mUnSelectedTextColor)
            }
            tvUploadSpeed.setOnClickListener {
                mViewModel.fetchDataByUploadSpeedOrder()

                tvTime.setTextColor(mUnSelectedTextColor)
                tvNetworkType.setTextColor(mUnSelectedTextColor)
                tvDownloadSpeed.setTextColor(mUnSelectedTextColor)
                tvUploadSpeed.setTextColor(mSelectedTextColor)
            }
            rvHistory.adapter = mViewModel.adapter
            rvHistory.layoutManager = LinearLayoutManager(requireContext())
            mViewModel.adapter.apply {
                setOnItemClickListener { adapter, view, position ->
                    SpeedTest.record = mViewModel.adapter.data[position]
                    ActivityHelper.startActivity<DetailActivity>(requireActivity()) { }
                }
                setOnItemLongClickListener { adapter, view, position ->
                    AlertDialog.Builder(requireActivity())
                        .setTitle("提示")
                        .setMessage("是否删除此记录")
                        .setPositiveButton(
                            "是"
                        ) { dialog, which ->
                            mViewModel.deleteRecord(mViewModel.adapter.data[position])
                        }
                        .setNegativeButton("否") { dialog, which ->
                            dialog.dismiss()
                        }
                        .show()
                    true
                }
            }

            mBinding.ivDelete.setOnClickListener {
                AlertDialog.Builder(requireActivity())
                    .setTitle("提示")
                    .setMessage("是否删除全部记录")
                    .setPositiveButton(
                        "是"
                    ) { dialog, which ->
                        mViewModel.deleteAll()
                    }
                    .setNegativeButton("否") { dialog, which ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }

        mViewModel.recordLiveData.observe(this) {
            mViewModel.adapter.data.clear()
            mViewModel.adapter.data.addAll(it)
            mViewModel.adapter.notifyDataSetChanged()

            mBinding.tvRecordCount.text = it.size.toString()
        }

    }

    override fun onResume() {
        super.onResume()
        mViewModel.fetchData()
    }
}