package app.allever.android.sample.media.core

import app.allever.android.lib.common.SampleMainActivity
import app.allever.android.lib.common.databinding.ActivitySampleMainBinding
import app.allever.android.lib.mvvm.base.BaseViewModel
import com.therouter.router.Route

@Route(path = "/media/main")
class MediaSampleMainActivity: SampleMainActivity<ActivitySampleMainBinding, BaseViewModel>() {
    override fun getSampleName() = "MediaCore"

    override fun getSampleFragment() = MediaSampleTabFragment()
}