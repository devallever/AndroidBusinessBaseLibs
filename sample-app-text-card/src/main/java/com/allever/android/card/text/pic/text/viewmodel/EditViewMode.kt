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
                file.copyToAlbum(App.context, fileName, App.context.getString(R.string.app_name))
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