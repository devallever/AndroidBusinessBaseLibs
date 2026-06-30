package com.allever.video.editor.utils

import android.net.Uri

import java.util.Locale

/**
 * 用于检测当前的Uri是不是File URI
 */
object UriUtil {

    private val START_WIDTH_FILE = "file://"

    fun isFileUri(u: Uri): Boolean {
        val str = u.toString()
        return str.startsWith(START_WIDTH_FILE)
    }

}
