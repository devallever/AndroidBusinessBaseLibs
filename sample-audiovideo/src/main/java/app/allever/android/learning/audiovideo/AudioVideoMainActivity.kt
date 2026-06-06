package app.allever.android.learning.audiovideo

import app.allever.android.lib.common.BaseActivity
import app.allever.android.lib.core.helper.FragmentHelper
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.audiovideo.R
import app.allever.android.sample.audiovideo.databinding.ActivityAudioVideoMainBinding
import com.alibaba.android.arouter.facade.annotation.Route

@Route(path = "/audiovideo/main")
class AudioVideoMainActivity :
    BaseActivity<ActivityAudioVideoMainBinding, AudioVideoMainViewModel>() {
    override fun init() {
        initTopBar("音视频")
        FragmentHelper.addToContainer(
            supportFragmentManager,
            AudioVideoMainListFragment(),
            R.id.fragmentContainer
        )
    }

    override fun inflateChildBinding() = ActivityAudioVideoMainBinding.inflate(layoutInflater)
}

class AudioVideoMainViewModel : BaseViewModel() {

}