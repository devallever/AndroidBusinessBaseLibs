package app.android.gp.ai.translator.ui

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import app.android.gp.ai.translator.R
import app.android.gp.ai.translator.app.AppMvpFragment
import app.android.gp.ai.translator.app.Global
import app.android.gp.ai.translator.event.*
import app.android.gp.ai.translator.language.LanguageHelper
import app.android.gp.ai.translator.function.SettingHelper
import app.android.gp.ai.translator.bean.TranslateResult
import app.android.gp.ai.translator.databinding.FTranslationBinding
import app.android.gp.ai.translator.language.Lang
import app.android.gp.ai.translator.ui.adapter.item.SelectLangItem
import app.android.gp.ai.translator.ui.dialog.DialogHelper
import app.android.gp.ai.translator.ui.mvp.presenter.TranslationPresenter
import app.android.gp.ai.translator.ui.mvp.view.TranslationView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class TranslationFragmentPage : AppMvpFragment<TranslationView, TranslationPresenter>(), TranslationView,
    View.OnClickListener {

    private lateinit var mInputContainer: ViewGroup
    private lateinit var mEtContent: EditText

    //    private lateinit var mBtnTranslate: View
    private lateinit var mTvResult: TextView
    private lateinit var mIvChangeLanguage: ImageView
    private lateinit var mTvSrcLanguage: TextView
    private lateinit var mTvTranslateLanguage: TextView

    private lateinit var mTvSoundSrcLanguage: TextView
    private lateinit var mTvSrcText: TextView
    private lateinit var mTvSrcSymbol: TextView
    private lateinit var mIvSrcSound: ImageView
    private lateinit var mIvLiked: ImageView

    private lateinit var mTvSoundTranslateLanguage: TextView
    private lateinit var mTvTranslateText: TextView
    private lateinit var mTvTranslateSymbol: TextView
    private lateinit var mIvTranslateSound: ImageView

    private lateinit var mCardDictPanel: ViewGroup

    private lateinit var mIvClose: ImageView

    private lateinit var tvTranslateLanguage: TextView

    private lateinit var mLlResultContainer: ViewGroup
    private var mSelectTranslateLangDialog: AlertDialog? = null
    private var mSelectSrcLangDialog: AlertDialog? = null
    private val mDefaultLang = Lang.CHINESE
    private val mDefaultLangCode = LanguageHelper.CHINESE()

    private val mContent: String
        get() {
            val value = mEtContent.text.toString()
            if (value.isEmpty()) {
                setVisibility(mLlResultContainer, false)
                mLlResultContainer.visibility = View.GONE
            } else {
                setVisibility(mLlResultContainer, true)
            }
            return value
        }


    private lateinit var mIvRecognized: ImageView
    private var mRecognizing = false
    private var mNeedRestartAudioRecognize = false

    private lateinit var mBinding: FTranslationBinding

    override fun getContentView(): View {
        mBinding = FTranslationBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun initView(root: View) {
        EventBus.getDefault().register(this)

        tvTranslateLanguage = root.findViewById(R.id.tvTranslateLanguage)
        mInputContainer = root.findViewById(R.id.inputContainer)
        mEtContent = root.findViewById(R.id.etInputContent)
        mIvClose = root.findViewById(R.id.ivInputClose)
        mIvClose.setOnClickListener(this)
//        mBtnTranslate = root.findViewById(R.id.btnTranslate)
        mTvResult = root.findViewById(R.id.tvResult)
//        mBtnTranslate.setOnClickListener(this)
        mIvChangeLanguage = root.findViewById(R.id.ivChange)
        mIvChangeLanguage.setOnClickListener(this)
        mTvSrcLanguage = root.findViewById(R.id.tvSrcLanguage)
        mTvSrcLanguage.text = Lang.AUTO.KEY
        mTvSrcLanguage.setOnClickListener(this)
        mTvTranslateLanguage = root.findViewById(R.id.tvTranslateLanguage)
        mTvTranslateLanguage.text = Lang.ENGLISH.KEY
        mTvTranslateLanguage.setOnClickListener(this)
        mTvTranslateLanguage.text = SettingHelper.getDefaultTranslateLangKey()

        mTvSoundSrcLanguage = root.findViewById(R.id.tvSoundSrcLanguage)
        mTvSrcText = root.findViewById(R.id.tvSrcText)
        mTvSrcSymbol = root.findViewById(R.id.tvSrcSymbol)
        mIvSrcSound = root.findViewById(R.id.ivSoundSrc)
        mIvSrcSound.setOnClickListener(this)
        mIvLiked = root.findViewById(R.id.ivLiked)
        mIvLiked.setOnClickListener(this)
        root.findViewById<View>(R.id.ivCopySrcText).setOnClickListener(this)

        mTvSoundTranslateLanguage = root.findViewById(R.id.tvSoundTranslateLanguage)
        mTvTranslateText = root.findViewById(R.id.tvTranslateText)
        mTvTranslateSymbol = root.findViewById(R.id.tvTranslateSymbol)
        mIvTranslateSound = root.findViewById(R.id.ivSoundTranslate)
        mIvTranslateSound.setOnClickListener(this)
        root.findViewById<View>(R.id.ivCopyTranslateText).setOnClickListener(this)
        root.findViewById<View>(R.id.ivShareTranslateSound).setOnClickListener(this)

        mLlResultContainer = root.findViewById(R.id.llResultContainer)

        mSelectTranslateLangDialog = DialogHelper.createSelectLangDialog(
            activity,
            1,
            listener = object : DialogHelper.SelectLangListener {
                override fun onItemSelected(AlertDialog: AlertDialog?, data: SelectLangItem) {
                    AlertDialog?.dismiss()
                    mTvTranslateLanguage.text = data.lang?.KEY ?: mDefaultLang.KEY
                    SettingHelper.setDefaultTranslateLang(data.lang?.KEY ?: mDefaultLang.KEY)
                    EventBus.getDefault().post(DefaultTranslateLangChangedEventB())
                    translate()
                }
            })
        mSelectSrcLangDialog = DialogHelper.createSelectLangDialog(
            activity,
            0,
            object : DialogHelper.SelectLangListener {
                override fun onItemSelected(AlertDialog: AlertDialog?, data: SelectLangItem) {
                    AlertDialog?.dismiss()
                    mTvSrcLanguage.text = data.lang?.KEY ?: mDefaultLang.KEY
                }
            })

        mEtContent.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                val editable = mEtContent.text
                if (editable != null) {
                    val content = editable.toString()
                    hideKeyboard()
                    translate()
                    Global.searchCount++
                    return@OnEditorActionListener true
                }
            }

            false
        })

        mEtContent.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length == 0) {
                    setVisibility(mIvClose, false)
                    setVisibility(mLlResultContainer, false)
                } else {
                    setVisibility(mIvClose, true)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}


        })

        mCardDictPanel = root.findViewById(R.id.cardDictPanel)

    }

    override fun initData() {
        val argSrcText = arguments?.getString(EXTRA_SRC_TEXT, "") ?: ""
        if (argSrcText.isEmpty()) {
            return
        }
        setVisibility(mInputContainer, false)
        setVisibility(mTvResult, false)
        setVisibility(mIvRecognized, false)
        mEtContent.setText(argSrcText)
        translate()
    }

    override fun createPresenter(): TranslationPresenter = TranslationPresenter()

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
//            R.id.btnTranslate -> {
//                translate()
//            }
            R.id.ivChange -> {
                //点击只允许中英切换
                if (tvTranslateLanguage.text == Lang.CHINESE.KEY) {
                    mTvTranslateLanguage.text = Lang.ENGLISH.KEY
                } else {
                    mTvTranslateLanguage.text = Lang.CHINESE.KEY
                }
                translate()
            }
            R.id.ivSoundSrc -> {
                val content = mTvSrcText.text.toString()
                val tl = Global.langKeyCodeMap[mTvSoundSrcLanguage.text] ?: mDefaultLangCode
                mPresenter.playAudio(content, tl)
            }

            R.id.ivSoundTranslate -> {
                val content = mTvTranslateText.text.toString()
                val tl = Global.langKeyCodeMap[mTvSoundTranslateLanguage.text] ?: mDefaultLangCode
                mPresenter.playAudio(content, tl)
            }

            R.id.tvTranslateLanguage -> {
                mSelectTranslateLangDialog?.show()
            }

            R.id.tvSrcLanguage -> {
                mSelectSrcLangDialog?.show()
            }
            R.id.ivInputClose -> {
                mEtContent.setText("")
                setVisibility(mIvClose, false)
            }
            R.id.ivLiked -> {
                liked()
            }
            R.id.ivCopySrcText -> {
                mPresenter?.copyText(mTvSrcText.text?.toString())
            }
            R.id.ivCopyTranslateText -> {
                mPresenter?.copyText(mTvTranslateText.text?.toString())
            }
            R.id.ivShareTranslateSound -> {
                val content = mTvTranslateText.text.toString()
                val tl = Global.langKeyCodeMap[mTvSoundTranslateLanguage.text] ?: mDefaultLangCode
                mPresenter.shareAudio(this, content, tl)
            }
        }
    }

    private fun translate() {
        val content = mContent
        val sl = Global.langKeyCodeMap[mTvSrcLanguage.text] ?: mDefaultLangCode
        val tl = Global.langKeyCodeMap[mTvTranslateLanguage.text] ?: mDefaultLangCode

        mPresenter.translate(content, sl, tl)
    }

    private fun liked() {
        val content = mContent
        val sl = Global.langKeyCodeMap[mTvSrcLanguage.text] ?: mDefaultLangCode
        val tl = Global.langKeyCodeMap[mTvTranslateLanguage.text] ?: mDefaultLangCode

        mPresenter.liked(content, sl, tl)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLikeUpdate(likeUpdateEvent: LikeUpdateEventB) {
        val history = likeUpdateEvent.history
        val content = mContent
        val sl = Global.langKeyCodeMap[mTvSrcLanguage.text] ?: mDefaultLangCode
        val tl = Global.langKeyCodeMap[mTvTranslateLanguage.text] ?: mDefaultLangCode
        if (content == history.srcText && sl == history.sl && tl == history.tl) {
            refreshLiked(history.liked == 1)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRemoveLikesEvent(removeLikesEvent: RemoveLikesEventB) {
        removeLikesEvent.list.map {
            val history = it
            val content = mContent
            val sl = Global.langKeyCodeMap[mTvSrcLanguage.text] ?: mDefaultLangCode
            val tl = Global.langKeyCodeMap[mTvTranslateLanguage.text] ?: mDefaultLangCode
            if (content == history.srcText && sl == history.sl && tl == history.tl) {
                refreshLiked(history.liked == 1)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPlayAudioFinish(playAudioFinishEvent: PlayAudioFinishEventB) {
        if (mNeedRestartAudioRecognize) {
            EventBus.getDefault().post(RecognizedEventB(true))
        }
    }

    override fun updateResult(
        bean: TranslateResult
    ) {
        setVisibility(mLlResultContainer, true)

        mTvSoundTranslateLanguage.text = mTvTranslateLanguage.text
        mTvSoundSrcLanguage.text = bean.fromLangText
        mTvSrcText.text = bean.srcText
        mTvSrcSymbol.text = bean.srcSymbol
        mTvTranslateText.text = bean.translateText
        mTvTranslateSymbol.text = bean.translateTextSymbol
        mTvResult.text = bean.more
    }

    override fun refreshLiked(liked: Boolean) {
        if (liked) {
            mIvLiked.setImageResource(R.drawable.ic_star_full)
        } else {
            mIvLiked.setImageResource(R.drawable.ic_star_empty)
        }
    }

    override fun showOrHideSoundSrcSymbol(show: Boolean) {
        setVisibility(mTvSrcSymbol, show)
    }

    override fun showOrHideSoundTranslateSymbol(show: Boolean) {
        setVisibility(mTvTranslateSymbol, show)
    }

    override fun showOrHideDictInfo(show: Boolean) {
        setVisibility(mCardDictPanel, show)
    }

    fun hideKeyboard() {
        val inputMethodManager =
            context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputMethodManager.isActive) {
            inputMethodManager.hideSoftInputFromWindow(
                activity?.currentFocus?.windowToken, 0
            )
        }
    }

    companion object {
        const val EXTRA_SRC_TEXT = "EXTRA_SRC_TEXT"
        fun newInstance(srcText: String = ""): TranslationFragmentPage {
            val fragment = TranslationFragmentPage()
            val bundle = Bundle()
            bundle.putString(EXTRA_SRC_TEXT, srcText)
            fragment.arguments = bundle
            return fragment
        }
    }
}