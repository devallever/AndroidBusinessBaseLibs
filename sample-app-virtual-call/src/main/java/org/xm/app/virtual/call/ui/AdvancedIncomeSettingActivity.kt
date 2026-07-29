package org.xm.app.virtual.call.ui

import android.app.Activity
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import com.allever.app.virtual.call.R
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.util.FileUtils
import com.bumptech.glide.Glide
import org.xm.app.virtual.call.app.BaseActivity
import org.xm.app.virtual.call.function.SettingHelper
import org.xm.app.virtual.call.ui.mvp.presenter.AdvancedIncomeSettingPresenter
import org.xm.app.virtual.call.ui.mvp.view.AdvancedIncomeSettingView
import org.xm.app.virtual.call.util.SystemUtils
import java.io.File

class AdvancedIncomeSettingActivity :
    BaseActivity<AdvancedIncomeSettingView, AdvancedIncomeSettingPresenter>(),
    AdvancedIncomeSettingView,
    View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private lateinit var mTvRingtoneTitle: TextView
    private lateinit var mTvWallPagerTitle: TextView
    private lateinit var mSwitchVibrator: SwitchCompat
    private lateinit var mSwitchRepeat: SwitchCompat
    private lateinit var mSwitchRadomContact: SwitchCompat
    private lateinit var mRepeatIntervalContainer: ViewGroup
    private lateinit var mRepeatCountContainer: ViewGroup
    private lateinit var mEtRepeatInterval: EditText
    private lateinit var mEtRepeatCount: EditText
    private lateinit var mTvAvatarPath: TextView
    private lateinit var mIvAvatar: ImageView

    private lateinit var mTempPath: String

    override fun getContentView(): Any = R.layout.vc_activity_advanced_income_setting

    override fun initView() {
        //判断是否有刘海屏幕
        checkNotch(Runnable {
            val rootLayout = findViewById<ViewGroup>(R.id.rootLayout)
            val statusBarViewId = addStatusBar(rootLayout)
            if (rootLayout is RelativeLayout) {
                val topBar = findViewById<View>(R.id.top_bar).layoutParams as? RelativeLayout.LayoutParams
                topBar?.addRule(RelativeLayout.BELOW, statusBarViewId.id)
            }
        })

        mTempPath = "$cacheDir${File.separator}temp.jpg"

        findViewById<View>(R.id.iv_left).setOnClickListener(this)
        findViewById<TextView>(R.id.tv_label).text = getString(R.string.vc_advanced_setting)
        mTvRingtoneTitle = findViewById(R.id.setting_item_tv_ringtone)
        mTvRingtoneTitle.setOnClickListener(this)
        mTvWallPagerTitle = findViewById(R.id.setting_item_tv_background)
        mTvWallPagerTitle.setOnClickListener(this)
        mSwitchVibrator = findViewById(R.id.setting_item_switch_vibrator)
        mSwitchVibrator.isChecked = SettingHelper.getVibrator()
        mRepeatCountContainer = findViewById(R.id.setting_item_repeat_count)
        mRepeatIntervalContainer = findViewById(R.id.setting_item_repeat_interval)
        mSwitchRepeat = findViewById(R.id.setting_item_switch_repeat)

        mSwitchRepeat.isChecked = SettingHelper.getRepeat()
        showRepeatSetting(mSwitchRepeat.isChecked)
        mSwitchRadomContact = findViewById(R.id.setting_item_switch_redom_contact)

        mSwitchRadomContact.isChecked = SettingHelper.getRandomContact()

        mEtRepeatInterval = findViewById(R.id.setting_item_et_repeat_interval)
        mEtRepeatInterval.setText(SettingHelper.getRepeatInterval().toString())
        mEtRepeatCount = findViewById(R.id.setting_item_et_repeat_count)
        mEtRepeatCount.setText(SettingHelper.getRepeatCount().toString())

        findViewById<View>(R.id.tvResetAvatar).setOnClickListener(this)
        findViewById<View>(R.id.setting_item_avatar).setOnClickListener(this)
        mTvAvatarPath = findViewById(R.id.tvAvatarPath)

        mIvAvatar = findViewById(R.id.ivAvatar)

        mSwitchRadomContact.setOnCheckedChangeListener(this)
        mSwitchRepeat.setOnCheckedChangeListener(this)

    }

    override fun initData() {
    }

    override fun createPresenter(): AdvancedIncomeSettingPresenter =
        AdvancedIncomeSettingPresenter()

    override fun onResume() {
        super.onResume()
        mTvRingtoneTitle.text = SettingHelper.getRingtoneTitle()
        mTvWallPagerTitle.text = SettingHelper.getWallPagerTitle()
        mTvAvatarPath.text = SettingHelper.getAvatarPath()
        loadAvatar()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        SettingHelper.setVibrator(mSwitchVibrator.isChecked)
        SettingHelper.setRepeat(mSwitchRepeat.isChecked)
        SettingHelper.setRepeatInterval(
            mEtRepeatInterval.text.toString().toIntOrNull() ?: SettingHelper.getRepeatInterval()
        )
        SettingHelper.setRepeatCount(
            mEtRepeatCount.text.toString().toIntOrNull() ?: SettingHelper.getRepeatCount()
        )
        SettingHelper.setRandomContact(mSwitchRadomContact.isChecked)
        SettingHelper.setAvatarPath(mTvAvatarPath.text.toString())

        super.onDestroy()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_left -> {
                finish()
            }
            R.id.setting_item_tv_ringtone -> {
                RingtonePickerActivity.start(this)
            }
            R.id.setting_item_tv_background -> {
                WallPagerPickerActivity.start(this)
            }
            R.id.setting_item_avatar -> {
                SystemUtils.chooseImageFromGallery(
                    this,
                    RC_PICK_IMAGE
                )
            }
            R.id.tvResetAvatar -> {
                mTvAvatarPath.text = ""
                SettingHelper.setAvatarPath("")
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
                    mTvAvatarPath.text = filePath
                    SettingHelper.setAvatarPath(filePath)
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
                    mSwitchRadomContact.isChecked = false
                }
            }
            R.id.setting_item_switch_redom_contact -> {
                if (isChecked) {
                    mSwitchRepeat.isChecked = false
                }
            }
        }
    }

    private fun showRepeatSetting(show: Boolean) {
        if (show) {
            mRepeatCountContainer.visibility = View.VISIBLE
            mRepeatIntervalContainer.visibility = View.VISIBLE
        } else {
            mRepeatCountContainer.visibility = View.GONE
            mRepeatIntervalContainer.visibility = View.GONE
        }
    }

    private fun loadAvatar() {
        val filePath = SettingHelper.getAvatarPath()
        if (filePath.isNotEmpty()) {
            Glide.with(App.context).load(filePath).into(mIvAvatar)
        } else {
            mIvAvatar.setImageResource(R.drawable.vc_ic_contact)
        }
    }
    

    companion object {
        private const val RC_PICK_IMAGE = 0x01
        fun start(context: Activity) {
            val intent = Intent(context, AdvancedIncomeSettingActivity::class.java)
            context.startActivityForResult(intent, 0)
        }
    }
}