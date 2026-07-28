package com.alsg.bakericon.base

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import app.allever.android.lib.core.helper.ActivityHelper
import app.allever.android.lib.core.helper.ViewHelper
import app.allever.android.lib.core.util.BarUtils
import app.allever.android.lib.mvvm.base.BaseViewModel

import com.alsg.bakericon.R
import com.alsg.bakericon.databinding.SiActivityFragmentBinding

/**
 *@Description
 *@author: zq
 *@date: 2024/1/11
 */
class AppFragmentActivity : AppActivity<SiActivityFragmentBinding, BaseViewModel>() {


    companion object {
        inline fun <reified T> start(
            title: String,
            showTopBar: Boolean = true,
            darkMode: Boolean = false
        ) {
            ActivityHelper.startActivity<AppFragmentActivity> {
                putExtra("fragmentName", T::class.java.name)
                putExtra("title", title)
                putExtra("darkMode", darkMode)
                putExtra("showTopBar", showTopBar)
            }
        }

        inline fun <reified T> start(
            title: String,
            showTopBar: Boolean = true,
            darkMode: Boolean = false,
            block: (fragmentArgs: Bundle) -> Unit
        ) {
            val bundle = Bundle()
            block.invoke(bundle)
            ActivityHelper.startActivity<AppFragmentActivity> {
                putExtra("fragmentName", T::class.java.name)
                putExtra("title", title)
                putExtra("showTopBar", showTopBar)
                putExtra("darkMode", darkMode)
                putExtra("fragmentArgs", bundle)
            }
        }

        inline fun <reified T> starts(
            title: String,
            showTopBar: Boolean = true,
            darkMode: Boolean = false,
            block: (fragmentArgs: HashMap<String, *>) -> Unit
        ) {
            val bundle = Bundle()
            val map = hashMapOf<String, Any>()
            block(map)
            map.map {
                when (it.value) {
                    is String -> {
                        bundle.putString(it.key, it.value as String)
                    }
                }
            }
            ActivityHelper.startActivity<AppFragmentActivity> {
                putExtra("fragmentName", T::class.java.name)
                putExtra("title", title)
                putExtra("showTopBar", showTopBar)
                putExtra("darkMode", darkMode)
                putExtra("fragmentArgs", bundle)
            }
        }
    }

    private lateinit var mFragment: Fragment

    override fun inflate() = SiActivityFragmentBinding.inflate(layoutInflater)

    override fun init() {
        ViewHelper.setMarginTop(mBinding.topBar, BarUtils.getStatusBarHeight())
        mBinding.tvTitle.text = (intent?.getStringExtra("title") ?: "")
        mBinding.ivBack.setOnClickListener {
            finish()
        }
        attachFragment()?.apply {
            mFragment = this
        }
        supportFragmentManager.beginTransaction().replace(R.id.contentContainer, mFragment)
            .commit()
    }

    private fun attachFragment(): Fragment? {
        val fragmentArgs = intent?.getBundleExtra("fragmentArgs")
        try {
            val clzName = intent.getStringExtra("fragmentName")
            if (TextUtils.isEmpty(clzName)) {
                return null
            }
            val fragment = Class.forName(clzName!!).getConstructor().newInstance() as Fragment
            fragment.arguments = fragmentArgs
            return fragment
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    override fun isDarkMode(): Boolean {
        return true
    }

}