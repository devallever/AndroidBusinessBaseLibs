/*
 * Copyright 2014 http://Bither.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.allever.video.editor.utils

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * Class containing some static utility methods.
 */
object StreamUtils {


    @Throws(IOException::class)
    fun readBytes(paramInputStream: InputStream): ByteArray {
        val arrayOfByte = ByteArray(paramInputStream.available())
        paramInputStream.read(arrayOfByte)
        return arrayOfByte
    }

    fun copyStream(`is`: InputStream, os: OutputStream) {
        var bis: BufferedInputStream? = null
        var bos: BufferedOutputStream? = null
        try {
            bis = BufferedInputStream(`is`)
            bos = BufferedOutputStream(os)
            val bt = ByteArray(8192)
            var len = bis.read(bt)
            while (len != -1) {
                bos.write(bt, 0, len)
                len = bis.read(bt)
            }
        } catch (e: Exception) {

            e.printStackTrace()
        } finally {
            try {
                bis?.close()
            } catch (e: Exception) {
            }
            try {
                bos?.close()
            } catch (e: Exception) {
            }
        }

    }


}
