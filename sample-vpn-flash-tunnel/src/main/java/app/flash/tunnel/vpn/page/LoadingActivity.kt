package app.flash.tunnel.vpn.page

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import app.flash.tunnel.vpn.Constants
import app.flash.tunnel.vpn.R
import app.flash.tunnel.vpn.databinding.ActivityLoadingBinding
import app.flash.tunnel.vpn.helper.LogScene
import app.flash.tunnel.vpn.lib.admob.AdCallback
import app.flash.tunnel.vpn.lib.common.util.ActivityManager
import app.flash.tunnel.vpn.page.viewmodel.LoadingViewModel
import kotlinx.coroutines.launch


/**
 * normal progress finish
 *  a: showAd ->> not handle
 *  b: not showAd -> handleFinish() & finishSelf
 *
 *  adShow -> Manual finish progress not finishSelf(showAdFlag  = true)
 *  adDismiss -> handleFinish() & finishSelf
 */
class LoadingActivity : BaseActivity<ActivityLoadingBinding>() {


    companion object {
        const val LOADING_DEFAULT = -1
        const val LOADING_CONNECT = 8
        const val LOADING_DISCONNECT = 9
        const val LOADING_REWARD = 10
        const val LOADING_RETURN_APP = 11

        private const val EXTRA_KEY_TYPE = "type_fl"

        /**
         * bgStopLoc only = LOADING_CONNECT
         */
        fun launch(context: Context, type: Int) {
            //log("start LoadingActivity: $type")
            ActivityManager.start(context, LoadingActivity::class.java) {
                putExtra(EXTRA_KEY_TYPE, type)
            }
        }
    }

    private val mViewModel by viewModels<LoadingViewModel>()

    private val adCallback = object : AdCallback {
        override fun onShow() {
            mViewModel.handleAdShow()
        }

        override fun onFailedToLoad(code: Int, err: String) {
            mViewModel.multiStepProgress.finish()
        }

        override fun onDismiss() {
            mViewModel.handleFinish(this@LoadingActivity, LogScene.AD_FINISH)
            finish()
        }

        override fun onRewarded() {
            mViewModel.mRewardSuccess = true
        }
    }

    override fun inflate() = ActivityLoadingBinding.inflate(layoutInflater)

    override fun init() {
        initView()
        mViewModel.initLoadingType(this, adCallback)
        onBackPressedDispatcher.addCallback {
            if (mViewModel.mType == LOADING_REWARD) {
                setResult(Activity.RESULT_OK, Intent().apply {
                    putExtra(Constants.EXTRA_REWARD_CANCEL, true)
                })
                finish()
            }
        }
    }

    private fun initView() {
        //init intent
        mViewModel.mType = intent?.getIntExtra(EXTRA_KEY_TYPE, LOADING_DEFAULT) ?: LOADING_DEFAULT

        //init delayTime
        mViewModel.mProgressDuration = when (mViewModel.mType) {
            LOADING_CONNECT -> mViewModel.CONNECT_DELAY
            else -> mViewModel.DEFAULT_DELAY
        }
        //init displayText
        mBinding.tvLoading.text = when (mViewModel.mType) {
            LOADING_CONNECT -> getString(R.string.loading_connecting)
            LOADING_DISCONNECT -> getString(R.string.loading_disconnecting)
            else -> getString(R.string.loading)
        }

        //
        mBinding.animView.playAnimation()

        //start progress
        lifecycleScope.launch {
            mViewModel.multiStepProgress.start {
                mBinding.progressBar.progress = it
                mBinding.tvProgress.text = "${it}%"
                if (it >= 100) {
                    if (mViewModel.mAdShow) {
                        //wait for ad dismiss
                    } else {
                        mViewModel.handleFinish(
                            this@LoadingActivity,
                            LogScene.LOADING_PROGRESS_FINISH
                        )
                        finish()
                    }
                }
            }
        }
    }

    override fun onResume() {
        mViewModel.mEnterPageStartTime = System.currentTimeMillis()
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
        mViewModel.handleActivityStop(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewModel.multiStepProgress.cancel()
        mBinding.animView.cancelAnimation()
    }
}