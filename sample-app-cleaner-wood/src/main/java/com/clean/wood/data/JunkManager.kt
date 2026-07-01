package com.clean.wood.data

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import androidx.lifecycle.MutableLiveData
import com.clean.wood.WoodApp
import com.clean.wood.data.model.JunkInfo
import com.clean.wood.utils.Constant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID


class JunkManager private constructor() {
    companion object {
        val ins by lazy {
            JunkManager()
        }
    }

    val junkCache = HashMap<Constant.JunkType, MutableList<JunkInfo>>()
    private var scanningId = ""

    /**
     * 扫描设备存储中的冗余文件并计算总大小
     *
     * @param junkLive 用于实时更新扫描进度的LiveData对象，数值表示当前累计扫描到的文件大小（单位：KB）
     * @return 最终扫描到的垃圾文件总大小（单位：KB）
     */
    suspend fun scanJunk(junkLive: MutableLiveData<Double>): Double = withContext(Dispatchers.IO) {
        // 权限检查与申请流程：当系统版本为Android 11+且未获得文件管理权限时，跳转系统设置
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            WoodApp.context.startActivity(
                Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION).addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )
            )
            return@withContext 0.0
        }

        // 扫描初始化：生成唯一标识符，清空缓存，重置进度
        scanningId = UUID.randomUUID().toString()
        junkCache.clear()
        junkLive.postValue(0.0)

        // 文件扫描执行流程：从存储根目录开始递归扫描，并将字节转换为KB单位
        val rootFile = Environment.getExternalStorageDirectory()
        val scanResult = scanFiles(rootFile, 0, junkLive, scanningId) / 1024.0

        // 最终结果处理：更新最终结果到LiveData并返回
        junkLive.postValue(scanResult)
        return@withContext scanResult
    }


    private fun scanFiles(
        file: File,
        junkTempSize: Long,
        junkLive: MutableLiveData<Double>,
        scanId: String
    ): Long {
        if (scanId != scanningId) {
            return 0
        }
        var junkSumSize = junkTempSize
        isJunkFile(file)?.let { junkInfo ->
            val cachedList = junkCache[junkInfo.junkType] ?: mutableListOf()
            junkCache[junkInfo.junkType] = cachedList
            cachedList.add(junkInfo)
            return junkInfo.size
        }
        if (file.isDirectory) {
            val dirJunkSize = file.listFiles()?.sumOf {
                val singleSize = scanFiles(it, junkSumSize, junkLive, scanId)
                junkSumSize += singleSize
                singleSize
            } ?: 0
            if (scanId != scanningId) {
                return 0
            }
            if (dirJunkSize != 0L && junkLive.value != junkSumSize / 1024.0) {
                junkLive.postValue(junkSumSize / 1024.0)
            }
            return dirJunkSize
        }
        return 0
    }

    private fun isJunkFile(file: File): JunkInfo? {
        return when {
            isAdJunk(file) -> Constant.JunkType.Ad
            isSystemCache(file) -> Constant.JunkType.SystemCache
            isResidualJunk(file) -> Constant.JunkType.Residual
            isObsoleteApk(file) -> Constant.JunkType.ObsoleteApk
            isTempFile(file) -> Constant.JunkType.Temp
            isThumbPhoto(file) -> Constant.JunkType.Thumb
            else -> null
        }?.let {
            JunkInfo(
                it,
                file.absolutePath,
                getFilesSize(file)
            )
        }
    }

    private fun isSystemCache(file: File): Boolean {
        return file.path.contains("Android/data") && file.name == "cache"
    }

    private fun isResidualJunk(file: File): Boolean {
        return file.name.startsWith(".") &&
                file.name.contains("Trash", ignoreCase = true) &&
                file.lastModified() < System.currentTimeMillis() - 15 * 24 * 60 * 60 * 1000L
    }

    private fun isAdJunk(file: File): Boolean {
        return false
    }

    private fun isObsoleteApk(file: File): Boolean {
        return file.name.endsWith(".apk", ignoreCase = true) ||
                file.name.endsWith(".apks", ignoreCase = true) ||
                file.name.endsWith(".xapk", ignoreCase = true) ||
                file.name.endsWith(".aab", ignoreCase = true) ||
                file.name.endsWith(".apk.1", ignoreCase = true) ||
                file.name.endsWith(".apk.zip", ignoreCase = true)
    }

    private fun isTempFile(file: File): Boolean {
        return file.name.endsWith(".tmp", ignoreCase = true) ||
                file.name.endsWith(".part", ignoreCase = true) ||
                file.name.endsWith(".temp", ignoreCase = true) ||
                file.name.endsWith(".log", ignoreCase = true)
    }

    private fun isThumbPhoto(file: File): Boolean {
        return (file.name.startsWith("thumb_", ignoreCase = true) &&
                file.name.endsWith(".jpg", ignoreCase = true)
                ) ||
                file.startsWith(".thumbnails") ||
                file.name == ".thumb"
    }

    /**
     * Unit is Byte
     */
    private fun getFilesSize(file: File): Long {
        return if (!file.exists()) {
            0
        } else if (file.isDirectory) {
            file.listFiles()?.sumOf { getFilesSize(it) } ?: 0
        } else {
            file.length()
        }
    }

    /**
     * @return Size of junk cleaned. Unit is KB.
     */
    suspend fun cleanJunk(types: List<Constant.JunkType>): Double = withContext(Dispatchers.IO) {
        val result = types.sumOf { junkType ->
            junkCache[junkType]?.sumOf { junk ->
                File(junk.path).deleteRecursively()
                junk.size
            } ?: 0L
        }
        return@withContext result / 1024.0
    }

    /**
     * @return Size of junk type. Unit is KB.
     */
    fun getJunkSize(type: Constant.JunkType): Double {
        val junkSize = junkCache[type]?.sumOf { junk ->
            junk.size
        } ?: 0L
        return junkSize / 1024.0
    }

    fun getStorageUsagePercent(): Double {
        val statFsInternal = StatFs(Environment.getDataDirectory().path)
        val internalAvailableBytes = statFsInternal.availableBytes
        val internalTotalBytes = statFsInternal.totalBytes
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            val statFsExternal = StatFs(Environment.getExternalStorageDirectory().path)
            val externalAvailableBytes = statFsExternal.availableBytes
            val externalTotalBytes = statFsExternal.totalBytes
            val sum = externalTotalBytes + internalTotalBytes
            val remain = externalAvailableBytes + internalAvailableBytes
            return (sum - remain).toDouble() / sum
        } else {
            return (internalTotalBytes - internalAvailableBytes).toDouble() / internalTotalBytes
        }
    }

    fun getRamUsagePercent(): Double {
        try {
            val memoryInfo = ActivityManager.MemoryInfo()
            val activityManager =
                (WoodApp.context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
            activityManager.getMemoryInfo(memoryInfo)
            val usage =
                (memoryInfo.totalMem - memoryInfo.availMem).toDouble() / memoryInfo.totalMem
            return usage
        } catch (_: Exception) {
        }
        return 0.0
    }

}