package com.alsg.bakericon.ui

import com.allever.lib.base.function.imageloader.loadCircle
import com.allever.lib.base.helper.ViewHelper
import com.allever.lib.base.util.BarUtils
import com.alsg.bakericon.BuildConfig
import com.alsg.bakericon.Constant
import com.alsg.bakericon.R
import com.alsg.bakericon.base.AppFragment
import com.alsg.bakericon.base.AppFragmentActivity
import com.alsg.bakericon.databinding.FragmentMoreBinding
import com.alsg.bakericon.vm.MoreViewModel

/**
 *@Description
 *@author: zq
 *@date: 2024/1/9
 */
class MoreFragment : AppFragment<FragmentMoreBinding, MoreViewModel>() {
    override fun inflate() = FragmentMoreBinding.inflate(layoutInflater)

    override fun init() {

        mBinding.apply {
            ViewHelper.setMarginTop(llContent, BarUtils.getStatusBarHeight())

            ivLogo.loadCircle(R.drawable.logo)

            favouritesItem.setOnClickListener {
                AppFragmentActivity.start<FavouritesFragment>(getString(R.string.my_favourites)) {

                }
            }
            faqItem.setOnClickListener {
                WebViewFragment.start(Constant.FAQ_URL, getString(R.string.faq))
            }
            privacyItem.setOnClickListener {
                WebViewFragment.start(Constant.PRIVACY_URL, getString(R.string.privacy))
            }

            tvVersion.text = BuildConfig.VERSION_NAME
            tvAboutBaker.text = getString(R.string.about_baker_icon, getString(R.string.app_name))

        }
    }

    override fun initObserver() {

    }
}