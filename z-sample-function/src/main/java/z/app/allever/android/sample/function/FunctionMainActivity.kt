package z.app.allever.android.sample.function

import androidx.fragment.app.Fragment
import app.allever.android.lib.common.SampleMainActivity
import app.allever.android.lib.common.databinding.ActivitySampleMainBinding
import app.allever.android.lib.core.app.App
import app.allever.android.lib.imageloader.core.ImageLoaderCore
import app.allever.android.lib.imageloader.engine.glide.GlideLoader
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.lib.router.annotation.Route
import com.vanniktech.emoji.EmojiManager
import skin.support.SkinCompatManager
import skin.support.app.SkinAppCompatViewInflater
import skin.support.app.SkinCardViewInflater
import skin.support.constraint.app.SkinConstraintViewInflater
import skin.support.design.app.SkinMaterialViewInflater
import z.app.allever.android.lib.widget.Widget
import z.app.allever.android.sample.function.im.function.MyEmojiProvider

@Route(path = "/zfunction/main")
class FunctionMainActivity: SampleMainActivity<ActivitySampleMainBinding, BaseViewModel>() {

    override fun init() {
        super.init()
        Widget.init(applicationContext)
        EmojiManager.install(MyEmojiProvider())
        ImageLoaderCore.init(applicationContext, GlideLoader, ImageLoaderCore.Builder.create())
        initSkin()
    }
    override fun getSampleName(): String  = "功能实现"

    override fun getSampleFragment(): Fragment  = FunctionMainFragment()

    private fun initSkin() {
        SkinCompatManager.withoutActivity(App.app)
            .addInflater(SkinAppCompatViewInflater()) // 基础控件换肤初始化
            .addInflater(SkinMaterialViewInflater()) // material design 控件换肤初始化[可选]
            .addInflater(SkinConstraintViewInflater()) // ConstraintLayout 控件换肤初始化[可选]
            .addInflater(SkinCardViewInflater()) // CardView v7 控件换肤初始化[可选]
            .setSkinStatusBarColorEnable(false) // 关闭状态栏换肤，默认打开[可选]
            .setSkinWindowBackgroundEnable(false) // 关闭windowBackground换肤，默认打开[可选]
            .loadSkin()
    }
}