package app.android.gp.ai.translator.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.android.gp.ai.translator.app.mvp.BaseMvpFragment
import app.android.gp.ai.translator.app.mvp.BasePresenter
import app.allever.android.lib.core.ext.log

abstract class AppMvpFragment<V, P : BasePresenter<V>> : BaseMvpFragment<V, P>() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        log("fwoj390852u.ekjtowu0.xje0w")
        super.onCreateView(inflater, container, savedInstanceState)
        log("fwoj390852u.ekjtowu0.xje0w")
        val view = getContentView()
        log("fwoj390852u.ekjtowu0.xje0w")
        initView(view)
        log("fwoj390852u.ekjtowu0.xje0w")
        initData()
        log("fwoj390852u.ekjtowu0.xje0w")
        return view
    }

    abstract fun getContentView(): View
    abstract fun initView(root: View)
    abstract fun initData()
}