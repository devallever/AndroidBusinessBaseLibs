package app.android.gp.ai.translator.ui

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import app.android.gp.ai.translator.R
import app.android.gp.ai.translator.app.AppMvpFragment
import app.android.gp.ai.translator.databinding.FSettingBinding
import app.android.gp.ai.translator.event.DefaultTranslateLangChangedEventB
import app.android.gp.ai.translator.language.Lang
import app.android.gp.ai.translator.ui.adapter.item.SelectEngineItem
import app.android.gp.ai.translator.ui.adapter.item.SelectLangItem
import app.android.gp.ai.translator.function.SettingHelper
import app.android.gp.ai.translator.translate.TranslationHelper
import app.android.gp.ai.translator.translate.EngineType
import app.android.gp.ai.translator.ui.dialog.DialogHelper
import app.android.gp.ai.translator.ui.mvp.presenter.SettingPresenter
import app.android.gp.ai.translator.ui.mvp.view.SettingView
import app.android.gp.ai.translator.util.CommonHelper
import app.woejt.wwzdndgl.lib.app.App
import app.woejt.wwzdndgl.lib.util.ActivityCollector
import app.woejt.wwzdndgl.lib.util.ShareHelper
import app.woejt.wwzdndgl.lib.util.toast
import com.allever.android.lib.admob.AdManager
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SettingFragmentPage : AppMvpFragment<SettingView, SettingPresenter>(), SettingView,
    View.OnClickListener {
    private lateinit var mTvDefaultTranslateLang: TextView
    private lateinit var mTvDefaultTranslateEngine: TextView
    private var mLangDialog: AlertDialog? = null
    private var mEngineDialog: AlertDialog? = null

    private lateinit var mBinding: FSettingBinding

    override fun getContentView(): View {
        mBinding = FSettingBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun initView(root: View) {
        EventBus.getDefault().register(this)
        root.findViewById<View>(R.id.setting_tv_share).setOnClickListener(this)
//        root.findViewById<TextView>(R.id.setting_tv_feedback).setOnClickListener(this)
        root.findViewById<TextView>(R.id.setting_tv_about).setOnClickListener(this)
//        root.findViewById<TextView>(R.id.setting_tv_permission).setOnClickListener(this)
        root.findViewById<TextView>(R.id.setting_tv_guide).setOnClickListener(this)
        root.findViewById<View>(R.id.itemDefaultTranslateLang).setOnClickListener(this)
        root.findViewById<View>(R.id.itemDefaultTranslateEngine).setOnClickListener(this)
//        root.findViewById<View>(R.id.itemBackupRestore).setOnClickListener(this)

        val switchAutoPlayAudio = root.findViewById<SwitchCompat>(R.id.switchAutoPlayAudio)
        switchAutoPlayAudio.isChecked = SettingHelper.getAutoPlayAudio()
        switchAutoPlayAudio.setOnCheckedChangeListener { buttonView, isChecked ->
            SettingHelper.setAutoPlayAudioSwitch(isChecked)
        }

        val switchCopyToClipBoard = root.findViewById<SwitchCompat>(R.id.switchCopyToClipboard)
        switchCopyToClipBoard.isChecked = SettingHelper.getAutoPlayAudio()
        switchCopyToClipBoard.setOnCheckedChangeListener { buttonView, isChecked ->
            SettingHelper.setCopyClipBoardSwitch(isChecked)
        }

        val switchTranslate = root.findViewById<SwitchCompat>(R.id.switchAutoTranslate)
        switchTranslate.isChecked = SettingHelper.getAutoTranslate()
        switchTranslate.setOnCheckedChangeListener { buttonView, isChecked ->
            SettingHelper.setAutoTranslateSwitch(isChecked)
            if (CommonHelper.isAboveAndrodQ()) {
                toast("Your version of Android does not support this feature because Android 10 has disabled access to the clipboard.")
            }
        }

        val switchForegroundService =
            root.findViewById<SwitchCompat>(R.id.switchForegroundService)
        switchForegroundService.isChecked = SettingHelper.getForegroundServiceSwitch()
        switchForegroundService.setOnCheckedChangeListener { buttonView, isChecked ->
            SettingHelper.setForegroundServiceSwitch(isChecked)
        }

        mTvDefaultTranslateLang = root.findViewById(R.id.tvDefaultTranslateLang)
        mTvDefaultTranslateLang.text = SettingHelper.getDefaultTranslateLangKey()
        mLangDialog = DialogHelper.createSelectLangDialog(
            activity,
            1,
            object : DialogHelper.SelectLangListener {
                override fun onItemSelected(alertDialog: AlertDialog?, data: SelectLangItem) {
                    alertDialog?.dismiss()
                    mTvDefaultTranslateLang.text = data.lang?.KEY
                    SettingHelper.setDefaultTranslateLang(data.lang?.KEY ?: Lang.CHINESE.KEY)
                }
            })
        mTvDefaultTranslateEngine = root.findViewById(R.id.tvDefaultTranslateEngine)
        mTvDefaultTranslateEngine.text =
            EngineType.getEngineName(SettingHelper.getDefaultTranslateEngine())
        mEngineDialog = DialogHelper.createSelectEngineDialog(
            activity,
            object : DialogHelper.SelectEngineListener {
                override fun onItemSelected(alertDialog: AlertDialog?, data: SelectEngineItem) {
                    alertDialog?.dismiss()
                    mTvDefaultTranslateEngine.text = EngineType.getEngineName(data.value ?: 1)
                    SettingHelper.setDefaultTranslateEngine(data.value ?: 1)
                    TranslationHelper.init(App.context)
                }
            })

        AdManager.loadNativeAd(mBinding.bannerContainer, "setting")
    }

    override fun initData() {
    }

    override fun createPresenter(): SettingPresenter = SettingPresenter()

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.setting_tv_guide -> {
                ActivityCollector.startActivity(requireActivity(), GuidePage::class.java)
            }
//            R.id.setting_tv_permission -> {
//            }
            R.id.setting_tv_share -> {
                val msg = getString(
                    R.string.share_content,
                    getString(R.string.app_name),
                    App.context.packageName
                )
                ShareHelper.shareText(this, msg)
            }
//            R.id.setting_tv_feedback -> {
//                FeedbackHelper.feedback(activity)
//            }
            R.id.setting_tv_about -> {
                AboutPage.start(requireActivity())
            }
            R.id.itemDefaultTranslateLang -> {
                mLangDialog?.show()
            }
            R.id.itemDefaultTranslateEngine -> {
                mEngineDialog?.show()
            }
//            R.id.itemBackupRestore -> {
//                BackupRestoreActivity.start(requireActivity())
//            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDefaultTranslateLangChanged(defaultTranslateLangChangedEvent: DefaultTranslateLangChangedEventB) {
        mTvDefaultTranslateLang.text = SettingHelper.getDefaultTranslateLangKey()
    }

    companion object {
        fun newInstance(): SettingFragmentPage = SettingFragmentPage()
    }

}