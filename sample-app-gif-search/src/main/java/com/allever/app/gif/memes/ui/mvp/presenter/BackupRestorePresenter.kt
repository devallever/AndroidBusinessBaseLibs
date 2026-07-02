package com.allever.app.gif.memes.ui.mvp.presenter

import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.core.util.FileUtils
import com.allever.app.gif.memes.R
import com.allever.app.gif.memes.ui.mvp.base.BasePresenter
import com.funny.gif.memes.app.Global
import com.funny.gif.memes.bean.BackupBean
import com.funny.gif.memes.bean.event.RestoreLikeEvent
import com.allever.app.gif.memes.ui.mvp.view.BackupRestoreView
import com.funny.gif.memes.util.DBHelper
import com.funny.gif.memes.util.JsonHelper

import com.google.gson.Gson
import org.greenrobot.eventbus.EventBus

class BackupRestorePresenter : BasePresenter<BackupRestoreView>() {
    private val BACKUP_FILE_PATH = Global.backupFilePath


    fun backup(task: Runnable) {

        val likeList = DBHelper.getAllLikeItem()
        if (likeList.isEmpty()) {
            toast(R.string.no_backup_data)
            task.run()
            return
        }
        val backupBean = BackupBean()
        backupBean.data = likeList
        val result = Gson().toJson(backupBean)
        log("backupResult = $result")
        val success = FileUtils.saveStringToFile(result, BACKUP_FILE_PATH)
        if (success) {
            toast(R.string.backup_success)
        } else {
            toast(R.string.backup_fail)
        }
        task.run()

    }

    fun restore(task: Runnable) {
        kotlin.run {
            val data = FileUtils.readFileToString(BACKUP_FILE_PATH)
            if (data == null || data.isEmpty()) {
                toast(R.string.no_backup_data)
                task.run()
                return
            }

            try {
                val backupBean = JsonHelper.json2Object(data, BackupBean::class.java)
                val likeList = backupBean?.data
                likeList?.map {
                    val likeItem = DBHelper.getLikeItem(it.gifId)
                    if (likeItem == null) {
                        DBHelper.liked(it.gifId, it.data)
                    }
                }
                EventBus.getDefault().post(RestoreLikeEvent())
                toast(R.string.restore_success)
            } catch (e: Exception) {
                e.printStackTrace()
                toast(R.string.restore_fail)
            }
            task.run()
        }
    }

    fun delBackup(task: Runnable) {

        kotlin.run {
            FileUtils.deleteFile(BACKUP_FILE_PATH)
            task.run()
            toast(App.context.getString(R.string.backup_success))
        }

    }
}