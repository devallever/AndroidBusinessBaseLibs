package app.android.allever.gp.quick.project.ui

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.REVERSE
import android.view.animation.LinearInterpolator
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.helper.ActivityHelper
import app.allever.android.lib.core.helper.DeviceHelper
import app.android.allever.gp.quick.project.SpeedTest
import app.android.allever.gp.quick.project.base.AppFragment
import app.android.allever.gp.quick.project.databinding.FragmentSpeedTestBinding
import app.android.allever.gp.quick.project.util.InternetUtil
import app.android.allever.gp.quick.project.util.NetworkOperator
import app.android.allever.gp.quick.project.util.PhoneStateUtils
import app.android.allever.gp.quick.project.vm.SpeedTestViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SpeedTestFragment : AppFragment<FragmentSpeedTestBinding, SpeedTestViewModel>() {
    private val ringScaleXAnimator by lazy {
        val animator = ObjectAnimator.ofFloat(mBinding.ringCircle, "scaleX", 1F, 0.5F)
        animator.repeatCount = -1
        animator.repeatMode = REVERSE
        animator.duration = 2000
        animator.interpolator = LinearInterpolator()
        animator
    }

    private val ringScaleYAnimator by lazy {
        val animator = ObjectAnimator.ofFloat(mBinding.ringCircle, "scaleY", 1F, 0.5F)
        animator.repeatCount = -1
        animator.repeatMode = REVERSE
        animator.duration = 2000
        animator.interpolator = LinearInterpolator()
        animator
    }

    override fun inflate() = FragmentSpeedTestBinding.inflate(layoutInflater)

    override fun init() {

        PhoneStateUtils.registerPhoneStateListener(requireActivity()) {

        }

        mBinding.gaugeView.prepareGauge(requireContext())
        mViewModel.loadServers {}

        mBinding.btnSpeedTest.setOnClickListener {
            showPingContent()
            startPing()
        }

        mBinding.btnRetest.setOnClickListener {
            showInitContent()
        }
        mBinding.btnDetail.setOnClickListener {
            SpeedTest.record = mViewModel.record
            ActivityHelper.startActivity<DetailActivity>(requireActivity()) {

            }
        }
        showSpeedTestContent()
        lifecycleScope.launch {
            delay(100)
            showInitContent()
        }

        ringScaleXAnimator.start()
        ringScaleYAnimator.start()

        initObserver()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        PhoneStateUtils.unRegisterPhoneStateListener(requireActivity())
        mViewModel.stop()
    }

    private fun initObserver() {
        mViewModel.pingResultLiveData.observe(this) {
            mBinding.tvPingResult.text = it.toString()
        }

        mViewModel.downloadTestResult.observe(this) {
            mBinding.tvDownloadResult.text = it.toString()
            mBinding.ivDownload.isVisible = false
            mBinding.downloadProgress.isVisible = false
            mBinding.tvDownloadResult.isVisible = true

            val animator = ValueAnimator.ofFloat(it.toFloat(), 0F).apply {
                duration = 1000
                addUpdateListener {
                    val value = it.animatedValue as Float
                    mBinding.gaugeView.setProgress(value)
                }
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {

                    }

                    override fun onAnimationEnd(animation: Animator, isReverse: Boolean) {
                        lifecycleScope.launch {
                            delay(1000)
                            mViewModel.startUploadTest(mViewModel.targetUrl)
                            mBinding.gaugeView.setGaugeText("Upload speed")
                        }
                    }

                    override fun onAnimationEnd(animation: Animator) {


                    }

                    override fun onAnimationCancel(animation: Animator) {

                    }

                    override fun onAnimationRepeat(animation: Animator) {

                    }
                })
            }
            animator.interpolator = LinearInterpolator()
            animator.start()
        }

        mViewModel.downloadProgressLiveData.observe(this) {
            mBinding.gaugeView.setProgress(it.toFloat())
        }

        mViewModel.uploadTestResult.observe(this) {
            mBinding.tvUploadResult.text = it.toString()
            mBinding.ivUpload.isVisible = false
            mBinding.uploadProgress.isVisible = false
            mBinding.tvUploadResult.isVisible = true

            val animator = ValueAnimator.ofFloat(it.toFloat(), 0F).apply {
                duration = 1000
                addUpdateListener {
                    val value = it.animatedValue as Float
                    mBinding.gaugeView.setProgress(value)
                }
            }
            animator.interpolator = LinearInterpolator()
            animator.start()

            showSpeedTestFinishContent()

            mViewModel.saveRecord()
        }

        mViewModel.uploadProgressLiveData.observe(this) {
            mBinding.gaugeView.setProgress(it.toFloat())
        }

    }

    private fun startPing() {
        ValueAnimator.ofInt(0, 100).apply {
            duration = 3000
            interpolator = LinearInterpolator()
            addUpdateListener {
                val value = it.animatedValue as Int
                mBinding.progressBar.progress = value
                mBinding.tvPingProgress.text = "$value %"
                if (value >= 100) {
                    showSpeedTestContent()
                    lifecycleScope.launch {
                        delay(2000)
                        mViewModel.startDownloadTest(
                            mViewModel.targetUrl.replace(
                                "upload.php",
                                ""
                            )
                        ) {

                        }
                    }

                }
            }
        }.start()
        mViewModel.startPing()
    }

    private fun showInitContent() {
        mBinding.apply {
            initContent.isVisible = true
            pingContent.isVisible = false
            resultContent.isVisible = false

            tvNetworkTypeTips.isVisible = !InternetUtil.isWifi(requireContext())
        }

    }

    private fun showPingContent() {
        mBinding.apply {
            initContent.isVisible = false
            pingContent.isVisible = true
            resultContent.isVisible = false
        }
    }

    private fun showSpeedTestContent() {
        mBinding.apply {
            initContent.isVisible = false
            pingContent.isVisible = false
            resultContent.isVisible = true

            downloadProgress.isVisible = true
            ivDownload.isVisible = true
            tvDownloadResult.isVisible = false

            uploadProgress.isVisible = true
            ivUpload.isVisible = true
            tvUploadResult.isVisible = false

            gaugeView.setGaugeText("Download speed")

            btnDetailContainer.isVisible = false

            val mccMnc = DeviceHelper.getMCC_MNC9(requireContext())
            log("mccMnc = $mccMnc")
            val opName = NetworkOperator.from(mccMnc.toInt())
            log("opName = $opName")
            tvNetOperator.text = "运营商：$opName"

            var strength = PhoneStateUtils.getCurrentSignalStrength().toString()
            if (strength.length >= 3) {
                strength = strength.substring(0, 2)
            }
            tvSignal.text = "强度：$strength dBm"

            tvInternetType.text = InternetUtil.getNetworkStateName(requireContext())
        }
    }

    private fun showSpeedTestFinishContent() {
        mBinding.apply {
            btnDetailContainer.isVisible = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ringScaleXAnimator.cancel()
        ringScaleYAnimator.cancel()
    }


}