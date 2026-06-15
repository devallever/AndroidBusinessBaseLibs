package app.allever.android.sample.dj.csj

import app.allever.android.lib.common.BaseActivity
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.dj.csj.databinding.ActivityVideoDetailBinding

class VideoDetailActivity: BaseActivity<ActivityVideoDetailBinding, BaseViewModel>() {
    override fun inflateChildBinding(): ActivityVideoDetailBinding = ActivityVideoDetailBinding.inflate(layoutInflater)

    override fun init() {
    }
}