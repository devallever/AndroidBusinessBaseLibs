package org.xm.app.virtual.call.ui

import android.Manifest
import android.os.Bundle
import com.allever.app.virtual.call.R
import com.allever.lib.common.app.BaseActivity
import com.allever.lib.common.util.ActivityCollector
import com.allever.lib.notchcompat.NotchCompat
import com.allever.lib.permission.PermissionManager
import org.xm.app.virtual.call.app.Global
import org.xm.app.virtual.call.bean.ContactBean
import org.xm.app.virtual.call.function.SettingHelper
import org.xm.app.virtual.call.util.SystemUtils

class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // 全屏显示并适配
        NotchCompat.adaptNotchWithFullScreen(window)


        mHandler.postDelayed({
            gotoMain()
        }, 1000)

        if (PermissionManager.hasPermissions(Manifest.permission.READ_CONTACTS)) {
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
        ActivityCollector.startActivity(this, HomeActivity::class.java)
        finish()
    }
}