package app.android.gp.ai.translator.ui

import android.view.View
import android.widget.TextView
import app.android.gp.ai.translator.R
import app.android.gp.ai.translator.app.AppMvpActivity
import app.android.gp.ai.translator.databinding.ASettingBinding
import app.android.gp.ai.translator.ui.mvp.presenter.SettingPresenter
import app.android.gp.ai.translator.ui.mvp.view.SettingView

class SettingPage : AppMvpActivity<SettingView, SettingPresenter>(), SettingView {
    private lateinit var mBinding: ASettingBinding

    override fun getContentView(): Any  {
        mBinding = ASettingBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun initView() {
        addStatusBar(mBinding.rootLayout, findViewById(R.id.top_bar))
        findViewById<TextView>(R.id.tv_label).text = getString(R.string.setting)
        findViewById<View>(R.id.iv_left).setOnClickListener {
            finish()
        }
    }

    override fun initData() {

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, SettingFragmentPage.newInstance())
        transaction.commit()
    }

    override fun createPresenter(): SettingPresenter = SettingPresenter()
}