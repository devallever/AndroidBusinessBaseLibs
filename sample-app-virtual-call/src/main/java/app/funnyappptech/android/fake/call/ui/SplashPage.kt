package app.funnyappptech.android.fake.call.ui

import android.os.Bundle
import app.allever.android.lib.core.base.AbstractActivity
import app.allever.android.lib.core.function.notchcompat.NotchCompat
import app.allever.android.lib.core.helper.ActivityHelper
import app.allever.android.lib.router.annotation.Route
import app.funnyappptech.android.fake.call.R
import app.funnyappptech.android.fake.call.app.FakeCall
import app.funnyappptech.android.fake.call.data.ContactBean
import app.funnyappptech.android.fake.call.function.SettingStore

@Route(path = "/appvirtualcall/main")
class SplashPage : AbstractActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.vc_activity_splash)

        // 全屏显示并适配
        NotchCompat.adaptNotchWithFullScreen(window)


        mHandler.postDelayed({
            gotoMain()
        }, 1000)


        FakeCall.contactList.clear()
        val contact = ContactBean()
        contact.name = SettingStore.getContact() ?: "张三"
        contact.phone = SettingStore.getPhone() ?: "13800138000"
        FakeCall.contactList.add(contact)

//        if (true) {
//            SystemUtils.getContactList()
//        } else {
//            FakeCall.contactList.clear()
//            val contact = ContactBean()
//            contact.name = SettingHelper.getContact() ?: "张三"
//            contact.phone = SettingHelper.getPhone() ?: "13800138000"
//            FakeCall.contactList.add(contact)
//        }
    }

    override fun onBackPressed() {

    }

    private fun gotoMain() {
        ActivityHelper.startActivity<HomePage>()
        finish()
    }
}