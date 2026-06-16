package app.flash.tunnel.vpn.page

import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import app.flash.tunnel.vpn.R
import app.flash.tunnel.vpn.databinding.ActivityListBinding
import app.flash.tunnel.vpn.helper.TunnelHelper
import app.flash.tunnel.vpn.lib.common.ext.loadCircle
import app.flash.tunnel.vpn.lib.common.util.log
import app.flash.tunnel.vpn.page.adapter.ServerAdapter
import app.flash.tunnel.vpn.page.viewmodel.ServerViewModel

class ListActivity : BaseActivity<ActivityListBinding>() {

    private val mViewModel by viewModels<ServerViewModel>()
    override fun inflate() = ActivityListBinding.inflate(layoutInflater)

    override fun init() {
        fixStatusBar(mBinding.topBar)
        mBinding.ivClose.setOnClickListener {
            finish()
        }

        mBinding.rvServer.layoutManager = GridLayoutManager(this@ListActivity, 2).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    val type = mViewModel.adapter.data[position].type
                    return if (type == ServerAdapter.TYPE_AD) {
                        2
                    } else {
                        1
                    }
                }
            }
        }
        mBinding.rvServer.adapter = mViewModel.adapter
        mViewModel.adapter.updateSelectPosition()
        mViewModel.updateSelectedIndex()
        mViewModel.adapter.callback = { position, item ->
            mViewModel.handleClickItem(this@ListActivity, mViewModel.adapter, item, position)
            updateNodeUi()
        }

        updateNodeUi()
    }

    private fun updateNodeUi() {
        val isSmartMode = TunnelHelper.isSmartMode()

        if (TunnelHelper.isServiceConnected()) {
            //update top
            val node = TunnelHelper.getConnectedNodeItem()
            if (node == null) {
                mBinding.ivFlag.loadCircle(R.drawable.icon_default_region)
                return
            }
            node.let {
                TunnelHelper.loadRegionsFlag(mBinding.ivFlag, it.cc)
            }
        } else {
            //update bottom
            val node = TunnelHelper.getSelectedNodeItem()
            log("updateConnectedNodeUi: isSmartNode -> $isSmartMode")
            log("updateConnectedNodeUi: node == null -> ${node == null}")
            if (node == null || isSmartMode) {
                mBinding.ivFlag.loadCircle(R.drawable.icon_default_region)
            } else {
                node.let {
                    TunnelHelper.loadRegionsFlag(mBinding.ivFlag, it.cc)
                }
            }
        }
    }
}