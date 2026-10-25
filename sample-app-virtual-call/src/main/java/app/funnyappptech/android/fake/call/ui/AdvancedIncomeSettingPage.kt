package app.funnyappptech.android.fake.call.ui

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.*
import app.funnyappptech.android.fake.call.R
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.util.FileUtils
import app.funnyappptech.android.fake.call.base.BasePage
import app.funnyappptech.android.fake.call.base.BaseViewModel
import app.funnyappptech.android.fake.call.databinding.VcActivityAdvancedIncomeSettingBinding
import app.funnyappptech.android.fake.call.function.SettingStore
import app.funnyappptech.android.fake.call.util.SystemUtils
import coil.load

class AdvancedIncomeSettingPage :
    BasePage<VcActivityAdvancedIncomeSettingBinding, BaseViewModel>(),
    View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    override fun onResume() {
        super.onResume()
        mBinding.settingItemTvRingtone.text = SettingStore.getRingtoneTitle()
        mBinding.settingItemTvBackground.text = SettingStore.getWallPagerTitle()
        mBinding.tvAvatarPath.text = SettingStore.getAvatarPath()
        loadAvatar()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        SettingStore.setVibrator(mBinding.settingItemSwitchVibrator.isChecked)
        SettingStore.setRepeat(mBinding.settingItemSwitchRepeat.isChecked)
        SettingStore.setRepeatInterval(
            mBinding.settingItemEtRepeatInterval.text.toString().toIntOrNull() ?: SettingStore.getRepeatInterval()
        )
        SettingStore.setRepeatCount(
            mBinding.settingItemEtRepeatCount.text.toString().toIntOrNull() ?: SettingStore.getRepeatCount()
        )
        SettingStore.setRandomContact(mBinding.settingItemSwitchRedomContact.isChecked)
        SettingStore.setAvatarPath(mBinding.tvAvatarPath.text.toString())

        super.onDestroy()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_left -> {
                finish()
            }
            R.id.setting_item_tv_ringtone -> {
                RingtonePickerPage.start(this)
            }
            R.id.setting_item_tv_background -> {
                WallPagerPickerPage.start(this)
            }
            R.id.setting_item_avatar -> {
                SystemUtils.chooseImageFromGallery(
                    this,
                    RC_PICK_IMAGE
                )
            }
            R.id.tvResetAvatar -> {
                mBinding.tvAvatarPath.text = ""
                SettingStore.setAvatarPath("")
                loadAvatar()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                RC_PICK_IMAGE -> {
                    val filePath = FileUtils.getFilePathByUri(this, data?.data) ?: ""
                    log("url to file = $filePath")
                    mBinding.tvAvatarPath.text = filePath
                    SettingStore.setAvatarPath(filePath)
                    loadAvatar()
                }
                else -> {
                }
            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        when (buttonView?.id) {
            R.id.setting_item_switch_repeat -> {
                showRepeatSetting(isChecked)
                if (isChecked) {
                    mBinding.settingItemSwitchRedomContact.isChecked = false
                }
            }
            R.id.setting_item_switch_redom_contact -> {
                if (isChecked) {
                    mBinding.settingItemSwitchRepeat.isChecked = false
                }
            }
        }
    }

    private fun showRepeatSetting(show: Boolean) {
        if (show) {
            mBinding.settingItemRepeatCount.visibility = View.VISIBLE
            mBinding.settingItemRepeatInterval.visibility = View.VISIBLE
        } else {
            mBinding.settingItemRepeatCount.visibility = View.GONE
            mBinding.settingItemRepeatInterval.visibility = View.GONE
        }
    }

    private fun loadAvatar() {
        val filePath = SettingStore.getAvatarPath()
        if (filePath.isNotEmpty()) {
            mBinding.ivAvatar.load(filePath)
        } else {
            mBinding.ivAvatar.setImageResource(R.drawable.vc_ic_contact)
        }
    }

    override fun inflate(): VcActivityAdvancedIncomeSettingBinding {
        return VcActivityAdvancedIncomeSettingBinding.inflate(layoutInflater)
    }

    override fun init() {
        findViewById<View>(R.id.iv_left).setOnClickListener(this)
        findViewById<TextView>(R.id.tv_label).text = getString(R.string.vc_advanced_setting)

        mBinding.settingItemTvRingtone.setOnClickListener(this)
        mBinding.settingItemTvBackground.setOnClickListener(this)
        mBinding.settingItemSwitchVibrator.isChecked = SettingStore.getVibrator()

        mBinding.settingItemSwitchRepeat.isChecked = SettingStore.getRepeat()
        showRepeatSetting(mBinding.settingItemSwitchRepeat.isChecked)

        mBinding.settingItemSwitchRedomContact.isChecked = SettingStore.getRandomContact()

        mBinding.settingItemEtRepeatInterval.setText(SettingStore.getRepeatInterval().toString())
        mBinding.settingItemEtRepeatCount.setText(SettingStore.getRepeatCount().toString())

        mBinding.tvResetAvatar.setOnClickListener(this)
        mBinding.settingItemAvatar.setOnClickListener(this)

        mBinding.settingItemSwitchRedomContact.setOnCheckedChangeListener(this)
        mBinding.settingItemSwitchRepeat.setOnCheckedChangeListener(this)
    }


    companion object {
        private const val RC_PICK_IMAGE = 0x01
        fun start(context: Activity) {
            val intent = Intent(context, AdvancedIncomeSettingPage::class.java)
            context.startActivityForResult(intent, 0)
        }
    }
}