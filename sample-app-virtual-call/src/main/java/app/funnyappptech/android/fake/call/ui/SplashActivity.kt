package app.funnyappptech.android.fake.call.ui

import android.os.Bundle
import app.allever.android.lib.core.base.AbstractActivity
import app.allever.android.lib.core.function.notchcompat.NotchCompat
import app.allever.android.lib.core.helper.ActivityHelper
import app.funnyappptech.android.fake.call.R
import app.funnyappptech.android.fake.call.app.Global
import app.funnyappptech.android.fake.call.bean.ContactBean
import app.funnyappptech.android.fake.call.function.SettingHelper
import app.funnyappptech.android.fake.call.util.SystemUtils

class SplashActivity : AbstractActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.vc_activity_splash)

        // 全屏显示并适配
        NotchCompat.adaptNotchWithFullScreen(window)


        mHandler.postDelayed({
            gotoMain()
        }, 1000)


        if (true) {
            SystemUtils.getContactList()
        } else {
            Global.contactList.clear()
            val contact = ContactBean()
            contact.name = SettingHelper.getContact() ?: "张三"
            contact.phone = SettingHelper.getPhone() ?: "13800138000"
            Global.contactList.add(contact)
        }
    }

    override fun onBackPressed() {

    }

    private fun gotoMain() {
        ActivityHelper.startActivity<HomeActivity>()
        finish()
    }
}