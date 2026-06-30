package com.step.wincash.base

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.step.wincash.utils.dp2px
import com.step.wincash.utils.getStatusBarHeight
import org.greenrobot.eventbus.EventBus

abstract class BaseFragment<T : ViewBinding> : Fragment() {

    lateinit var binding: T

    abstract fun getBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): T

    companion object {
        inline fun <reified A : BaseActivity<*>> goTo(context: Context) {
            val intent = Intent(context, A::class.java)
            context.startActivity(intent)
        }

        // 如果需要传递额外参数
        inline fun <reified A : BaseActivity<*>> goTo(
            context: Context,
            block: Intent.() -> Unit
        ) {
            val intent = Intent(context, A::class.java)
            intent.block()
            context.startActivity(intent)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val auto = SystemBarStyle.auto(Color.BLACK, Color.BLACK) { resources: Resources? -> false }
        requireActivity().enableEdgeToEdge(auto, auto)
        binding = getBinding(inflater, container)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterEventbus()
    }

    protected fun registerEventbus() {
        EventBus.getDefault().register(this)
    }

    protected fun unregisterEventbus() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    protected fun fixStatusBar(targetView: View, appendDp: Int = 0){
        targetView.post {
            val statusBarHeight = getStatusBarHeight( requireContext())
            val lp = targetView.layoutParams as ViewGroup.MarginLayoutParams
            lp.topMargin = statusBarHeight + dp2px(appendDp.toFloat())
            targetView.layoutParams = lp
        }
    }

}