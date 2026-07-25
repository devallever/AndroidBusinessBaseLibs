package app.allever.android.ai.qr.scanner.core.history

import android.app.Activity
import android.util.SparseArray
import com.android.absbase.utils.TaskRunnable
import com.android.absbase.utils.TimeUtils
import com.google.zxing.Result
import com.google.zxing.client.android.history.HistoryManager
import com.google.zxing.client.android.result.ResultHandler
import app.allever.android.ai.qr.scanner.core.result.ResultHandlerFactory
import kotlin.math.min

class HistoryDataModel(val activity: Activity, val historyManager: HistoryManager) {
    private var mDatas = mutableListOf<com.google.zxing.client.android.history.HistoryItem>()
    private var mResultHandlers = SparseArray<ResultHandler>()
    var dataChangeListner: OnDataChangeLisenter? = null

    fun updateDataAsync() {
        TaskRunnable.run({
            updateData()
        }, 0, TaskRunnable.TYPE_BACKGROUND)
    }

    fun updateData() {
        val items = historyManager.buildHistoryItems()
        val tmpDatas = mDatas.subList(0, min(mDatas.size, items.size))
        var changed = tmpDatas.size != mDatas.size || tmpDatas.size == 0
        val oldSize = tmpDatas.size
        for ((index, value) in items.withIndex())
            if (index < oldSize) {
                if (tmpDatas[index] != value) {
                    val resultHandler = ResultHandlerFactory.makeResultHandler(activity, value.result)
                    mResultHandlers.put(index, resultHandler)
                    tmpDatas[index] = value
                    changed = true
                    continue
                }
            } else {
                val resultHandler = ResultHandlerFactory.makeResultHandler(activity, value.result)
                mResultHandlers.put(index, resultHandler)
                tmpDatas.add(value)
                changed = true
            }
        mDatas = tmpDatas
        if (changed) {
            dataChangeListner?.onChange()
        }
    }

    fun getResultHandler(id: Int, rawResult: Result): ResultHandler {
        var resultHandler: ResultHandler? = mResultHandlers.get(id)
        if (resultHandler == null) {
            resultHandler = ResultHandlerFactory.makeResultHandler(activity, rawResult)
            mResultHandlers.put(id, resultHandler)
        }
        return resultHandler!!
    }

    fun getFormatDatas(): MutableList<AnyItem> {
        val formatDatas = arrayListOf<AnyItem>()
        var prevTime: String? = null
        for ((id, item) in mDatas.withIndex()) {
            val timestamp = item.result.timestamp
            val time = TimeUtils.getTime(timestamp, TimeUtils.dateFormat)
            if (prevTime == null || prevTime != time) {
                prevTime = time
                formatDatas.add(DateItem(prevTime))
            }
            val resultHandler = getResultHandler(id, item.result)
            val type = resultHandler.type
            val historyItem = HistoryItem(item, id, resultHandler, type, false)
            formatDatas.add(historyItem)
        }
        if (formatDatas.size == 0) {
            formatDatas.add(Item<String>(ITEM_TYPE_NO_DATA, ""))
        }
        return formatDatas
    }

    fun clearup(deleteItems: List<HistoryItem>? = null) {
        if (deleteItems == null) {
            mDatas.clear()
            TaskRunnable.run({
                historyManager.clearHistory()
            }, 0, TaskRunnable.TYPE_BACKGROUND)
        } else {
            val items = mutableListOf<com.google.zxing.client.android.history.HistoryItem>()
            for (tmp in deleteItems) {
                items.add(tmp.obj)
                mResultHandlers.remove(tmp.id)
            }
            mDatas.removeAll(items)
            TaskRunnable.run({
                for (item in items) {
                    historyManager.deleteHistoryItemById(item.id)
                }
                updateData()
            }, 0, TaskRunnable.TYPE_BACKGROUND)
        }

        dataChangeListner?.onChange()
    }

    interface OnDataChangeLisenter {
        fun onChange()
    }
}