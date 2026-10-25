package app.funnyappptech.android.fake.call.ui

import androidx.activity.OnBackPressedCallback
import app.funnyappptech.android.fake.call.R
import app.funnyappptech.android.fake.call.base.BasePage
import app.funnyappptech.android.fake.call.base.BaseViewModel
import app.funnyappptech.android.fake.call.databinding.VcActivityHomeBinding
import app.funnyappptech.android.fake.call.ui.dialog.DialogHelper

class HomePage : BasePage<VcActivityHomeBinding, BaseViewModel>() {

    override fun onBackPressed() {
        checkExit()
    }

    override fun inflate(): VcActivityHomeBinding {
        return VcActivityHomeBinding.inflate(layoutInflater)
    }

    override fun init() {
        DialogHelper.createGuideDialog(this)

        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, MainFragment())
            .commit()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                checkExit()
            }
        })
    }
}