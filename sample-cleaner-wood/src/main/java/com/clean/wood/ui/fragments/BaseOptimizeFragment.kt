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
import androidx.lifecycle.lifecycleScope
import com.clean.wood.R
import com.clean.wood.data.AdManager
import com.clean.wood.databinding.FragmentBaseOptimizeBinding
import com.clean.wood.utils.Constant
import kotlinx.coroutines.launch

abstract class BaseOptimizeFragment : BaseFragment() {

    protected lateinit var mBinding: FragmentBaseOptimizeBinding
    protected var mRotateAnimator: Animator? = null

    override fun backPressedEnable() = true

    override fun onBackPressed() = handleClickBack()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentBaseOptimizeBinding.inflate(layoutInflater)

        ViewCompat.setOnApplyWindowInsetsListener(mBinding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mBinding.apply {
            tvTitle.text = title()
            ivIcon.setImageResource(centerIcon())
            ivFun.setImageResource(centerBg())
            tvFun.text = funDesc()
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
        AdManager.ins.showNative(Constant.AdPosition.OptimizingNative, mBinding.adContainer)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopAnim()
        AdManager.ins.cancelNativeShow(Constant.AdPosition.OptimizingNative)
    }

    private fun startAnim() {
        mBinding.ivFun.rotation
        mRotateAnimator = ObjectAnimator.ofFloat(mBinding.ivFun, "Rotation", 0f, 360f)
        mRotateAnimator?.doOnEnd {
            mBinding.ivFun.post {
                mRotateAnimator?.start()
            }
        }
        mRotateAnimator?.duration = 1000
        mRotateAnimator?.interpolator = LinearInterpolator()
        mRotateAnimator?.start()
    }

    protected fun stopAnim() {
        mRotateAnimator?.cancel()
    }

    private fun checkAd() {
        AdManager.ins.checkAd(Constant.AdPosition.OptimizingNative)

        AdManager.ins.checkAd(Constant.AdPosition.OptimizingInter)
    }

    protected fun waiting(startTime: Long) {
        waitingAd2(
            startTime,
            check = {
                AdManager.ins.isAdReadyNext(Constant.AdPosition.OptimizingInter)
            }, next = {
                lifecycleScope.launch {
                    AdManager.ins.showInterAd(Constant.AdPosition.OptimizingInter)
                    jumpResult()
                }
            }, timeOut = {
                jumpResult()
            })

    }

    private fun jumpResult() {
        pop()
        pushFragment(ResultFragment.newIns(funType(), title()))
    }

    protected fun centerBg() = R.drawable.ic_optimize

    abstract fun onStartScan()

    abstract fun title(): String

    abstract fun centerIcon(): Int

    abstract fun funDesc(): String

    abstract fun funType(): Int
}