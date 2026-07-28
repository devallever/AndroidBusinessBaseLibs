package com.alsg.bakericon.ui

import app.allever.android.lib.imageloader.core.loadCircle
import app.allever.android.lib.core.helper.ViewHelper
import app.allever.android.lib.core.util.BarUtils
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

            tvVersion.text = "v1.0"
            tvAboutBaker.text = getString(R.string.about_baker_icon, getString(R.string.app_name))

        }
    }
}