package org.xm.app.virtual.call.ui.mvp.presenter

import android.annotation.SuppressLint
import android.database.Cursor
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.media.RingtoneManager.TITLE_COLUMN_INDEX
import android.os.AsyncTask
import app.allever.android.lib.core.app.App
import org.xm.app.virtual.call.mvp.BasePresenter
import app.allever.android.lib.core.ext.log
import org.xm.app.virtual.call.app.Global
import org.xm.app.virtual.call.bean.RingtoneItem
import org.xm.app.virtual.call.function.SettingHelper
import org.xm.app.virtual.call.ui.mvp.view.RingtonePickerView

class RingtonePickerPresenter : BasePresenter<RingtonePickerView>() {
    private var mRingtoneDataTask = RingtoneDataTask()
    private var mMediaPlayer: MediaPlayer = MediaPlayer()

    fun getRingtoneData() {
        mRingtoneDataTask.execute()
//        if (Global.ringtoneItemList.isEmpty()) {
//            mRingtoneDataTask.execute()
//        } else {
//            mViewRef?.get()?.refreshRingtoneList(Global.ringtoneItemList)
//        }
    }

    fun playRingtone(ringtoneItem: RingtoneItem) {
        try {
            if (ringtoneItem.uri == null) {
                return
            }

            mMediaPlayer.stop()
            mMediaPlayer.release()
            mMediaPlayer = MediaPlayer()
            mMediaPlayer.setDataSource(App.context, ringtoneItem.uri!!)
            mMediaPlayer.prepare()
            mMediaPlayer.start()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopPlay() {
        mMediaPlayer.stop()
    }

    fun saveRingtone(ringtoneItem: RingtoneItem) {
        SettingHelper.setRingtoneTitle(ringtoneItem.title ?: "Default")
        SettingHelper.setRingtoneUri(ringtoneItem.uri.toString())
    }

    fun destroy() {
        mRingtoneDataTask.cancel(true)
        mMediaPlayer.stop()
        mMediaPlayer.release()
    }

    @SuppressLint("StaticFieldLeak")
    private inner class RingtoneDataTask : AsyncTask<Void, Void, MutableList<RingtoneItem>>() {
        private var mSaveRingtoneItemPosition = 0
        override fun doInBackground(vararg params: Void?): MutableList<RingtoneItem> {
            val ringtoneItemList = mutableListOf<RingtoneItem>()

            val ringtoneManager = RingtoneManager(App.context)
            ringtoneManager.setType(RingtoneManager.TYPE_RINGTONE)

            val systemRingtoneCursor: Cursor
            try {
                Global.ringtoneItemList.clear()
                Global.ringtoneItemMap.clear()
                systemRingtoneCursor = ringtoneManager.cursor

                systemRingtoneCursor.moveToFirst()
                while (!systemRingtoneCursor.isAfterLast) {
                    val ringtoneItem = RingtoneItem()
                    val ringtoneTitle = systemRingtoneCursor.getString(TITLE_COLUMN_INDEX)
                    val ringtoneUri = ringtoneManager.getRingtoneUri(systemRingtoneCursor.position)
                    ringtoneItem.uri = ringtoneUri
                    ringtoneItem.title = ringtoneTitle
                    ringtoneItem.checked = false
                    ringtoneItemList.add(ringtoneItem)

                    Global.ringtoneItemList.add(ringtoneItem)
                    Global.ringtoneItemMap[ringtoneTitle] = ringtoneItem

                    systemRingtoneCursor.moveToNext()
                }

                val selectedItem = Global.ringtoneItemMap[SettingHelper.getRingtoneTitle()]
                selectedItem?.checked = true
                if (selectedItem != null) {
                    mSaveRingtoneItemPosition = Global.ringtoneItemList.indexOf(selectedItem)
                }
            } catch (e: Exception) {
                log("Could not get system ringtone cursor")
            }

            return ringtoneItemList
        }

        override fun onPostExecute(result: MutableList<RingtoneItem>?) {
            if (result != null) {
                mViewRef?.get()?.refreshRingtoneList(result, mSaveRingtoneItemPosition)
            }
        }
    }
}