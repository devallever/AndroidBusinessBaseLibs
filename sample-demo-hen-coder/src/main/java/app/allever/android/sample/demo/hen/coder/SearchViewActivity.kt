package app.allever.android.sample.demo.hen.coder

import app.allever.android.lib.core.base.AbstractBindingActivity
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.toast
import app.allever.android.sample.demo.hen.coder.databinding.HcActivitySearchViewBinding

class SearchViewActivity: AbstractBindingActivity<HcActivitySearchViewBinding>() {

    override fun inflate() = HcActivitySearchViewBinding.inflate(layoutInflater)

    override fun init() {
//        searchView.setBg(resources.getDrawable(R.drawable.search_bar_bg_2))
//        searchView.setIconColor(Color.parseColor("#ffffff"))
//        searchView.setTextColor(Color.parseColor("#ffffff"))
//        searchView.setHintTextColor(Color.parseColor("#666666"))
        adaptStatusBar(mBinding.searchView)
        mBinding.searchView.addSearchListener {
            toast(it)
            log(it)
        }
    }
}