package com.allever.video.editor.function.music

import com.android.absbase.utils.FileUtils
import com.allever.video.editor.function.DataManager
import com.allever.video.editor.utils.AssetsUtil
import com.allever.video.editor.utils.FileUtil
import java.io.File

object InsideMusicTool {

    var MUSIC_INSIDE_RESOURCE = mutableListOf<InsideMusicResource>()

    init {
        val frenzyResource = InsideMusicResource()
        frenzyResource.createInsideMusicResource(
            "frenzy.mp3",
            "music/frenzy/frenzy.mp3",
            "music/frenzy/logo.jpg",
            "Frenzy"
        )
        MUSIC_INSIDE_RESOURCE.add(frenzyResource)

        val nightResource = InsideMusicResource()
        nightResource.createInsideMusicResource("night.mp3", "music/night/night.mp3", "music/night/logo.jpg", "Night")
        MUSIC_INSIDE_RESOURCE.add(nightResource)

        val streetResource = InsideMusicResource()
        streetResource.createInsideMusicResource(
            "street.mp3",
            "music/street/street.mp3",
            "music/street/logo.jpg",
            "Street"
        )
        MUSIC_INSIDE_RESOURCE.add(streetResource)

    }

    class InsideMusicResource(
        var path: String? = null,
        var cachePath: String? = null,
        var existCacheFile: Boolean? = false,
        var name: String? = null,
        var iconPath: String? = null,
        var title: String? = null
    ) {

        fun createInsideMusicResource(name: String?, path: String?, iconPath: String?, title: String?) {
            this.title = title
            this.name = name
            this.path = path
            this.cachePath = DataManager.EXTERNAL_CACHE_DIR + File.separator + path
            this.iconPath = iconPath
            this.existCacheFile = FileUtils.isExistFile(this.cachePath!!)
            if (this.existCacheFile == false) {
                val ins = AssetsUtil.toInputStream(this.path)
                val outPutFile = FileUtil.createNewFile(this.cachePath, false)
                this.existCacheFile = FileUtil.inputStream2File(ins, outPutFile)
            }
        }
    }

}