package app.allever.android.sample.dj.csj

import android.content.Context
import androidx.fragment.app.Fragment
import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.toJson
import app.allever.android.lib.core.helper.FragmentHelper
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.dj.csj.databinding.FragmentPlayListBinding
import com.bytedance.sdk.djx.DJXSdk
import com.bytedance.sdk.djx.IDJXDramaDetailDelegate
import com.bytedance.sdk.djx.interfaces.listener.IDJXDramaHomeListener
import com.bytedance.sdk.djx.interfaces.listener.IDJXDramaUnlockListener
import com.bytedance.sdk.djx.model.DJXDrama
import com.bytedance.sdk.djx.model.DJXDramaDetailConfig
import com.bytedance.sdk.djx.model.DJXDramaUnlockAdMode
import com.bytedance.sdk.djx.params.DJXWidgetDramaHomeParams

class PlayListFragment : BaseFragment<FragmentPlayListBinding, BaseViewModel>() {
    override fun inflate(): FragmentPlayListBinding =
        FragmentPlayListBinding.inflate(layoutInflater)

    override fun init() {
        FragmentHelper.addToContainer(
            childFragmentManager, createDramaHomeFragment(), mBinding.fragmentContainer.id
        )
    }

    private fun createDramaHomeFragment(): Fragment {
        val detailConfig = DJXDramaDetailConfig.obtain(
            DJXDramaUnlockAdMode.MODE_COMMON, Constants.FREE_SET, object : IDJXDramaUnlockListener {
                override fun unlockFlowEnd(
                    drama: DJXDrama,
                    errCode: IDJXDramaUnlockListener.UnlockErrorStatus?,
                    map: Map<String, Any>?
                ) {
                    log("unlockFlowEnd: ${errCode?.name} ${drama.toJson()}")
                }

                override fun unlockFlowStart(
                    drama: DJXDrama,
                    callback: IDJXDramaUnlockListener.UnlockCallback,
                    map: Map<String, Any>?
                ) {
                    log("unlockFlowStart: ${drama.toJson()}")
                }

            })
        val params =
            DJXWidgetDramaHomeParams.obtain(detailConfig).showBackBtn(false).showPageTitle(false)
                .setTopOffset(80).setEnterDelegate(object : IDJXDramaDetailDelegate {
                    override fun onEnter(context: Context?, djxDrama: DJXDrama, current: Int) {
                        CsjDjHelper.toVideoDetailPage(
                            requireContext(), djxDrama.id, djxDrama.index, current, -1L
                        )
                    }
                }).listener(object : IDJXDramaHomeListener() {
                    override fun onItemClick(p0: DJXDrama?, p1: Map<String?, Any?>?) {
                        log("onItemClick: ${p0?.toJson()}")
                    }
                })

        val dramaHome = DJXSdk.factory().createDramaHome(params)

        return dramaHome.fragment
    }
}