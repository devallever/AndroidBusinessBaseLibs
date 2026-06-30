package com.allever.video.editor.function.font

import android.graphics.Typeface
import android.text.TextUtils
import android.widget.ImageView

import com.android.absbase.App
import com.android.absbase.helper.log.DLog
import com.android.absbase.utils.ZipUtils
import com.allever.video.editor.R
import com.allever.video.editor.function.DataManager
import com.allever.video.editor.function.online.LocalDataBean
import com.allever.video.editor.function.online.OnlineDataManager
import com.allever.video.editor.function.online.OnlineManager
//import com.allever.video.editor.glide.GlideApp
import com.allever.video.editor.utils.FileUtil

import java.io.File
import java.util.ArrayList

/**
 * 管理内置及在线字体，字体加载
 */

object FontHelper {

    private val TAG = FontHelper::class.java.simpleName

    const val DEFAULT_FONT_PKG_NAME = "DEFAULT"
    private const val FONT_PKG_NAME_PREFIX = "com.photoeditor.plugins.font."
    private val sFontPath = DataManager.RES_FONT_DIR
    private var builtinFontList = ArrayList<LocalDataBean>()

    init {
        builtinFontList = ArrayList()
        val fontOBjs = arrayOf(
            arrayOf(DEFAULT_FONT_PKG_NAME, R.drawable.gird_icon_fonts_roboto, ""),
            arrayOf(FONT_PKG_NAME_PREFIX + "cinzel.bold", R.drawable.gird_icon_fonts_cinzel, "fonts/Cinzel-Bold.ttf"),
            arrayOf(
                FONT_PKG_NAME_PREFIX + "concertone.regular",
                R.drawable.gird_icon_fonts_concert_one,
                "fonts/ConcertOne-Regular.ttf"
            ),
            arrayOf(
                FONT_PKG_NAME_PREFIX + "indieflower",
                R.drawable.gird_icon_fonts_indie_flower,
                "fonts/IndieFlower.ttf"
            ),
            arrayOf(
                FONT_PKG_NAME_PREFIX + "lobster.regular",
                R.drawable.gird_icon_fonts_lobster,
                "fonts/Lobster-Regular.ttf"
            ),
            arrayOf(FONT_PKG_NAME_PREFIX + "vt323.regular", R.drawable.gird_icon_fonts_vt323, "fonts/VT323-Regular.ttf")
        )
        for (objs in fontOBjs) {
            val bean = LocalDataBean()
            bean.packageName = objs[0] as String
            bean.isBuildin = true
            bean.resIconName = objs[1] as Int
            if (!TextUtils.isEmpty(objs[2] as String)) {
                bean.assetName = objs[2] as String
            }
            builtinFontList.add(bean)
        }
    }

    fun getFontList(): List<LocalDataBean> {
        val fonts = ArrayList(builtinFontList)
        // 这里增加在线数据
        val localFontDatas = OnlineDataManager.getInstance().localFontBeanList
        for (it in localFontDatas.iterator()) {
            val pkg = it.key
            val localFontData = it.value
            fonts.add(localFontData)
        }
        DLog.d(TAG, "font size = ${fonts.size}")
        // ...
        return fonts
    }

