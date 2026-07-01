package com.step.wincash.ui.dialog

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.lxj.xpopup.core.CenterPopupView
import com.lxj.xpopup.util.XPopupUtils
import com.step.wincash.R
import com.step.wincash.databinding.StDialogSettingsBinding
import com.step.wincash.init.Constance
import com.step.wincash.ui.activity.STWebActivity
import com.step.wincash.utils.MusicUtil
import com.step.wincash.utils.SoundUtil
import com.step.wincash.utils.SpKey
import com.step.wincash.utils.SpUtil
import com.step.wincash.utils.isAppInstalled
import com.step.wincash.utils.openBrowser
import com.step.wincash.utils.setOnSingleListener

class SettingsDialog(context: Context, var onDismissCallback: (() -> Unit)? = null) :
    CenterPopupView(context) {

    private lateinit var binding: StDialogSettingsBinding

    override fun getImplLayoutId(): Int {
        return R.layout.st_dialog_settings
    }

    override fun onCreate() {
        super.onCreate()
        binding = StDialogSettingsBinding.bind(this.contentView)
        initView()
        initListener()
    }


    override fun getMaxWidth(): Int {
        return XPopupUtils.getAppWidth(context)
    }


    private fun initView() {
        binding.tvVersion.text = "v${1.0}"
        binding.tvEmail.text =
            String.format(context.getString(R.string.txt_service_email), Constance.EMAIL)
        binding.cbMusic.isChecked = SpUtil.get(SpKey.IS_MUSIC_OPEN, true)
        binding.cbSound.isChecked = SpUtil.get(SpKey.IS_SOUND_OPEN, true)
    }


    private fun initListener() {

        binding.ivClose.setOnSingleListener { it ->
            dismissWith {
                onDismissCallback?.invoke()
            }
        }

        binding.feedbackContainer.setOnSingleListener {
            sendEmail()
        }

        binding.tvPolicy.setOnSingleListener {
            STWebActivity.start(context, Constance.PRIVACY_URL)
        }

        binding.cbMusic.setOnCheckedChangeListener { _, isChecked ->
            MusicUtil.isOpenMusic = isChecked
            if (isChecked) {
                MusicUtil.play()
            } else {
                MusicUtil.pause()
            }
        }

        binding.cbSound.setOnCheckedChangeListener { _, isChecked ->
            SoundUtil.isOpenSound = isChecked
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