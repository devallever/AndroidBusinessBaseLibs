package app.allever.android.ai.qr.scanner.core.preview

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.View
import com.android.absbase.utils.ResourcesUtils
import com.google.zxing.client.result.ParsedResultType
import com.allever.app.qr.code.scaner.R
import java.lang.ref.WeakReference

/*
public enum ParsedResultType {

  ADDRESSBOOK,
  EMAIL_ADDRESS,
  PRODUCT,
  URI,
  TEXT,
  GEO,
  TEL,
  SMS,
  CALENDAR,
  WIFI,
  ISBN,
  VIN,

}
 */


data class ResultTypeUIData(val iconId: Int, val nameResId: Int, val startColor: Int, val endColor: Int, val iconHasBackground: Boolean = true) {
    var drawableWRef: WeakReference<Drawable?>? = null
    var drawable: Drawable? = null
        get() {
            var res = drawableWRef?.get()
            if (res == null) {
                res = ResourcesUtils.getDrawable(iconId)
                drawableWRef = WeakReference(res)
            }
            return res
        }

    var name: String? = null
        get() {
            return if (field == null) {
                ResourcesUtils.getString(nameResId)
            } else ""
        }
}

object ResultUIModel {
    private val prt2UIData = mapOf(
            ParsedResultType.ADDRESSBOOK to getUIData(R.drawable.icon_user, R.string.result_type_name_addressbook, "#ffa564", "#ff8f30")
            , ParsedResultType.EMAIL_ADDRESS to getUIData(R.drawable.icon_email, R.string.result_type_name_email_address, "#e57ab9", "#de49a4")
            , ParsedResultType.PRODUCT to getUIData(R.drawable.icon_bar, R.string.result_type_name_product, "#a1adb8", "#7b8996")
            , ParsedResultType.URI to getUIData(R.drawable.icon_url, R.string.result_type_name_uri, "#5589cc", "#0075c1")
            , ParsedResultType.TEXT to getUIData(R.drawable.icon_word, R.string.result_type_name_text, "#00c8e1", "#00bbdc")
            , ParsedResultType.GEO to getUIData(R.drawable.icon_location, R.string.result_type_name_geo, "#ff706c", "#ff3942")
            , ParsedResultType.TEL to getUIData(R.drawable.icon_number, R.string.result_type_name_tel, "#5462b5", "#4b4ab2")
            , ParsedResultType.SMS to getUIData(R.drawable.icon_message, R.string.result_type_name_sms, "#a5cb00", "#7fb700")
            , ParsedResultType.CALENDAR to getUIData(R.drawable.icon_bar, R.string.result_type_name_calendar, "#a1adb8", "#7b8996")
            , ParsedResultType.WIFI to getUIData(R.drawable.icon_wifi, R.string.result_type_name_wifi, "#92d957", "#7cc43a")
            , ParsedResultType.ISBN to getUIData(R.drawable.icon_bar, R.string.result_type_name_isbn, "#a1adb8", "#7b8996")
            , ParsedResultType.VIN to getUIData(R.drawable.icon_bar, R.string.result_type_name_vin, "#a1adb8", "#7b8996")


            , ParsedResultType.INSTAGRAM to getUIData(R.drawable.icon_instagram, R.string.result_type_name_instagram, "#f58243", "#eb7949", false)
            , ParsedResultType.FACEBOOK to getUIData(R.drawable.icon_facebook, R.string.result_type_name_facebook, "#3d5ea6", "#3b5999", false)
            , ParsedResultType.WHATSAPP to getUIData(R.drawable.icon_whatsapp, R.string.result_type_name_whatsapp, "#00e676", "#05d570", false)
            , ParsedResultType.YOUTUBE to getUIData(R.drawable.icon_youtube, R.string.result_type_name_youtube, "#e93323", "#d62d1e", false)
            , ParsedResultType.TWITTER to getUIData(R.drawable.icon_twitter, R.string.result_type_name_twitter, "#40b6fc", "#2ba9fc", false)
            , ParsedResultType.SPOTIFY to getUIData(R.drawable.icon_spotify, R.string.result_type_name_spotify, "#1ed762", "#21d55d", false)
            , ParsedResultType.VIBER to getUIData(R.drawable.icon_viber, R.string.result_type_name_viber, "#8d4cc8", "#7e4aa8", false)


    )
    public val defaultUIData = prt2UIData[ParsedResultType.TEXT]!!
    public val clipboardUIData = getUIData(R.drawable.icon_clipboard, R.string.share_type_clipboard, "#a1adb8", "#7b8996")

