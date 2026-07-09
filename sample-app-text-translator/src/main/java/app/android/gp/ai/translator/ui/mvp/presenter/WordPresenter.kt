package app.android.gp.ai.translator.ui.mvp.presenter

import app.android.gp.ai.translator.event.LikeUpdateEventB
import app.android.gp.ai.translator.event.RemoveLikesEventB
import app.android.gp.ai.translator.ui.adapter.item.WordItem
import app.android.gp.ai.translator.db.DBHelper
import app.android.gp.ai.translator.db.History
import app.android.gp.ai.translator.ui.mvp.view.WordView
import app.woejt.wwzdndgl.lib.mvp.BasePresenter
import org.greenrobot.eventbus.EventBus
import org.litepal.LitePal

class WordPresenter : BasePresenter<WordView>() {
    fun getLikedHistory(sl: String, tl: String) {
        run {
            try {
                //四种情况
                var historyList = mutableListOf<History>()
                if (sl.isEmpty() && tl.isEmpty()) {
                    //查所有
                    historyList =
                        LitePal.where("liked = ?", "1").order("time desc").find(History::class.java)
                }

                if (sl.isNotEmpty() && tl.isNotEmpty()) {
                    historyList = LitePal
                        .where("sl = ? and tl = ? and liked = ?", sl, tl, "1")
                        .order("time desc")
                        .find(History::class.java)
                }

                if (sl.isEmpty() && tl.isNotEmpty()) {
                    historyList = LitePal
                        .where("tl = ? and liked = ?", tl, "1")
                        .order("time desc")
                        .find(History::class.java)
                }

                if (sl.isNotEmpty() && tl.isEmpty()) {
                    historyList = LitePal
                        .where("sl = ? and liked = ?", sl, "1")
                        .order("time desc")
                        .find(History::class.java)
                }
                val wordItemList = mutableListOf<WordItem>()
                historyList.map {
                    val wordItem = WordItem()
                    wordItem.history = it
                    wordItem.checked = false
                    wordItemList.add(wordItem)
                }
                mViewRef?.get()?.updateWordList(wordItemList)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun removeLiked(history: History?) {
        history ?: return
        val result = DBHelper.like(history)
        if (result) {
            val newHistory = DBHelper.getHistory(history.srcText, history.sl, history.tl)
            if (newHistory != null) {
                EventBus.getDefault().post(LikeUpdateEventB(newHistory))
            }
        } else {
        }
    }

    fun removeLikes(wordItemSet: MutableSet<WordItem>?) {
        val historyList = mutableListOf<History>()
        wordItemSet?.map {
            DBHelper.like(it.history, false)
            historyList.add(it.history!!)
        }
        EventBus.getDefault().post(RemoveLikesEventB(historyList))
    }
}