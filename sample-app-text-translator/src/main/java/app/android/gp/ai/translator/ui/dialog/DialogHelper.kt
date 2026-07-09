package app.android.gp.ai.translator.ui.dialog

//import org.xm.app.text.translator.ui.adapter.SelectEngineAdapter
//import org.xm.app.text.translator.ui.adapter.SelectLangAdapter
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.android.gp.ai.translator.R
import app.android.gp.ai.translator.app.Global
import app.android.gp.ai.translator.language.Lang
import app.android.gp.ai.translator.ui.adapter.item.SelectEngineItem
import app.android.gp.ai.translator.ui.adapter.item.SelectLangItem
import app.android.gp.ai.translator.language.LanguageHelper
import app.android.gp.ai.translator.translate.TranslationHelper
import app.android.gp.ai.translator.db.History
import app.android.gp.ai.translator.bean.TranslateResult
import app.android.gp.ai.translator.ui.adapter.SelectEngineAdapter
import app.android.gp.ai.translator.ui.adapter.SelectLangAdapter
import app.android.gp.ai.translator.util.ClipboardHelper
import app.android.gp.ai.translator.util.JsonHelper
import app.woejt.wwzdndgl.lib.recycler.BaseViewHolder
import app.woejt.wwzdndgl.lib.recycler.ItemListener

object DialogHelper {

    fun createTranslateResultDialog(activity: Activity?, history: History?): AlertDialog? {
        activity ?: return null
        history ?: return null

        //解析数据
        val translationBean =
            JsonHelper.json2Object(history.result, TranslateResult::class.java)

        val root =
            LayoutInflater.from(activity).inflate(R.layout.d_translate_result, null, false)

        lateinit var mTranslateResultContainer: View

        lateinit var mTvSoundSrcLanguage: TextView
        lateinit var mTvSrcText: TextView
        lateinit var mTvSrcSymbol: TextView
        lateinit var mIvSrcSound: ImageView
        lateinit var mIvLiked: ImageView
        lateinit var mIvCopySrcText: ImageView


        lateinit var mTvSoundTranslateLanguage: TextView
        lateinit var mTvTranslateText: TextView
        lateinit var mTvTranslateSymbol: TextView
        lateinit var mIvTranslateSound: ImageView
        lateinit var mIvCopyTranslateText: ImageView

        lateinit var mTvResult: TextView

        mTranslateResultContainer = root.findViewById(R.id.llResultContainer)

        mTvSoundSrcLanguage = root.findViewById(R.id.tvSoundSrcLanguage)
        mTvSrcText = root.findViewById(R.id.tvSrcText)
        mTvSrcSymbol = root.findViewById(R.id.tvSrcSymbol)
        mIvSrcSound = root.findViewById(R.id.ivSoundSrc)
        mIvSrcSound.setOnClickListener {
            if (translationBean != null) {
                val srcLang = translationBean.fromLang
                TranslationHelper.playTTS(
                    mTvSrcText.text.toString(),
                    Global.langKeyCodeMap[srcLang] ?: LanguageHelper.CHINESE()
                )
            }
        }
        mIvLiked = root.findViewById(R.id.ivLiked)
        mIvLiked.setOnClickListener {
            val newHistory = TranslationHelper.liked(history)
            if (newHistory?.liked == 1) {
                mIvLiked.setImageResource(R.drawable.ic_star_full)
            } else {
                mIvLiked.setImageResource(R.drawable.ic_star_empty)
            }
        }
        mIvCopySrcText = root.findViewById(R.id.ivCopySrcText)
        mIvCopySrcText.setOnClickListener {
            ClipboardHelper.copy(mTvSrcText.text.toString())
        }

        mTvSoundTranslateLanguage = root.findViewById(R.id.tvSoundTranslateLanguage)
        mTvTranslateText = root.findViewById(R.id.tvTranslateText)
        mTvTranslateSymbol = root.findViewById(R.id.tvTranslateSymbol)
        mIvTranslateSound = root.findViewById(R.id.ivSoundTranslate)
        mIvTranslateSound.setOnClickListener {
            TranslationHelper.playTTS(mTvTranslateText.text.toString(), history.tl)
        }
        mIvCopyTranslateText = root.findViewById(R.id.ivCopyTranslateText)
        mIvCopyTranslateText.setOnClickListener {
            ClipboardHelper.copy(mTvTranslateText.text.toString())
        }
        root.findViewById<View>(R.id.ivShareTranslateSound).setOnClickListener {
            val content = mTvTranslateText.text.toString()
            val tl =
                Global.langKeyCodeMap[mTvSoundTranslateLanguage.text] ?: LanguageHelper.CHINESE()
            TranslationHelper.shareAudio(activity, content, tl)
        }

        mTvResult = root.findViewById(R.id.tvResult)

        if (translationBean != null) {
            val srcLang = translationBean.fromLang
            val srcText = translationBean.srcText
            val srcSymbol = translationBean.srcSymbol
            val translateText = translationBean.translateText
            val translateSymbol = translationBean.translateTextSymbol
            val dictText = translationBean.more
            mTvSoundSrcLanguage.text = srcLang
            if (history.liked == 1) {
                mIvLiked.setImageResource(R.drawable.ic_star_full)
            } else {
                mIvLiked.setImageResource(R.drawable.ic_star_empty)
            }
            mTvSrcText.text = srcText
            mTvSrcSymbol.text = srcSymbol
            mTvSoundTranslateLanguage.text = Global.langCodeKeyMap[history.tl]
            mTvTranslateText.text = translateText
            mTvTranslateSymbol.text = translateSymbol
            mTvResult.text = dictText

        } else {
            mIvSrcSound.visibility = View.GONE
            mIvTranslateSound.visibility = View.GONE
            mIvLiked.visibility = View.GONE
            mIvSrcSound.visibility = View.GONE
            mIvCopySrcText.visibility = View.GONE
            mIvCopyTranslateText.visibility = View.GONE
        }

        mTranslateResultContainer.visibility = View.VISIBLE

        return AlertDialog.Builder(activity, app.woejt.wwzdndgl.lib.R.style.CommonCustomDialogStyle)
            .setView(root)
            .setCancelable(true)
            .create()
    }

