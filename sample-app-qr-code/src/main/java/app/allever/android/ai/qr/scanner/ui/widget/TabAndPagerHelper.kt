package app.allever.android.ai.qr.scanner.ui.widget

import com.android.absbase.utils.ResourcesUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import app.android.base.lib.tab.TabLayout
import com.allever.app.qr.code.scaner.R

class TabAndPagerHelper(val tabs: TabLayout, val viewPager: androidx.viewpager.widget.ViewPager) : TabLayout.OnTabSelectedListener {
    var adapter: Adapter? = null
        set(value) {
            field = value
            if (value != null) {
                for (i in 0 until value.getCount()) {
                    val tv = TextView(tabs.context)
                    tv.text = value.getTitle(i)
                    tv.gravity = Gravity.CENTER
                    tv.setTextColor(ResourcesUtils.getColor(R.color.main_tab_unselect_color))
                    val tab = tabs.newTab()
                            .setCustomView(tv)
                    tabs.addTab(tab)
                }
                viewPager.adapter = BasePagerAdapter(value)
            }
        }

    init {
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.setOnTabSelectedListener(this)
//        tabs.setSelectedTabIndicatorWidth(DeviceUtils.dip2px(8f))
//        tabs.setSelectedTabIndicatorHeight(DeviceUtils.dip2px(2f))
//        tabs.setSelectedTabIndicatorColor(mainTabHighlight)
        tabs.needDrawSelectedIndicator(false)
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        viewPager.currentItem = tab?.position ?: 0

        for (i in 0 until tabs.tabCount) {
            val tabAt = tabs.getTabAt(i)
            if (tabAt != null) {
                val textView = tabAt.customView as? TextView
                if (tabAt == tab) {
                    textView?.setTextColor(ResourcesUtils.getColor(R.color.main_tab_highlight))
                } else {
                    textView?.setTextColor(ResourcesUtils.getColor(R.color.main_tab_unselect_color))
                }
            }
        }
        adapter?.pageSelected(viewPager.currentItem)
    }

    fun currentItem(): Int {
        return viewPager.currentItem
    }


    internal class BasePagerAdapter(val adapter: Adapter) : androidx.viewpager.widget.PagerAdapter() {

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun getCount(): Int {
            return adapter.getCount()
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view = adapter.getView(position)
            container?.addView(view)
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container?.removeView(`object` as View)
        }

        override fun getPageTitle(position: Int): CharSequence {
            return adapter.getTitle(position)
        }
    }

    interface Adapter {
        fun getCount(): Int
        fun getView(position: Int): View
        fun getTitle(position: Int): String
        fun pageSelected(position: Int)
    }
}