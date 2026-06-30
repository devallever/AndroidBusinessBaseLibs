package com.allever.video.editor.function.sticker

import com.allever.video.editor.function.online.LocalDataBean

object StickerTool {

    val STICKER_UNINSTALL_INFO = mutableMapOf<String, UnInstallStickerResource>()

    fun getUnInstallStickerResource(localDataBean: LocalDataBean): UnInstallStickerResource {
        val pkg = localDataBean.packageName
        val path = localDataBean.path
        var unInstallStickerResource = STICKER_UNINSTALL_INFO[pkg]
        return if (unInstallStickerResource == null) {
            unInstallStickerResource = UnInstallStickerResource(pkg, path)
            STICKER_UNINSTALL_INFO[pkg] = unInstallStickerResource
            unInstallStickerResource
        } else {
            STICKER_UNINSTALL_INFO[pkg]!!
        }
    }

    fun addUnInstallStickerResource(localDataBean: LocalDataBean) {
        STICKER_UNINSTALL_INFO[localDataBean.packageName] =
                UnInstallStickerResource(localDataBean.packageName, localDataBean.path)
    }
}