    fun loadFont(bean: LocalDataBean): Typeface? {
        var ret: Typeface? = null
        if (DEFAULT_FONT_PKG_NAME == bean.packageName) {
            ret = Typeface.DEFAULT
        } else if (bean.isBuildin) {
            //内置字体
            ret = Typeface.createFromAsset(App.getContext().assets, bean.assetName)
        } else {
            //下载的字体
            if (FontHelper.checkExistTTF(bean)) {
                //存在ttf字体文件
                val file = File(getTtfFontPath(bean))
                if (file.exists()) {
                    ret = Typeface.createFromFile(file)
                }
            } else if (FontHelper.checkExistFontZip(bean)) {
                //存在 zip文件，则解压zip
                try {
                    val fileName = OnlineDataManager.getInstance().getMd5FromFileName(FileUtil.getFileName(bean.url))
                    FileUtil.unzipFolder(
                        DataManager.RES_FONT_DIR + File.separator + fileName,
                        DataManager.INTERNAL_FONT_DIR
                    )
                    val file = File(getTtfFontPath(bean))
                    ret = Typeface.createFromFile(file)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
        return ret
    }

    fun loadFontIcon(bean: LocalDataBean?, imageView: ImageView? /*, ImageView iconLayer*/) {
        if (bean == null || imageView == null/* || iconLayer == null*/) {
            return
        }
        if (bean.resIconName > 0) {
            //内置的字体
            imageView.setImageResource(bean.resIconName)
        } else {
            //下载的字体
            //加载字体小图
            val smallImgUrl = bean.getDefaultSmallImgUrl()
            if (smallImgUrl?.isEmpty() == false) {
//                GlideApp.with(App.getContext()).load(smallImgUrl).error(R.drawable.ic_download).into(imageView)
            }
        }
    }


    fun checkExistFontZip(bean: LocalDataBean): Boolean {
        val zipFileName = OnlineDataManager.getInstance().getMd5FromFileName(FileUtil.getFileName(bean.url))
        if (TextUtils.isEmpty(zipFileName)) {
            return false
        }

        val zipFile = File(sFontPath + File.separator + zipFileName)
        return zipFile.exists()
    }

    fun checkExistTTF(bean: LocalDataBean): Boolean {
        val ttfDir = DataManager.INTERNAL_FONT_DIR
        val name = getNameFromUrlPath(bean)
        val ttfDirFile = File(ttfDir)
        if (!ttfDirFile.exists()) {
            return false
        }

        for (ttfFileName in ttfDirFile.list()) {
            if (ttfFileName.endsWith(".ttf", ignoreCase = true)) {
                val realName = ttfFileName.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                if (name.equals(realName, ignoreCase = true)) {
                    return true
                }
            }
        }
        return false
    }

    /***
     * 获取下载字体.zip 并解压后的字体.ttf 的文件路径
     * ttf文件名 和 zip文件名相同
     * @param bean
     * @return
     */
    fun getTtfFontPath(bean: LocalDataBean): String {
        val stringBuilder = StringBuilder(DataManager.INTERNAL_FONT_DIR + File.separator)
        val parentFile = File(stringBuilder.toString())
        if (!parentFile.exists()) {
            parentFile.mkdirs()
        }
        for (fileName in parentFile.list()) {
            if (fileName.endsWith(".ttf", ignoreCase = true)) {
                val name = fileName.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                val compareName = getNameFromUrlPath(bean)
                if (name == compareName) {
                    stringBuilder.append(fileName)
                    val result = stringBuilder.toString()
                    return result
                }
            }
        }
        return ""
    }

    /***
     * /fileName.zip -> fileName
     * @param bean
     * @return
     */
    fun getNameFromUrlPath(bean: LocalDataBean?): String {
        if (bean == null) {
            return ""
        }

        val downloadUrl = bean.url
        if (TextUtils.isEmpty(downloadUrl)) {
            return ""
        }
        var fileName = FileUtil.getFileName(downloadUrl)
        if (TextUtils.isEmpty(fileName)) {
            return ""
        }

        return if (fileName.contains(".")) {
            fileName = fileName.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            fileName
        } else {
            fileName
        }

    }

    fun downloadFont(localDataBean: LocalDataBean, callback: OnlineManager.DownloadResourceCallback) {
        OnlineManager.downloadResource(localDataBean, callback)
    }

    fun unzipDownloadFontFile(localDataBean: LocalDataBean): Boolean {
        val unzipFolder = DataManager.INTERNAL_FONT_DIR
        ZipUtils.unzipFile(File(localDataBean.path), unzipFolder)
        return checkExistTTF(localDataBean)
    }
}
