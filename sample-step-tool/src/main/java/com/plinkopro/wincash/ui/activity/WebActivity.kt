package com.plinkopro.wincash.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.plinkopro.wincash.R
import com.plinkopro.wincash.base.BaseActivity
import com.plinkopro.wincash.databinding.ActivityWebBinding
import com.plinkopro.wincash.ui.fragment.WebFragment

private const val PARAM_URL = "param_url"

class WebActivity : BaseActivity<ActivityWebBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val loadUrl = intent.getStringExtra(PARAM_URL) ?: ""
            val mFragment = WebFragment.newInstance(loadUrl)
            supportFragmentManager.beginTransaction()
                .add(R.id.container, mFragment)
                .commit()
        }
    }
    override fun getBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): ActivityWebBinding {
        return ActivityWebBinding.inflate(inflater, container, false)
    }

    companion object {
        fun start(context: Context, url: String?) {
            if (url.isNullOrEmpty()) return
            val intent = Intent(context, WebActivity::class.java).apply {
                putExtra(PARAM_URL, url)
            }
            context.startActivity(intent)
        }
    }
}