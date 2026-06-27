package com.clean.wood.utils

import android.annotation.SuppressLint

object StorageUtils {

    @SuppressLint("DefaultLocale")
    fun convertBytesToMBOrGB(bytes: Long): String {
        val bytesInMB = ByteConstants.MB // 1 MB = 1048576 Bytes
        val bytesInGB = ByteConstants.GB // 1 GB = 1073741824 Bytes

        return if (bytes < bytesInMB) {
            String.format("%.2f KB", bytes.toFloat() / ByteConstants.KB)
        } else if (bytes < bytesInGB) {
            String.format("%.2f MB", bytes.toFloat() / bytesInMB)
        } else {
            String.format("%.2f GB", bytes.toFloat() / bytesInGB)
        }
    }

    object ByteConstants {
        const val KB = 1024
        const val MB = 1048576
        const val GB = 1073741824
    }
}