package com.allever.video.editor.function.sticker

import com.allever.video.editor.function.ResourceManager
import com.allever.video.editor.function.UnInstallResource
import java.lang.Exception

class UnInstallStickerResource : UnInstallResource {
    private var iconName: String? = null
    var iconId: Int = 0
    var resIds = mutableListOf<Int>()
    private var resNames: Array<String>? = null

    constructor(pkg: String, zipPath: String) {
        this.packageName = pkg
        this.zipPath = zipPath
        init(zipPath)
    }

    private fun init(zipPath: String) {
        try {
            resource = ResourceManager.getApkResource(zipPath, packageName)
            if (resource != null) {
                exist = true
                name = resource?.getString(resource?.getIdentifier("app_name", "string", packageName) ?: 0) ?: ""
                doInit()
            } else {
                exist = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            exist = false
        }
    }

    private fun doInit() {
        val id = resource?.getIdentifier("sticker_name", "array", packageName) ?: 0
        val names = resource?.getStringArray(id)
        this.resNames = names

        val count = resNames?.size ?: 0
        for (i in 0 until count) {
            val id = resource?.getIdentifier(resNames?.get(i), "drawable", packageName) ?: 0
            this.resIds.add(id)
        }

        val arg1 = resource?.getIdentifier("sticker_icon_name", "string", packageName) ?: 0
        val iconName = resource?.getString(arg1)
        this.iconName = iconName
        val iconId = resource?.getIdentifier(iconName, "drawable", packageName) ?: 0
        this.iconId = iconId
    }
}