package com.allever.video.editor.function.sticker

import com.allever.video.editor.function.download.DownloadManager
import com.allever.video.editor.function.download.*
import com.allever.video.editor.function.online.LocalDataBean
import com.allever.video.editor.function.online.OnlineDataManager
import com.allever.video.editor.function.online.OnlineManager

object StickerManger {
    private val TAG = StickerManger::class.java.simpleName

    var insideStickerPackageNames = arrayListOf<String>(
        InsideStickerTool.STICKER_PKG_NAME_EMOJI,
//            InsideStickerTool.STICKER_PKG_NAME_WORLD_CUP
        InsideStickerTool.STICKER_PKG_NAME_SOCIAL
    )

    fun getInsideSticker(packageName: String): InsideStickerTool.StickerInsideResource? {
        return InsideStickerTool.STICKER_INSIDE_INFO[packageName]
    }

    fun getUnInstallSticker(packageName: String): UnInstallStickerResource? {
        return StickerTool.STICKER_UNINSTALL_INFO[packageName]
    }

    fun getUnInstallSticker(localDataBean: LocalDataBean): UnInstallStickerResource? {
        return StickerTool.getUnInstallStickerResource(localDataBean)
    }

    fun addUnInstallStickerRes(localDataBean: LocalDataBean) {
        StickerTool.addUnInstallStickerResource(localDataBean)
    }

    fun getLocalStickerDatas(): Map<String, LocalDataBean> {
        return OnlineDataManager.getInstance().localStickerBeanList
    }

    fun downloadSticker(
        localDataBean: LocalDataBean,
        downloadResourceCallback: OnlineManager.DownloadResourceCallback
    ) {
        OnlineManager.downloadResource(localDataBean, downloadResourceCallback)
    }

    fun stopDownloadSticker(localDataBean: LocalDataBean) {
        DownloadManager.getInstance().cancel(localDataBean.url)
    }
}