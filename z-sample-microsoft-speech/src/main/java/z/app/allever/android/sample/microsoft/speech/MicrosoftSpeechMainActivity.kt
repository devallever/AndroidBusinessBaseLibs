package z.app.allever.android.sample.microsoft.speech

import androidx.fragment.app.Fragment
import app.allever.android.lib.common.SampleMainActivity
import app.allever.android.lib.common.databinding.ActivitySampleMainBinding
import app.allever.android.lib.mvvm.base.BaseViewModel
import com.alibaba.android.arouter.facade.annotation.Route

@Route(path = "/zmicrosoftspeech/main")
class MicrosoftSpeechMainActivity: SampleMainActivity<ActivitySampleMainBinding, BaseViewModel>() {
    override fun getSampleName(): String = "微软语音"

    override fun getSampleFragment(): Fragment = MicrosoftSpeechFunListFragment()
}