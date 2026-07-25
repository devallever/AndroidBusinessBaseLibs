package com.google.zxing.client.android

import android.app.Activity
import android.app.Fragment
import android.content.Intent
import android.provider.MediaStore


object SystemAlbumHelper {

    const val REQUEST_CODE_IMG_SELECTION = 0xff01


    fun start(context: Any) {
        try {
            val intent = Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            when (context) {
                is androidx.fragment.app.Fragment -> {
                    context.startActivityForResult(intent, REQUEST_CODE_IMG_SELECTION)
                }
                is Fragment -> context.startActivityForResult(intent, REQUEST_CODE_IMG_SELECTION)
//                is androidx.core.app.Fragment -> context.startActivityForResult(intent, REQUEST_CODE_IMG_SELECTION)
                is Activity -> context.startActivityForResult(intent, REQUEST_CODE_IMG_SELECTION)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun handleActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?): String? {
        var imagePath: String? = null
        //获取图片路径
        if (requestCode == REQUEST_CODE_IMG_SELECTION && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImage = data.data
            val filePathColumns = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = try {
                activity.contentResolver.query(selectedImage!!, filePathColumns, null, null, null)
            } catch (e: Exception) {
                null
            }
            //小米4手机这种方法cursor的结果为null，需要判空指针
            if (cursor != null) {
                cursor.moveToFirst()
                val columnIndex = cursor.getColumnIndex(filePathColumns[0])
                imagePath = cursor.getString(columnIndex)
                cursor.close()
            } else {
                imagePath = selectedImage?.path
            }
        }
        return imagePath
    }

}