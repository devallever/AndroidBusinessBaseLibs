package com.clean.wood.ui.fragments

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.clean.wood.R
import com.clean.wood.data.AdManager
import com.clean.wood.databinding.FragmentAppListBinding
import com.clean.wood.utils.Constant
import com.clean.wood.utils.DisplayUtils
import com.clean.wood.vm.AppListViewModel
import kotlinx.coroutines.launch

class OptimizeAppFragment : BaseFragment() {

    protected lateinit var mBinding: FragmentAppListBinding

    private val mViewModel by viewModels<AppListViewModel>()

    override fun stackKey(): String {
        return "/app_list"
    }

    override fun backPressedEnable() = true

    override fun onBackPressed() = handleClickBack()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentAppListBinding.inflate(layoutInflater)

        ViewCompat.setOnApplyWindowInsetsListener(mBinding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mBinding.apply {
            includeTopBar.tvTitle.text = getString(R.string.fun_app_manage)

            rvApp.layoutManager = LinearLayoutManager(requireContext())
            rvApp.adapter = mViewModel.adapter
            rvApp.addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    val position = rvApp.getChildLayoutPosition(view)
                    if (position == mViewModel.adapter.data.size - 1) {
                        outRect.bottom = DisplayUtils.dip2px(108)
                    }
                }
            })

            btnUninstall.setOnClickListener {
                mViewModel.openSystemAppManage()
            }

            includeTopBar.ivBack.setOnClickListener {
                handleClickBack()
            }
        }
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel.checkAd()
    }

    override fun onResume() {
        super.onResume()
        if (mViewModel.jumpResult) {
            jumpResult()
        }
    }

    private fun jumpResult() {
        pop()
        pushFragment(
            ResultFragment.newIns(
                Constant.FunType.APP_MANAGER,
                getString(R.string.fun_app_manage)
            )
        )
    }
}