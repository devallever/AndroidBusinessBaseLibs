package app.allever.android.lib.common

import app.allever.android.lib.common.databinding.EmptyPageBinding
import app.allever.android.lib.core.base.AbstractBindingActivity

class FullScreenActivity : AbstractBindingActivity<EmptyPageBinding>() {
    override fun init() {
        adaptStatusBar(mBinding.btnTopView)
    }

    override fun inflate() = EmptyPageBinding.inflate(layoutInflater)
}