    /***
     * @param type 0: 原， 1：翻译
     */
    fun createSelectLangDialog(
        activity: Activity?,
        type: Int = 1,
        listener: SelectLangListener?
    ): AlertDialog? {
        activity ?: return null
        val root =
            LayoutInflater.from(activity).inflate(R.layout.d_select_language, null, false)

        val dialog = activity.let {
            AlertDialog.Builder(it)
                .setView(root)
                .create()
        }

        val dataList = mutableListOf<SelectLangItem>()
        Global.langList.map {
            val item = SelectLangItem()
            item.lang = it
            item.selected = false
            dataList.add(item)
        }

        if (type == 1) {
            dataList.removeAt(0)
        }

        val adapter = SelectLangAdapter(activity, R.layout.item_language, dataList)
        val recyclerView = root.findViewById<RecyclerView>(R.id.rvLanguage)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()

        adapter.mItemListener = object : ItemListener {
            override fun onItemClick(position: Int, holder: BaseViewHolder) {
                listener?.onItemSelected(dialog, dataList[position])
            }
        }

        return dialog
    }

    /***
     *
     */
    fun createSelectLangDialog(
        activity: Activity?,
        listener: SelectLangListener?
    ): AlertDialog? {
        activity ?: return null
        val root =
            LayoutInflater.from(activity).inflate(R.layout.d_select_language, null, false)

        val dialog = activity.let {
            AlertDialog.Builder(it)
                .setView(root)
                .create()
        }

        val dataList = mutableListOf<SelectLangItem>()
        Global.langList.map {
            val item = SelectLangItem()
            item.lang = it
            item.selected = false
            dataList.add(item)
        }

        dataList.removeAt(0)
        val firstItem = SelectLangItem()
        firstItem.lang = Lang.ALL
        dataList.add(0, firstItem)

        val adapter = SelectLangAdapter(activity, R.layout.item_language, dataList)
        val recyclerView = root.findViewById<RecyclerView>(R.id.rvLanguage)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()

        adapter.mItemListener = object : ItemListener {
            override fun onItemClick(position: Int, holder: BaseViewHolder) {
                listener?.onItemSelected(dialog, dataList[position])
            }
        }

        return dialog
    }

    /***
     *
     */
    fun createSelectEngineDialog(
        activity: Activity?,
        listener: SelectEngineListener?
    ): AlertDialog? {
        activity ?: return null
        val root =
            LayoutInflater.from(activity).inflate(R.layout.d_select_engine, null, false)

        val dialog = activity.let {
            AlertDialog.Builder(it)
                .setView(root)
                .create()
        }

        val dataList = mutableListOf<SelectEngineItem>()
        Global.engineList.map {
            val item = SelectEngineItem()
            item.value = it
            item.selected = false
            dataList.add(item)
        }

        val adapter = SelectEngineAdapter(activity, R.layout.item_language, dataList)
        val recyclerView = root.findViewById<RecyclerView>(R.id.rvLanguage)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()

        adapter.mItemListener = object : ItemListener {
            override fun onItemClick(position: Int, holder: BaseViewHolder) {
                listener?.onItemSelected(dialog, dataList[position])
            }
        }

        return dialog
    }

    interface SelectLangListener {
        fun onItemSelected(alertDialog: AlertDialog?, data: SelectLangItem)
    }

    interface SelectEngineListener {
        fun onItemSelected(alertDialog: AlertDialog?, data: SelectEngineItem)
    }
}