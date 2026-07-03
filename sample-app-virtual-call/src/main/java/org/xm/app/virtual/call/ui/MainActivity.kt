package org.xm.app.virtual.call.ui

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.allever.app.virtual.call.R
import org.xm.app.virtual.call.app.BaseActivity
import org.xm.app.virtual.call.service.VirtualCallService
import org.xm.app.virtual.call.ui.mvp.presenter.MainPresenter
import org.xm.app.virtual.call.ui.mvp.view.MainView

class MainActivity : BaseActivity<MainView, MainPresenter>(),
    MainView, View.OnClickListener {

    override fun getContentView(): Any = R.layout.vc_activity_main

    override fun initView() {
        findViewById<TextView>(R.id.tv_label).text = getString(R.string.vc_app_name)
        findViewById<View>(R.id.iv_left).visibility = View.GONE
        findViewById<View>(R.id.main_btn_start).setOnClickListener(this)
        findViewById<View>(R.id.main_btn_call_setting).setOnClickListener(this)
        findViewById<View>(R.id.main_btn_call_guide).setOnClickListener(this)
        val rightView = findViewById<ImageView>(R.id.iv_right)
        rightView.setOnClickListener(this)
        rightView.setImageResource(R.drawable.vc_ic_setting)
        rightView.visibility = View.VISIBLE
    }

    override fun initData() {
    }

    override fun createPresenter(): MainPresenter =
        MainPresenter()

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_left -> {
                finish()
            }
            R.id.iv_right -> {
                SettingActivity.start(this)
            }
            R.id.main_btn_start -> {
                //启动服务，倒计时启动Activity
                VirtualCallService.start(this)
                finish()
            }
            R.id.main_btn_call_setting -> {
                InComeCallSettingActivity.start(
                    this
                )
            }

            R.id.main_btn_call_guide -> {
                GuideActivity.start(
                    this
                )
            }

        }
    }

    override fun updateContact(name: String) {

    }

    override fun updatePhone(phone: String) {

    }
}
