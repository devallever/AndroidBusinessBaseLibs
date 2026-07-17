package com.allever.android.card.text.pic.text.viewmodel

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Environment
import android.view.View
import androidx.lifecycle.viewModelScope
import com.allever.android.card.text.pic.text.App
import com.allever.android.card.text.pic.text.R
import com.allever.android.card.text.pic.text.base.AbsViewModel
import com.allever.android.card.text.pic.text.model.TemplateManager
import com.allever.android.card.text.pic.text.model.TextCardCore
import com.allever.android.card.text.pic.text.util.copyToAlbum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class EditViewMode : AbsViewModel() {


    private var adThreadHold = 3

    fun checkCanShowAd(): Boolean {
        if (adThreadHold > 2) {
            adThreadHold = 0
            return true
        } else {
            adThreadHold++
            return false
        }
    }

    /**
     * 保存View为图片
     * 流程：
     * 1. 创建与View尺寸相同的Bitmap（ARGB_8888格式，支持透明度）
     * 2. 创建Canvas并绑定到Bitmap
     * 3. 调用view.draw(canvas)将View绘制到Bitmap上
     * 4. 在缓存目录创建临时文件
     * 5. 使用FileOutputStream将Bitmap压缩为PNG格式写入临时文件
     * 6. 调用copyToAlbum()将临时文件复制到系统相册，内部流程：
     *    a. 检查源文件是否存在且可读
     *    b. 打开源文件输入流
     *    c. 调用insertMediaImage()向MediaStore插入图片记录：
     *       - Android Q+: 使用RELATIVE_PATH指定Pictures/应用名目录，设置IS_PENDING=1（临时状态）
     *       - Android Q-: 创建Pictures目录，文件路径查重（重复则添加数字后缀），使用DATA列指定路径
     *    d. 打开MediaStore返回的Uri对应的输出流
     *    e. 将输入流数据复制到输出流
     *    f. 调用finishPending()完成操作：
     *       - Android Q+: 设置IS_PENDING=0（对其他应用可见）
     *       - Android Q-: 更新文件大小，发送MEDIA_SCANNER_SCAN_FILE广播通知媒体库刷新
     * 7. 删除临时文件，释放资源
     * @param view 要保存的View
     * @param fileName 保存的文件名（需带后缀）
     * @return true保存成功，false保存失败
     */
    suspend fun saveViewAsImage(view: View, fileName: String) =
        withContext(Dispatchers.IO) {
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            view.draw(canvas)
            val file = File(App.context.cacheDir.absolutePath, fileName)
            try {
                val out = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
                out.flush()
                out.close()
                file.copyToAlbum(App.context, fileName, App.context.getString(R.string.tc_app_name))
                file.delete()
                return@withContext true
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return@withContext false
        }

    suspend fun saveViewAsImageToCache(view: View, filename: String?): Boolean {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        val file = File(filename)
        try {
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
            out.flush()
            out.close()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun saveView(cb: (success: Boolean, path: String) -> Unit) {
        viewModelScope.launch(Dispatchers.Main) {
            val fileName = "${System.currentTimeMillis()}.jpg"
            val path =
                "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)}"
            val result = saveViewAsImage(
                TemplateManager.currentTemplate.getTemplateContentView(),
                fileName
            )
            cb.invoke(result, "${path}${File.separator}${fileName}")
        }
    }

    fun saveEdittextContent() {
        TemplateManager.currentTemplate.apply {
            TextCardCore.cardData.title = getTitleView().text.toString()
            TextCardCore.cardData.text = getContentView().text.toString()
            TextCardCore.cardData.author = getAuthorView().text.toString()
            TextCardCore.saveCardData()
        }
    }
}