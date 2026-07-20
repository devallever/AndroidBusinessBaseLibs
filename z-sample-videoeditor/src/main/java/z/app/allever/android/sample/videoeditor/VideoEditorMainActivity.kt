package z.app.allever.android.sample.videoeditor

import androidx.fragment.app.Fragment
import app.allever.android.lib.common.BaseActivity
import app.allever.android.lib.common.SampleMainActivity
import app.allever.android.lib.common.databinding.ActivitySampleMainBinding
import app.allever.android.lib.core.helper.FragmentHelper
import app.allever.android.lib.mvvm.base.BaseViewModel
import com.therouter.router.Route
import z.app.allever.android.sample.videoeditor.databinding.ActivityVideoEditorMainBinding

@Route(path = "/zvideoeditor/main")
class VideoEditorMainActivity : SampleMainActivity<ActivitySampleMainBinding, BaseViewModel>() {

    override fun getSampleName(): String = "视频编辑"

    override fun getSampleFragment(): Fragment = VideoEditorMainFragment()

}