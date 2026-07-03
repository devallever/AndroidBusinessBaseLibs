package org.xm.app.virtual.call.ui

import android.Manifest
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.ContactsContract
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.allever.app.virtual.call.R
import com.allever.lib.ad.chain.AdChainHelper
import com.allever.lib.ad.chain.AdChainListener
import com.allever.lib.ad.chain.IAd
import com.allever.lib.common.app.App
import com.allever.lib.common.util.ActivityCollector
import com.allever.lib.common.util.Tool
import com.allever.lib.common.util.toast
import com.allever.lib.permission.PermissionManager
import com.allever.lib.ui.widget.ShakeHelper
import com.allever.lib.umeng.UMeng
import org.xm.app.virtual.call.ad.AdContract
import org.xm.app.virtual.call.app.BaseFragment
import org.xm.app.virtual.call.app.Global
import org.xm.app.virtual.call.function.SettingHelper
import org.xm.app.virtual.call.service.VirtualCallService
import org.xm.app.virtual.call.ui.mvp.presenter.MainPresenter
import org.xm.app.virtual.call.ui.mvp.view.MainView


class MainFragment : BaseFragment<MainView, MainPresenter>(),
    MainView, View.OnClickListener {
    private lateinit var mEtTime: EditText
    private lateinit var mEtPhone: EditText
    private lateinit var mEtContact: EditText
    private lateinit var mEtLocal: EditText
    private lateinit var mTvChooseContact: TextView

    private var mShakeAnimator: ObjectAnimator? = null
    private lateinit var mIvRecommend: View


    private var mBannerAd: IAd? = null
    override fun getContentView(): Int = R.layout.fragment_main

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

        mBannerContainer = root.findViewById<ViewGroup>(R.id.banner_container)
        mNativeAdContainer = root.findViewById(R.id.nativeAdContainer)

        mIvRecommend = root.findViewById(R.id.ivRecommend)
        if (UMeng.getChannel().equals("google", true)) {
            mIvRecommend.visibility = View.VISIBLE
            mShakeAnimator = ShakeHelper.createShakeAnimator(mIvRecommend, true)
            mShakeAnimator?.start()
            mIvRecommend.setOnClickListener(this)
        } else {
            mIvRecommend.visibility = View.GONE
        }

    }

    override fun initData() {
        loadBanner()
    }

    override fun createPresenter(): MainPresenter = MainPresenter()

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.setting_btn_start -> {
                saveIncomeSetting()
                Global.leftRepeatCount = SettingHelper.getRepeatCount()
                VirtualCallService.start(App.context)
                ActivityCollector.finishAll()
            }
            R.id.setting_item_advanced -> {
                AdvancedIncomeSettingActivity.start(
                    activity!!
                )
            }
            R.id.tvChooseContact -> {
                if (PermissionManager.hasPermissions(Manifest.permission.READ_CONTACTS)) {
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
                } else {
                    if (PermissionManager.alwaysDenyPermissions(
                            activity!!,
                            Manifest.permission.READ_CONTACTS
                        )
                    ) {
                        PermissionManager.jumpPermissionSetting(activity, 0,
                            DialogInterface.OnClickListener { dialog, which ->
                                toast("请手动设置权限")
                            })
                    } else {
                        requestAdPermission(activity!!, getString(R.string.permission_tips))
                    }

                }
            }
            R.id.ivRecommend -> {
                val url =
                    "https://play.google.com/store/apps/details?id=com.allever.app.virtual.call.girlfriend"
                Tool.openInGooglePlay(App.context, url)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mPresenter.getContacts(data ?: return)
    }


    override fun onResume() {
        super.onResume()
        mBannerAd?.onAdResume()
    }

    override fun onPause() {
        super.onPause()
        mBannerAd?.onAdPause()
    }

    override fun onDestroy() {
        saveIncomeSetting()
        mBannerAd?.destroy()
        mNativeAd?.destroy()
        mShakeAnimator?.cancel()
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

    fun requestAdPermission(activity: Activity, msg: String) {
        // 如果api >= 23 需要显式申请权限
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_PHONE_STATE
            ) !== PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) !== PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_CONTACTS
            ) !== PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.INTERNET
            ) !== PackageManager.PERMISSION_GRANTED
        ) {

            AlertDialog.Builder(activity)
                .setMessage(msg)
                .setPositiveButton("同意") { dialog, which ->
                    dialog.dismiss()
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.READ_CONTACTS
                        ),
                        0
                    )
                }
                .setNegativeButton("拒绝") { dialog, which ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }
    }


    private lateinit var mNativeAdContainer: ViewGroup
    private var mNativeAd: IAd? = null
    private fun loadNativeAd() {
        AdChainHelper.loadAd(
            AdContract.AD_NAME_COMMON_NATIVE_SMALL,
            mNativeAdContainer,
            object : AdChainListener {
                override fun onLoaded(ad: IAd?) {
                    mNativeAd = ad
                    mNativeAd?.show()
                }

                override fun onFailed(msg: String) {
                    loadBanner()
                }

                override fun onShowed() {
                }

                override fun onDismiss() {
                }

            })

    }

    private lateinit var mBannerContainer: ViewGroup
    private fun loadBanner() {
        AdChainHelper.loadAd(
            AdContract.AD_NAME_MAIN_BANNER,
            mBannerContainer,
            object : AdChainListener {
                override fun onLoaded(ad: IAd?) {
                    mBannerAd = ad
                }

                override fun onFailed(msg: String) {}
                override fun onShowed() {}
                override fun onDismiss() {}

            })
    }

}