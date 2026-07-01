package com.allever.video.editor.function.online

import com.allever.video.editor.function.DataManager
import com.allever.video.editor.utils.FileUtil
import java.io.File

class LocalDataBean {
    var id = ""
    var packageName = ""

    /**
     * 资源类型，跟配置文件的type对应
     */
    var type = 0

    /**
     * 完整下载路径
     */
    var url = ""

    /***
     * 下载的文件名
     */
    var fileName = ""

    /**
     *
     */
    var fileNameMd5 = ""

    /**
     * 下载文件存储路径
     */
    var path = ""

    /***
     * 存储目录
     */
    var dir = ""

    /***
     * 资源的名称
     */
    var name = ""

    /**
     * 是否需要购买
     */
    var isNeedBuy = false

    var downloaded = false

    var smallImgUrlList: MutableList<String>? = mutableListOf()
    var midImgUrlList: MutableList<String>? = mutableListOf()
    var bigImgUrlList: MutableList<String>? = mutableListOf()

    //内置特效字段
    var isBuildin = true
    var resIconName = 0
    var assetName = ""


    fun getDefaultSmallImgUrl(): String? {
        return if (smallImgUrlList?.size!! > 0) {
            smallImgUrlList?.get(0)
        } else {
            ""
        }
    }

    fun getDefaultMidImgUrl(): String? {
        return if (midImgUrlList?.size!! > 0) {
            midImgUrlList?.get(0)
        } else {
            ""
        }
    }

    fun getDefaultBigImgUrl(): String? {
        return if (bigImgUrlList?.size!! > 0) {
            bigImgUrlList?.get(0)
        } else {
            ""
        }
    }

    companion object {
        fun toLocalBean(onlineEffectBean: OnlineEffectBean): LocalDataBean {
            val localDataBean = LocalDataBean()
            localDataBean.id = onlineEffectBean.id
            localDataBean.packageName = onlineEffectBean.pkgName
            localDataBean.type = onlineEffectBean.type
            localDataBean.url = onlineEffectBean.downloadUrl

            val fileName = FileUtil.getFileName(onlineEffectBean.downloadUrl)
            val md5FileName = OnlineDataManager.getInstance().getMd5FromFileName(fileName)

            var dir = ""
            when (onlineEffectBean.type) {
                OnlineDataManager.TYPE_STICKER -> {
                    dir = DataManager.RES_STICKER_DIR
                }

                OnlineDataManager.TYPE_FONT -> {
                    dir = DataManager.RES_FONT_DIR
                }

                OnlineDataManager.TYPE_MUSIC -> {
                    dir = DataManager.RES_MUSIC_DIR
                }
            }

            localDataBean.dir = dir
            localDataBean.path = dir + File.separator + md5FileName
            localDataBean.fileName = fileName
            localDataBean.fileNameMd5 = md5FileName
            localDataBean.name = onlineEffectBean.name
            localDataBean.downloaded = FileUtil.isExistsFile(localDataBean.path)
            localDataBean.smallImgUrlList = onlineEffectBean.smallImgUrlList
            localDataBean.midImgUrlList = onlineEffectBean.midImgUrlList
            localDataBean.bigImgUrlList = onlineEffectBean.bigImgUrlList
            localDataBean.isBuildin = onlineEffectBean.isBuildin
            localDataBean.resIconName = onlineEffectBean.resIconName
            localDataBean.assetName = onlineEffectBean.assetName
            localDataBean.isNeedBuy = onlineEffectBean.isNeedBuy
            return localDataBean
        }
    }
}