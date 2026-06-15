package com.example.charge.ui.dialog

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.ImageView
import androidx.core.net.toUri
import com.example.charge.R
import com.example.charge.databinding.DialogSettingsBinding
import com.example.charge.init.Constance
import com.example.charge.ui.activity.WebActivity
import com.example.charge.utils.AppLanguage
import com.example.charge.utils.LocaleManager
import com.example.charge.utils.MusicUtil
import com.example.charge.utils.SoundUtil
import com.example.charge.utils.isAppInstalled
import com.example.charge.utils.openBrowser
import com.example.charge.utils.setOnSingleListener
import com.example.charge.utils.setVisible
import com.lxj.xpopup.core.CenterPopupView

class SettingsDialog(val mActivity: Activity) : CenterPopupView(mActivity) {

    private val binding by lazy { DialogSettingsBinding.bind(this.contentView) }
    override fun getImplLayoutId(): Int {
        return R.layout.dialog_settings
    }

    override fun onCreate() {
        super.onCreate()

        binding.apply {
            closeImg.setOnSingleListener {
                dismiss()
            }
            termsTv.setOnSingleListener {
                sendEmail()
            }
            privacyTv.setOnSingleListener {
                WebActivity.start(context, Constance.PRIVACY_URL)
            }
            val language = LocaleManager.getSavedLanguage(mActivity)

            englishImg.setImageResource(if (language == AppLanguage.EN) R.drawable.ic_select else R.drawable.ic_unselect)
            brazilImg.setImageResource(if (language == AppLanguage.PT_BR) R.drawable.ic_select else R.drawable.ic_unselect)

            englishLL.setOnSingleListener {
                LocaleManager.setToEnglish(mActivity)
            }
            barsilLL.setOnSingleListener {
                LocaleManager.setToBrazilianPortuguese(mActivity)
            }
        }

        initView()
    }

    fun initView() {
        val musicOpen = MusicUtil.isOpenMusic
        val soundOpen = SoundUtil.isOpenSound
        binding.apply {
            musicOffImg.setVisible(!musicOpen)
            musicImg.setOnSingleListener {
                MusicUtil.isOpenMusic = !musicOpen
                /*                if (MusicUtil.isOpenMusic){
                                    MusicUtil.play()
                                }else{
                                    MusicUtil.pause()
                                }*/
                initView()
            }
            voiceOffImg.setVisible(!soundOpen)
            voiceImg.setOnSingleListener {
                SoundUtil.isOpenSound = !soundOpen
                initView()
            }
        }
    }

    private fun sendEmail() {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                data = "mailto:".toUri()
                putExtra(Intent.EXTRA_EMAIL, arrayOf<String>(Constance.EMAIL))
                if (context !is Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (isAppInstalled(context, "com.google.android.gm")) {
                    setPackage("com.google.android.gm")
                }
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            openBrowser(context, "https://mail.google.com")
        }
    }

}