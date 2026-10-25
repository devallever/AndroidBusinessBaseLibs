package app.funnyappptech.android.fake.call.viewmodel

import android.annotation.SuppressLint
import android.database.Cursor
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.media.RingtoneManager.TITLE_COLUMN_INDEX
import android.os.AsyncTask
import androidx.lifecycle.MutableLiveData
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.log
import app.funnyappptech.android.fake.call.app.FakeCall
import app.funnyappptech.android.fake.call.base.BaseViewModel
import app.funnyappptech.android.fake.call.data.RingtoneItem
import app.funnyappptech.android.fake.call.function.SettingStore
import kotlin.collections.get

class RingtonePickerViewModel : BaseViewModel() {
    private var mRingtoneDataTask = RingtoneDataTask()
    private var mMediaPlayer: MediaPlayer = MediaPlayer()

    val ringtoneLiveData by lazy {
        MutableLiveData<RingtoneData>()
    }

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
        SettingStore.setRingtoneTitle(ringtoneItem.title ?: "Default")
        SettingStore.setRingtoneUri(ringtoneItem.uri.toString())
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
                FakeCall.ringtoneItemList.clear()
                FakeCall.ringtoneItemMap.clear()
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

                    FakeCall.ringtoneItemList.add(ringtoneItem)
                    FakeCall.ringtoneItemMap[ringtoneTitle] = ringtoneItem

                    systemRingtoneCursor.moveToNext()
                }

                val selectedItem = FakeCall.ringtoneItemMap[SettingStore.getRingtoneTitle()]
                selectedItem?.checked = true
                if (selectedItem != null) {
                    mSaveRingtoneItemPosition = FakeCall.ringtoneItemList.indexOf(selectedItem)
                }
            } catch (e: Exception) {
                log("Could not get system ringtone cursor")
            }

            return ringtoneItemList
        }

        override fun onPostExecute(result: MutableList<RingtoneItem>?) {
            if (result != null) {
                ringtoneLiveData.postValue(RingtoneData(result, mSaveRingtoneItemPosition))
            }
        }
    }

    class RingtoneData(val ringtoneItemList: MutableList<RingtoneItem>, val saveIndex: Int)
}