package app.allever.android.sample.dj.csj

import android.content.Context
import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.toJson
import app.allever.android.lib.core.helper.FragmentHelper
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.dj.csj.databinding.FragmentRecommendBinding
import com.bytedance.sdk.djx.DJXSdk
import com.bytedance.sdk.djx.IDJXDramaDetailDelegate
import com.bytedance.sdk.djx.IDJXWidget
import com.bytedance.sdk.djx.interfaces.listener.IDJXAdListener
import com.bytedance.sdk.djx.interfaces.listener.IDJXDramaUnlockListener
import com.bytedance.sdk.djx.model.DJXDrama
import com.bytedance.sdk.djx.model.DJXDramaDetailConfig
import com.bytedance.sdk.djx.model.DJXDramaUnlockAdMode
import com.bytedance.sdk.djx.params.DJXWidgetDrawParams

class RecommendFragment : BaseFragment<FragmentRecommendBinding, BaseViewModel>(),
    OnPlayInfoListener {

    private var djxWidget: IDJXWidget? = null
    private var drawListener: MyIDJXDrawListener? = null

    val GIFT_COUNT: Int = 100
    private var five_second = 0

    override fun inflate(): FragmentRecommendBinding =
        FragmentRecommendBinding.inflate(layoutInflater)

    override fun init() {
        if (DJXSdk.isStartSuccess()) {
            initRecommandFragment()
            FragmentHelper.addToContainer(
                childFragmentManager,
                djxWidget?.fragment ?: return,
                mBinding.fragmentContainer.id
            )
        }
    }

    private fun initRecommandFragment() {
        DJXSdk.service().setGlobalSpeedPlay(1f)
        val dramaDetailConfig = DJXDramaDetailConfig.obtain(
            DJXDramaUnlockAdMode.MODE_COMMON, Constants.FREE_SET, object : IDJXDramaUnlockListener {
                override fun unlockFlowEnd(
                    drama: DJXDrama,
                    errCode: IDJXDramaUnlockListener.UnlockErrorStatus?,
                    map: Map<String, Any>?
                ) {
                    log("unlockFlowEnd: ${drama.toJson()}")

                }

                override fun unlockFlowStart(
                    drama: DJXDrama,
                    callback: IDJXDramaUnlockListener.UnlockCallback,
                    map: Map<String, Any>?
                ) {
                    log("unlockFlowStart: ${drama.toJson()}")
                }

            }).hideLongClickSpeed(true).hideLikeButton(true).hideFavorButton(true)
            .hideCellularToast(true).listener(null) // 短剧详情页视频播放回调
            .adListener(object : IDJXAdListener() {

            }) // 短剧详情页激励视频回调

        drawListener = MyIDJXDrawListener(this)
        val drawParams = DJXWidgetDrawParams.obtain().adOffset(0) //单位 dp，为 0 时可以不设置
            .drawContentType(DJXWidgetDrawParams.DRAW_CONTENT_TYPE_ONLY_DRAMA)
            .drawChannelType(DJXWidgetDrawParams.DRAW_CHANNEL_TYPE_RECOMMEND).hideClose(true, null)
            .hideChannelName(true).detailConfig(dramaDetailConfig).hideLongClickSpeed(false)
            .hideDoubleClickLike(true).hideLikeButton(true).hideFavorButton(true).bottomOffset(0)
            .setEnterDelegate(object : IDJXDramaDetailDelegate {
                override fun onEnter(context: Context?, djxDrama: DJXDrama, current: Int) {
                    CsjDjHelper.toVideoDetailPage(
                        requireContext(), djxDrama.id, djxDrama.index, current, -1L
                    )
                }
            }).listener(drawListener) // 混排流内视频监听
            .adListener(null) // 混排流内广告监听
        djxWidget = DJXSdk.factory().createDraw(drawParams)
        djxWidget!!.fragment.setMenuVisibility(userVisibleHint)

        //单集
        //widget.setSpeedPlay(speed, DJXPlaySpeedScope.DJX_VIDEO_SPEED_SCOPE_EPISODE)
        //全局倍速
        //  widget.setSpeedPlay(speed, DJXPlaySpeedScope.DJX_VIDEO_SPEED_SCOPE_DRAMA)
    }


    override fun onDJXPageChange() {

    }

    override fun onPlaySpeedBtnClick(holderKey: String?, speed: String?) {
    }

    override fun onChangePlaySpeed(speed: String?) {
    }

    override fun onDJXVideoPlay(drama_id: Long, ep_index: Int) {
    }

    override fun onDJXVideoPause() {
    }

    override fun onDJXVideoContinue() {
    }

    override fun onDJXVideoCompletion(drama_id: Long, ep_index: Int) {
    }

    override fun onShareClick() {
    }

    override fun unlockFlowStart() {
    }
}