package z.app.allever.android.learning.audiovideo

import app.allever.android.lib.common.BaseActivity
import app.allever.android.lib.core.helper.FragmentHelper
import app.allever.android.lib.mvvm.base.BaseViewModel
import z.app.allever.android.sample.audiovideo.R
import z.app.allever.android.sample.audiovideo.databinding.ActivityAudioVideoMainBinding
import app.allever.android.lib.router.annotation.Route

@Route(path = "/zaudiovideo/main")
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