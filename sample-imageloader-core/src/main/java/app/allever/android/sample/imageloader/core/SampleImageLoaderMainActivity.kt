package app.allever.android.sample.imageloader.core

import androidx.fragment.app.Fragment
import app.allever.android.lib.common.SampleMainActivity
import app.allever.android.lib.common.databinding.ActivitySampleMainBinding
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.lib.router.annotation.Route

/**
 * 图片加载组件示例入口
 */
@Route(path = "/imageloader/main")
class SampleImageLoaderMainActivity :
    SampleMainActivity<ActivitySampleMainBinding, BaseViewModel>() {

    override fun getSampleName(): String = "图片加载"

    override fun getSampleFragment(): Fragment = SampleImageLoaderMainFragment()
}
