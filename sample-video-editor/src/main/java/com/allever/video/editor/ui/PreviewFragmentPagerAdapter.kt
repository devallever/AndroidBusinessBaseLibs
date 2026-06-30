package com.allever.video.editor.ui

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.allever.video.editor.ui.bean.ThumbnailBean

class PreviewFragmentPagerAdapter(fragmentManager: androidx.fragment.app.FragmentManager, data: MutableList<ThumbnailBean>) :
    androidx.fragment.app.FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    var data: MutableList<ThumbnailBean>? = data
    var currentFragment: androidx.fragment.app.Fragment? = null

    override fun getItem(position: Int): Fragment {
        return PreviewFragment()
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val fragment = super.instantiateItem(container, position)
        if (fragment is PreviewFragment) {
            val fragmentData = data
            if (fragmentData != null && position in 0 until fragmentData.size) {
                fragment.setData(fragmentData[position])
            }
        }
        currentFragment = fragment as androidx.fragment.app.Fragment
        return fragment
    }

    override fun getCount(): Int = data?.size ?: 0

}