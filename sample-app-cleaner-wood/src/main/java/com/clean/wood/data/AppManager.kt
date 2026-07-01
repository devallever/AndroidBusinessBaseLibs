package com.clean.wood.data

import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import com.clean.wood.WoodApp
import com.clean.wood.data.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AppManager private constructor() {
    companion object {
        val ins by lazy {
            AppManager()
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    suspend fun scanApp(): List<AppInfo> = withContext(Dispatchers.IO) {
        val result = mutableListOf<AppInfo>()
        val packageManager = WoodApp.context.packageManager
        val packageInfoList: List<PackageInfo> = packageManager.getInstalledPackages(0)
        for (packageInfo in packageInfoList) {
            val appInfo = packageInfo.applicationInfo!!
            if (((ApplicationInfo.FLAG_SYSTEM and appInfo.flags) == 0)
                && ((ApplicationInfo.FLAG_UPDATED_SYSTEM_APP and appInfo.flags) == 0)
            ) {
                result.add(
                    AppInfo(
                        appInfo.loadIcon(packageManager),
                        packageManager.getApplicationLabel(appInfo).toString(),
                        packageInfo.firstInstallTime,
                        getApkFilesSize(appInfo) / 1000
                    )
                )
            }
        }
        return@withContext result
    }

    /**
     * Unit is Byte
     */
    private fun getApkFilesSize(applicationInfo: ApplicationInfo): Long {
        var filesSize: Long = 0
        try {
            val baseApkPath = applicationInfo.publicSourceDir
            filesSize = getFilesSize(File(baseApkPath))
            filesSize += applicationInfo.splitPublicSourceDirs?.sumOf { getFilesSize(File(it)) }
                ?: 0
        } catch (_: Exception) {
        }
        return filesSize
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
}