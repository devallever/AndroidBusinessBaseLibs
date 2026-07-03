package org.xm.app.virtual.call.ui

import android.os.Bundle
import app.allever.android.lib.core.base.AbstractActivity
import app.allever.android.lib.core.function.notchcompat.NotchCompat
import app.allever.android.lib.core.helper.ActivityHelper
import com.allever.app.virtual.call.R
import org.xm.app.virtual.call.app.Global
import org.xm.app.virtual.call.bean.ContactBean
import org.xm.app.virtual.call.function.SettingHelper
import org.xm.app.virtual.call.util.SystemUtils

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