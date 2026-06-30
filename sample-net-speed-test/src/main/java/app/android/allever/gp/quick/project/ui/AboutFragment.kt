package app.android.allever.gp.quick.project.ui

import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.android.allever.gp.quick.project.base.AppFragment
import app.android.allever.gp.quick.project.databinding.FragmentaAboutBinding

class AboutFragment : AppFragment<FragmentaAboutBinding, BaseViewModel>() {
    override fun inflate() = FragmentaAboutBinding.inflate(layoutInflater)

    override fun init() {
        mBinding.apply {
            tvVersion.text = "1.0"

            tvPrivacy.setOnClickListener {
                toast("Privacy")
            }
        }
    }
}