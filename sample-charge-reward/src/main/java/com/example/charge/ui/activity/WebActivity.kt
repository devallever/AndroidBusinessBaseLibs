package com.example.charge.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.charge.R
import com.example.charge.base.BaseActivity
import com.example.charge.databinding.ActivityWebBinding
import com.example.charge.ui.fragment.WebFragment
import kotlin.apply
import kotlin.jvm.java
import kotlin.text.isNullOrEmpty

private const val PARAM_URL = "param_url"

class WebActivity : BaseActivity<ActivityWebBinding>() {

    override fun initView() {
    }

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