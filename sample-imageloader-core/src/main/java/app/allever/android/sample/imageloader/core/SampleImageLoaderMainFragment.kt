package app.allever.android.sample.imageloader.core

import androidx.fragment.app.Fragment
import app.allever.android.lib.common.TabFragment
import app.allever.android.lib.common.TabViewModel
import app.allever.android.lib.common.databinding.FragmentTabBinding

/**
 * 图片加载示例主页面
 *
 * 包含四个演示 Tab：
 * 1. 基础加载 — URL / ResId / Bitmap / File 等多数据源加载
 * 2. 变换效果 — 圆角、圆形、高斯模糊、灰度化
 * 3. 高级用法 — 缓存策略、回调模式、监听器
 * 4. DSL 用法 — ImageView 扩展函数的简洁 API
 */
class SampleImageLoaderMainFragment : TabFragment<FragmentTabBinding, TabViewModel>() {

    override fun getTabTitles(): MutableList<String> = mutableListOf(
        "基础加载", "变换效果", "高级用法", "DSL 示例"
    )

    override fun getFragments(): MutableList<Fragment> = mutableListOf(
        BasicLoadFragment(),
        TransformationFragment(),
        AdvancedFragment(),
        DslUsageFragment()
    )
}