    private fun getUIData(iconId: Int, nameResId: Int, startColor: String, endColor: String, iconHasBackground:Boolean = true): ResultTypeUIData {
        val sc = Color.parseColor(startColor)
        val ec = Color.parseColor(endColor)
        return ResultTypeUIData(iconId, nameResId, sc, ec, iconHasBackground)
    }

    fun get(type: ParsedResultType? = null): ResultTypeUIData {
        var resultTypeUIData = defaultUIData
        if (type != null) {
            val tmp = prt2UIData[type]
            if (tmp != null) {
                resultTypeUIData = tmp
            }
        }
        return resultTypeUIData
    }

    fun changeBackground(type: ParsedResultType?, view: View?, singleColor: Boolean = true) {
        val data = get(type)
        val background: Drawable? = view?.background
        if (background is GradientDrawable) {
            background.mutate()
            when {
                singleColor -> {
                    background.setColor(data.endColor)
                }
                else -> {
                    background.colors = intArrayOf(data.startColor, data.endColor)
                }
            }
            background.setStroke(0, Color.TRANSPARENT)
        } else {
            view?.setBackgroundColor(data.endColor)
        }
    }

    fun changeBackground(uiData: ResultTypeUIData, view: View?, singleColor: Boolean = true) {
        val background: Drawable? = view?.background
        if (background is GradientDrawable) {
            background.mutate()
            when {
                singleColor -> {
                    background.setColor(uiData.endColor)
                }
                else -> {
                    background.colors = intArrayOf(uiData.startColor, uiData.endColor)
                }
            }
            background.setStroke(0, Color.TRANSPARENT)
        } else {
            view?.setBackgroundColor(uiData.endColor)
        }
    }
}

//object ResultModel {
//    class ResultBean(var id: Int, var historyItem: com.google.zxing.client.android.history.HistoryItem,
//                     var type: ParsedResultType? = null,
//                     var safe: Int? = null) {
//        var result: Result = historyItem.result
//    }
//
//    @SuppressLint("StaticFieldLeak")
//    private val historyManager: HistoryManager = HistoryManager(App.getContext())
//    var dataChangeListner: OnDataChangeLisenter? = null
//
//    var results = mutableListOf<ResultBean>()
//
//    fun updateData() {
//        val items = historyManager.buildHistoryItems()
//        val tmpDatas = results.subList(0, min(results.size, items.size))
//        var changed = tmpDatas.size != results.size
//        val oldSize = tmpDatas.size
//        for ((index, value) in items.withIndex()) {
//            if (index < oldSize) {
//                val result = tmpDatas[index]
//                result.id = index
//                if (result.historyItem !== value) {
//                    result.historyItem = value
//                    result.safe = null
//                    changed = true
//                    continue
//                }
//            } else {
//                tmpDatas.add(ResultBean(index, value))
//                changed = true
//            }
//        }
//        results = tmpDatas
//        if (changed) {
//            dataChangeListner?.onChange()
//        }
//    }
//
//    fun getResultHandler(activity: Activity, result: ResultBean): ResultHandler {
//        val historyItem = result.historyItem
//        return ResultHandlerFactory.makeResultHandler(activity, historyItem.result)
//    }
//
//    fun addHistoryItem(result: Result, handler: ResultHandler) {
//        addHistoryItem(null, result, handler)
//    }
//
//    fun addHistoryItem(activity: Activity?, result: Result, handler: ResultHandler) {
//        historyManager.addHistoryItem(activity, result, handler)
//    }
//
//    fun clearup(resultBeans: List<ResultBean>? = null) {
//        if (resultBeans == null) {
//            results.clear()
//            TaskRunnable.run(object : Runnable {
//                override fun run() {
//                    historyManager.clearHistory()
//                }
//            }, 0, TaskRunnable.TYPE_BACKGROUND)
//        } else {
//            val ids = resultBeans.map { it.historyItem.id }
//            results.removeAll(resultBeans)
//            historyManager.deleteHistoryItem(ids)
//            updateData()
//        }
//        dataChangeListner?.onChange()
//    }
//
//    interface OnDataChangeLisenter {
//        fun onChange()
//    }
//}