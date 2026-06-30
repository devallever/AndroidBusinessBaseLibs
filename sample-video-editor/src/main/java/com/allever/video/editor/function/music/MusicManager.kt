package com.allever.video.editor.function.music

import com.android.absbase.utils.FileUtils
import com.android.absbase.utils.ZipUtils
import com.allever.video.editor.function.DataManager
import com.allever.video.editor.function.online.LocalDataBean
import com.allever.video.editor.function.online.OnlineDataManager
import com.allever.video.editor.function.online.OnlineManager
import java.io.File

object MusicManager {
    private val TAG = MusicManager::class.java.simpleName

    fun getLocalMusicDatas(): Map<String, LocalDataBean> {
        return OnlineDataManager.getInstance().localMusicBeanList
    }

    fun getInsideMusicResource(): MutableList<InsideMusicTool.InsideMusicResource> {
        return InsideMusicTool.MUSIC_INSIDE_RESOURCE
    }

    fun downloadMusic(localDataBean: LocalDataBean, downloadMusicCallback: OnlineManager.DownloadResourceCallback) {
        OnlineManager.downloadResource(localDataBean, downloadMusicCallback)
    }

    fun checkUnzipFileExist(localDataBean: LocalDataBean): Boolean {
        val fileSourceName = getFileSourceName(localDataBean)
        //音乐文件和图标同时存在
        val dir = DataManager.INTERNAM_MUSIC_DIR + File.separator + fileSourceName + File.separator
        val musicPath = "$dir$fileSourceName.mp3"
        val existMusic = FileUtils.isExistFile(musicPath)
        val logoPath = dir + "logo.jpg"
        val existLogo = FileUtils.isExistFile(logoPath)
        return (existMusic && existLogo)
    }

    fun unzipDownloadMusicFile(localDataBean: LocalDataBean): Boolean {
        val unzipFolder = DataManager.INTERNAM_MUSIC_DIR
        ZipUtils.unzipFile(File(localDataBean.path), unzipFolder)
        return checkUnzipFileExist(localDataBean)
    }

    fun getUnzipIconPath(localDataBean: LocalDataBean): String {
        val fileSourceName = getFileSourceName(localDataBean)
        return DataManager.INTERNAM_MUSIC_DIR + File.separator + fileSourceName + File.separator + "logo.jpg"
    }

    fun getUnzipMusicPath(localDataBean: LocalDataBean): String {
        val fileSourceName = getFileSourceName(localDataBean)
        val dir = DataManager.INTERNAM_MUSIC_DIR + File.separator + fileSourceName + File.separator
        return "$dir$fileSourceName.mp3"
    }

    fun getUnzipSondInfo(localDataBean: LocalDataBean): SongInfo? {
        val unzipMusicPath = MusicManager.getUnzipMusicPath(localDataBean)
        return SongInfo.createFromPath(unzipMusicPath)
    }

    private fun getFileSourceName(localDataBean: LocalDataBean): String {
        val fileName = localDataBean.fileName
        var fileSourceName: String

        fileSourceName = if (fileName.contains(".")) {
            fileName.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        } else {
            fileName
        }
        return fileSourceName
    }
}