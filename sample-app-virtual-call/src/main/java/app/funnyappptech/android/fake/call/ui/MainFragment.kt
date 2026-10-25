package app.funnyappptech.android.fake.call.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.provider.ContactsContract
import android.view.View
import app.funnyappptech.android.fake.call.R
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.core.helper.ActivityHelper
import app.funnyappptech.android.fake.call.base.BaseFragment
import app.funnyappptech.android.fake.call.app.FakeCall
import app.funnyappptech.android.fake.call.databinding.VcFragmentMainBinding
import app.funnyappptech.android.fake.call.function.SettingStore
import app.funnyappptech.android.fake.call.service.VirtualCallService
import app.funnyappptech.android.fake.call.viewmodel.MainViewModel


class MainFragment : BaseFragment<VcFragmentMainBinding, MainViewModel>(),
    View.OnClickListener {

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.setting_btn_start -> {
                saveIncomeSetting()
                FakeCall.leftRepeatCount = SettingStore.getRepeatCount()
                VirtualCallService.start(App.context)
                ActivityHelper.finishAll()
            }
            R.id.setting_item_advanced -> {
                AdvancedIncomeSettingPage.start(
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
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mViewModel.getContacts(data ?: return)
    }

    override fun onDestroy() {
        saveIncomeSetting()
        super.onDestroy()
    }

    private fun saveIncomeSetting() {
        SettingStore.setTime(mBinding.settingItemEtTime.text.toString().toIntOrNull() ?: SettingStore.getTime() ?: 5)
        SettingStore.setPhone(mBinding.settingItemEtPhone.text.toString())
        SettingStore.setContact(mBinding.settingItemEtContact.text.toString())
        SettingStore.setLocal(mBinding.settingItemEtLocal.text.toString())
    }
    override fun inflate(): VcFragmentMainBinding {
        return VcFragmentMainBinding.inflate(layoutInflater)
    }

    @SuppressLint("SetTextI18n")
    override fun init() {
        adaptStatusBar(mBinding.contentContainer)
        mBinding.settingBtnStart.setOnClickListener(this)
        mBinding.settingItemAdvanced.setOnClickListener(this)
        mBinding.settingItemAdvanced.text = "${getString(R.string.ringtone)}/${getString(R.string.vibrate)}/${getString(R.string.wallpaper)}/${getString(
            R.string.repeat
        )}"

        mBinding.settingItemEtTime.setText(SettingStore.getTime().toString())
        mBinding.settingItemEtPhone.setText(SettingStore.getPhone())
        mBinding.settingItemEtContact.setText(SettingStore.getContact())
        mBinding.settingItemEtLocal.setText(SettingStore.getLocal())
        mBinding.tvChooseContact.setOnClickListener(this)

        mBinding.ivSetting.setOnClickListener {
            ActivityHelper.startActivity<SettingPage>()
        }
        mBinding.ivGuide.setOnClickListener {
            ActivityHelper.startActivity<GuidePage>()
        }

        initObserver()
    }

    private fun initObserver() {
        mViewModel.nameLiveData.observe(viewLifecycleOwner) {
            mBinding.settingItemEtContact.setText(it)
        }
        mViewModel.phoneLiveData.observe(viewLifecycleOwner) {
            mBinding.settingItemEtPhone.setText(it)
        }
    }

    companion object {
        private const val PICK_CONTACT = 0X01
    }

}