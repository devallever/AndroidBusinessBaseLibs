package app.android.gp.ai.translator.ui.mvp.presenter

import android.app.Activity
import android.os.Environment
import app.android.gp.ai.translator.ui.mvp.view.BackupRestoreView
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.toast
import app.android.gp.ai.translator.R
import app.android.gp.ai.translator.app.mvp.BasePresenter
import app.android.gp.ai.translator.bean.Backup
import app.android.gp.ai.translator.db.DBHelper
import app.android.gp.ai.translator.util.FileUtils
import com.google.gson.Gson
//import org.xm.app.text.translator.ui.mvp.view.BackupRestoreView
import java.io.File

class BackupRestorePresenter : BasePresenter<BackupRestoreView>() {
    private val BACKUP_FILE_PATH =
        Environment.getExternalStorageDirectory().absolutePath + File.separator + App.context.packageName + File.separator + "backup" + File.separator + "data.json"

    fun backup(activity: Activity, task: Runnable) {

        val historyList = DBHelper.getAllHistory()
        if (historyList.isEmpty()) {
            toast(R.string.no_backup_data)
            task.run()
            return
        }
        val backupBean = Backup()
        backupBean.data = historyList
        val result = Gson().toJson(backupBean)
        log("backupResult = $result")
        val success = app.allever.android.lib.core.util.FileUtils.saveStringToFile(result, BACKUP_FILE_PATH)
        if (success) {
            toast(R.string.backup_success)
        } else {
            toast(R.string.backup_fail)
        }
        task.run()

    }

    fun restore(activity: Activity, task: Runnable) {
//        PermissionManager.request(object : PermissionListener {
//            override fun onGranted(grantedList: MutableList<String>) {
//                kotlin.run {
//                    val data = FileUtil.readFileToString(BACKUP_FILE_PATH)
//                    if (data == null || data.isEmpty()) {
//                        toast(R.string.no_backup_data)
//                        task.run()
//                        return
//                    }
//
//                    try {
//                        val backupBean = JsonHelper.json2Object(data!!, BackupBean::class.java)
//                        val historyList = backupBean?.data
//                        historyList?.map {
//                            log("记录：${it.srcText}")
//                            val record = DBHelper.getHistory(it.srcText, it.sl, it.tl)
//                            if (record == null) {
//                                val history = History()
//                                history.srcText = it.srcText
//                                history.sl = it.sl
//                                history.tl = it.tl
//                                history.time = it.time
//                                history.liked = it.liked
//                                history.result = it.result
//                                history.ttsPath = it.ttsPath
//                                val saveResult = history.save()
//                                if (saveResult) {
//                                    log("恢复翻译成功")
//                                } else {
//                                    logE("恢复翻译失败")
//                                }
//                            }
//                        }
//                        EventBus.getDefault().post(RestoreEvent())
//                        toast(R.string.restore_success)
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                        toast(R.string.restore_fail)
//                    }
//                    task.run()
//                }
//            }
//
//            override fun onDenied(deniedList: MutableList<String>) {
//                toast(R.string.no_read_store_permission_tips)
//                task.run()
//            }
//        }, Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    fun delBackup(activity: Activity, task: Runnable) {

//        PermissionManager.request(object : PermissionListener {
//            override fun onGranted(grantedList: MutableList<String>) {
//                kotlin.run {
//                    FileUtil.deleteFile(BACKUP_FILE_PATH)
//                    task.run()
//                    toast(getString(R.string.backup_success))
//                }
//            }
//
//            override fun onDenied(deniedList: MutableList<String>) {
//                toast(R.string.no_wire_store_permission_tips)
//                task.run()
//            }
//        }, Manifest.permission.READ_EXTERNAL_STORAGE)

    }
}