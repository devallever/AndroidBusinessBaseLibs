package app.allever.android.sample.demo.hen.coder

import app.allever.android.lib.core.base.AbstractBindingActivity
import app.allever.android.sample.demo.hen.coder.databinding.HcActivityAutoTagBinding
import java.util.ArrayList

class AutoTagActivity: AbstractBindingActivity<HcActivityAutoTagBinding>() {

    private val mAutoAdapter by  lazy {
        MyTagAdapter(this)
    }

    override fun inflate() = HcActivityAutoTagBinding.inflate(layoutInflater)

    override fun init() {
        adaptStatusBar(mBinding.autoTagLayout)
        val list = ArrayList<String>()
        list.add("1")
        list.add("22222222222222222222222222222222222222222222")
        list.add("清空万里")
        list.add("两条相交线")
        list.add("短途")
        list.add("希望在明天会更好")
        mAutoAdapter.setData(list as ArrayList<String>?)
        mBinding.autoTagLayout.setAdapter(mAutoAdapter)
    }
}