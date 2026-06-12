package app.allever.android.sample.cleaner.ui.fragment

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import app.allever.android.lib.common.BaseFragment
import app.allever.android.sample.cleaner.CleanerViewModel
import app.allever.android.sample.cleaner.databinding.FragmentFileManageBinding
import com.google.android.material.tabs.TabLayoutMediator

/**
 * 文件管理 Fragment（主容器）
 *
 * 使用 ViewPager2 + TabLayout 承载 3 个独立子页面：
 * - Tab 0: 大文件扫描 (LargeFileFragment)
 * - Tab 1: 重复文件检测 (DuplicateFileFragment)
 * - Tab 2: 文件分类浏览 (FileCategoryFragment)
 *
 * 每个子页面相互独立，各自维护自己的状态和 UI。
 */
class FileManageFragment :
    BaseFragment<FragmentFileManageBinding, CleanerViewModel>() {

    private val tabTitles = listOf("大文件", "重复文件", "文件分类")

    override fun inflate(): FragmentFileManageBinding =
        FragmentFileManageBinding.inflate(layoutInflater)

    override fun init() {
        setupViewPager()
        setupTabLayout()
    }

    private fun setupViewPager() {
        mBinding.viewPager.adapter = FileManagePagerAdapter(this)

        // 预加载相邻页面，提升切换体验
        mBinding.viewPager.offscreenPageLimit = 1
    }

    private fun setupTabLayout() {
        TabLayoutMediator(mBinding.tabFileCategory, mBinding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

    // ========== ViewPager2 Adapter ==========

    /**
     * 文件管理页面的 ViewPager2 Adapter
     */
    private inner class FileManagePagerAdapter(fragment: Fragment) :
        FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment = when (position) {
            0 -> LargeFileFragment()
            1 -> DuplicateFileFragment()
            2 -> FileCategoryFragment()
            else -> LargeFileFragment()
        }
    }
}
