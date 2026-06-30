package com.allever.video.editor.function.online

import com.android.absbase.App
import com.android.absbase.helper.log.DLog
import com.android.absbase.utils.FileUtils
import com.android.absbase.utils.SecurityUtils
import com.allever.video.editor.function.DataManager
import com.allever.video.editor.function.download.DownloadCallback
import com.allever.video.editor.function.download.DownloadManager
import com.allever.video.editor.function.download.OkDownloadExecutor
import com.allever.video.editor.function.download.TaskInfo
import com.allever.video.editor.utils.FileUtil
import java.io.File

object OnlineManager {
    private val TAG = OnlineManager::class.java.name
    private val TEMP_DIR = FileUtils.getCacheDir(App.getContext(), "", true)

    fun preloadUrl(url: String, callback: DownloadCallback? = null): TaskInfo {
        val fileNameMd5 = SecurityUtils.encrypt(url) ?: "${System.currentTimeMillis()}"
        val taskInfo =
            TaskInfo(url, TEMP_DIR, fileNameMd5)
        DownloadManager.getInstance().start(taskInfo, object :
            DownloadCallback {
            override fun onStart() {
                callback?.onStart()
            }

            override fun onConnected(totalLength: Long) {
                callback?.onConnected(totalLength)
            }

            override fun onProgress(current: Long, totalLength: Long) {
                DLog.d(TAG, "download progress: $current / $totalLength")
                callback?.onProgress(current, totalLength)
            }

            override fun onPause(taskInfo: TaskInfo) {
                callback?.onPause(taskInfo)
            }

            override fun onCompleted(taskInfo: TaskInfo) {
                DLog.d(TAG, "download completed: " + taskInfo.path)
                callback?.onCompleted(taskInfo)
            }

            override fun onError(e: Exception) {
                callback?.onError(e)
            }
        }, false)
        return taskInfo
    }

    fun downloadResource(localDataBean: LocalDataBean, downloadResourceCallback: DownloadResourceCallback) {
        val isDownload = FileUtil.isExistsFile(localDataBean.path)
        if (!isDownload) {
            val tempPath = DataManager.TEMP_DIR
            val url = localDataBean.url
            DownloadManager.getInstance()
                .start(
                    OkDownloadExecutor(
                        localDataBean.fileNameMd5,
                        tempPath,
                        url,
                        object : DownloadCallback {
                            override fun onStart() {
                            }

                            override fun onConnected(totalLength: Long) {
                                downloadResourceCallback.onConnected()
                            }

                            override fun onProgress(current: Long, totalLength: Long) {
                                val percent = current.toFloat() / totalLength.toFloat() * 100
                                downloadResourceCallback.onProgress(percent.toInt())
                            }

                            override fun onPause(taskInfo: TaskInfo?) {
                            }

                            override fun onCompleted(taskInfo: TaskInfo?) {
                                if (taskInfo == null) {
                                    return
                                }
                                try {
                                    val sourcePath =
                                        taskInfo.path + File.separator + taskInfo.fileName
                                    FileUtil.copyAndDeleteFile(sourcePath, localDataBean.dir)
                                    localDataBean.downloaded = true
                                    downloadResourceCallback.onCompleted()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    downloadResourceCallback.onFailed()
                                }
                            }

                            override fun onError(e: Exception) {
                                e.printStackTrace()
                                downloadResourceCallback.onFailed()
                            }
                        })
                )
        } else {
            downloadResourceCallback.onCompleted()
        }
    }


    public interface DownloadResourceCallback {
        fun onConnected()
        fun onCompleted()
        fun onProgress(progress: Int)
        fun onFailed()
    }

}