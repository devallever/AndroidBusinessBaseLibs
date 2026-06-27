package com.clean.wood.ui.fragments

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.clean.wood.R
import com.clean.wood.data.AdManager
import com.clean.wood.databinding.FragmentBaseScanBinding
import com.clean.wood.utils.Constant

abstract class BaseScanFragment : BaseFragment() {

    protected lateinit var mBinding: FragmentBaseScanBinding
    protected var mRotateAnimator: Animator? = null

    override fun backPressedEnable() = true

    override fun onBackPressed() = handleClickBack()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentBaseScanBinding.inflate(layoutInflater)

        ViewCompat.setOnApplyWindowInsetsListener(mBinding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mBinding.apply {
            tvTitle.text = title()
            ivIcon.setImageResource(centerIcon())
            ivFun.setImageResource(centerBg())
            ivBack.setOnClickListener {
                handleClickBack()
            }
        }

        startAnim()

        onStartScan()

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkAd()
        AdManager.ins.showNative(Constant.AdPosition.ScanningNative, mBinding.adContainer)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopAnim()
        AdManager.ins.cancelNativeShow(Constant.AdPosition.ScanningNative)
    }

    private fun startAnim() {
        mRotateAnimator = ObjectAnimator.ofFloat(mBinding.ivFun, "Rotation", 0f, 360f)
        mRotateAnimator?.doOnEnd {
            mBinding.ivFun.post {
                mRotateAnimator?.start()
            }
        }

//        mRotateAnimator?.addListener(object : Animator.AnimatorListener {
//            override fun onAnimationStart(animation: Animator) {
//                log("onAnimator: onAnimationStart")
//            }
//
//            override fun onAnimationEnd(animation: Animator) {
//                log("onAnimator: onAnimationEnd")
//
////                handler.post {
////                    mRotateAnimator?.start()
////                }
////                lifecycleScope.launch(Dispatchers.Main) {
////                }
//            }
//
//            override fun onAnimationCancel(animation: Animator) {
//                log("onAnimator: onAnimationCancel")
//            }
//
//            override fun onAnimationRepeat(animation: Animator) {
//                log("onAnimator: onAnimationRepeat")
//            }
//
//        })

        mRotateAnimator?.duration = 1000
        mRotateAnimator?.interpolator = LinearInterpolator()
        mRotateAnimator?.start()
    }

    private fun checkAd() {
        AdManager.ins.checkAd(Constant.AdPosition.ScanningNative)
        AdManager.ins.checkAd(Constant.AdPosition.ResultNative)

        AdManager.ins.checkAd(Constant.AdPosition.ScanningInter)
        AdManager.ins.checkAd(Constant.AdPosition.ExitInter)
    }

    protected suspend fun showScanningInter() =
        AdManager.ins.showInterAd(Constant.AdPosition.ScanningInter)

    protected fun stopAnim() {
        mRotateAnimator?.cancel()
    }

    open fun centerBg() = R.drawable.ic_scan

    abstract fun onStartScan()

    abstract fun title(): String

    abstract fun centerIcon(): Int

}