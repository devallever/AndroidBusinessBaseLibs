/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.allever.android.ai.qr.scanner.ui

import android.app.Activity
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup


internal class FragmentTabPagerAdapter(
        private val mContext: Activity,
        private val mFragmentManager: androidx.fragment.app.FragmentManager
) : androidx.viewpager.widget.PagerAdapter() {

    private val mFragmentCache: MutableMap<TabModel.Tab, Fragment> = mutableMapOf()

    private var mCurrentTransaction: androidx.fragment.app.FragmentTransaction? = null

    private var mCurrentPrimaryItem: Fragment? = null

    override fun getCount(): Int {
        return TabModel.tabCount
    }

    fun getCurrentFragment(): Fragment {
        return mCurrentPrimaryItem!!
    }

    fun getFragment(position: Int): Fragment {
        // Fetch the tab the UiDataModel reports for the position.
        val tab = TabModel.getTabAt(position)

        // First check the local cache for the fragment.
        var fragment: Fragment? = mFragmentCache[tab]
        if (fragment != null) {
            return fragment
        }

        // Next check the fragment manager; relevant when app is rebuilt after locale changes
        // because this adapter will be new and mFragmentCache will be empty, but the fragment
        // manager will retain the Fragments built on original application launch.
        fragment = mFragmentManager.findFragmentByTag(tab.name) as? BaseFragment
        if (fragment != null) {
            mFragmentCache[tab] = fragment
            return fragment
        }

        // Otherwise, build the fragment from scratch.
        val fragmentClassName = tab.fragmentClassName
        val bundle = tab.bundle


        when (position) {
            0 -> {
                fragment = ScannerFragment()
            }
            else -> {
                fragment =
                    GeneratorFragment()
            }

//            2 -> {
//                fragment = CreatorFragment()
//            }
//            else -> {
//                fragment = SettingFragment()
//            }
        }

//        fragment = Fragment.instantiate(mContext, fragmentClassName, bundle) as BaseFragment

        mFragmentCache[tab] = fragment

        if (fragment is ScannerFragment) {
            fragment.intent = mContext.intent
        }
        return fragment
    }

    override fun startUpdate(container: ViewGroup) {
        if (container.id == View.NO_ID) {
            throw IllegalStateException("ViewPager with adapter " + this + " has no id")
        }
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        if (mCurrentTransaction == null) {
            mCurrentTransaction = mFragmentManager.beginTransaction()
        }

        // Use the fragment located in the fragment manager if one exists.
        val tab = TabModel.getTabAt(position)
        var fragment = mFragmentManager.findFragmentByTag(tab.name)
        if (fragment != null) {
            mCurrentTransaction!!.attach(fragment)
        } else {
            fragment = getFragment(position)
            mCurrentTransaction!!.add(container.id, fragment, tab.name)
        }
        if (fragment !== mCurrentPrimaryItem) {
            fragment.setMenuVisibility(false)
            fragment.userVisibleHint = false
        }

        return fragment
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        if (mCurrentTransaction == null) {
            mCurrentTransaction = mFragmentManager.beginTransaction()
        }
        if (`object` is Fragment) {
            mCurrentTransaction?.detach(`object`)
        }
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        val fragment = `object` as Fragment
        if (fragment !== mCurrentPrimaryItem) {
            mCurrentPrimaryItem?.setMenuVisibility(false)
            mCurrentPrimaryItem?.userVisibleHint = false
            mCurrentPrimaryItem = fragment
            mCurrentPrimaryItem?.setMenuVisibility(true)
            mCurrentPrimaryItem?.userVisibleHint = true
        }
    }

    override fun finishUpdate(container: ViewGroup) {
        if (mCurrentTransaction != null) {
            mCurrentTransaction?.commitAllowingStateLoss()
            mCurrentTransaction = null
        }
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return (`object` as Fragment).view === view
    }
}