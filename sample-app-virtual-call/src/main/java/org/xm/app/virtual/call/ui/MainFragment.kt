package org.xm.app.virtual.call.ui

import android.content.Intent
import android.provider.ContactsContract
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.allever.app.virtual.call.R
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.core.helper.ActivityHelper
import org.xm.app.virtual.call.app.BaseFragment
import org.xm.app.virtual.call.app.Global
import org.xm.app.virtual.call.function.SettingHelper
import org.xm.app.virtual.call.service.VirtualCallService
import org.xm.app.virtual.call.ui.mvp.presenter.MainPresenter
import org.xm.app.virtual.call.ui.mvp.view.MainView
import org.xm.app.virtual.call.util.SystemUtils


class MainFragment : BaseFragment<MainView, MainPresenter>(),
    MainView, View.OnClickListener {
    private lateinit var mEtTime: EditText
    private lateinit var mEtPhone: EditText
    private lateinit var mEtContact: EditText
    private lateinit var mEtLocal: EditText
    private lateinit var mTvChooseContact: TextView

    private lateinit var mIvRecommend: View


    override fun getContentView(): Int = R.layout.vc_fragment_main

    override fun initView(root: View) {
        root.findViewById<View>(R.id.setting_btn_start).setOnClickListener(this)
        val tvAdvanceSetting = root.findViewById<TextView>(R.id.setting_item_advanced)
        tvAdvanceSetting.setOnClickListener(this)
        tvAdvanceSetting.setText(
            "${getString(R.string.ringtone)}/${getString(R.string.vibrate)}/${getString(R.string.wallpaper)}/${getString(
                R.string.repeat
            )}"
        )
        mEtTime = root.findViewById(R.id.setting_item_et_time)
        mEtPhone = root.findViewById(R.id.setting_item_et_phone)
        mEtContact = root.findViewById(R.id.setting_item_et_contact)
        mEtLocal = root.findViewById(R.id.setting_item_et_local)

        mEtTime.setText(SettingHelper.getTime().toString())
        mEtPhone.setText(SettingHelper.getPhone())
        mEtContact.setText(SettingHelper.getContact())
        mEtLocal.setText(SettingHelper.getLocal())
        root.findViewById<View>(R.id.tvChooseContact).setOnClickListener(this)


        mIvRecommend = root.findViewById(R.id.ivRecommend)

        mIvRecommend.visibility = View.VISIBLE
        mIvRecommend.setOnClickListener(this)
    }

    override fun initData() {
    }

    override fun createPresenter(): MainPresenter = MainPresenter()

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.setting_btn_start -> {
                saveIncomeSetting()
                Global.leftRepeatCount = SettingHelper.getRepeatCount()
                VirtualCallService.start(App.context)
                ActivityHelper.finishAll()
            }
            R.id.setting_item_advanced -> {
                AdvancedIncomeSettingActivity.start(
                    requireActivity()
                )
            }
            R.id.tvChooseContact -> {
                try {
                    val intent =
                        Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
                    startActivityForResult(
                        intent,
                        PICK_CONTACT
                    )
                } catch (e: Exception) {
                    toast("没有找到联系人的软件，请手动输入")
                    e.printStackTrace()
                }
            }
            R.id.ivRecommend -> {
                val url =
                    "https://play.google.com/store/apps/details?id=com.allever.app.virtual.call.girlfriend"
                SystemUtils.openUrl(requireActivity(), url)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mPresenter.getContacts(data ?: return)
    }

    override fun onDestroy() {
        saveIncomeSetting()
        super.onDestroy()
    }

    private fun saveIncomeSetting() {
        SettingHelper.setTime(mEtTime.text.toString().toIntOrNull() ?: SettingHelper.getTime() ?: 5)
        SettingHelper.setPhone(mEtPhone.text.toString())
        SettingHelper.setContact(mEtContact.text.toString())
        SettingHelper.setLocal(mEtLocal.text.toString())
    }

    override fun updateContact(name: String) {
        mEtContact.setText(name)
    }

    override fun updatePhone(phone: String) {
        mEtPhone.setText(phone)
    }

    companion object {
        private const val PICK_CONTACT = 0X01
    }

}