package app.android.gp.ai.translator.ui

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.TextView
import app.allever.android.lib.core.helper.FeedbackHelper
import app.android.gp.ai.translator.R
import app.android.gp.ai.translator.app.AppActivity
import app.android.gp.ai.translator.databinding.AGuideBinding

class GuidePage : AppActivity() {

    private lateinit var mBinding: AGuideBinding
    override fun getContentView(): Any {
        mBinding = AGuideBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun initView() {
        addStatusBar(mBinding.rootLayout, findViewById(R.id.top_bar))
        findViewById<View>(R.id.iv_left).setOnClickListener {
            finish()
        }
        findViewById<TextView>(R.id.tv_label).text = getString(R.string.setting_guide)

        mBinding.btnFeedback.setOnClickListener {
            FeedbackHelper.feedback(this)
        }

    }

    override fun initData() {
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, GuidePage::class.java)
            context.startActivity(intent)
        }
    }
}