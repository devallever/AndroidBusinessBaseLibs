package app.android.gp.ai.translator.util

import app.woejt.wwzdndgl.lib.util.logRandomString
import java.io.File
import java.io.FileOutputStream

object FileUtils {
    fun saveByteArray2File(byteArray: ByteArray?, path: String): Boolean {
        logRandomString()
        val file = File(path)
        logRandomString()
        return saveByteArray2File(byteArray, file.parent, file.name)
    }


    fun saveByteArray2File(byteArray: ByteArray?, dir: String, fileName: String): Boolean {
        val file = File(dir, fileName)
        logRandomString()
        createDir(dir)
        logRandomString()
        if (file.exists()) {
            file.delete()
            logRandomString()
        } else {
            file.createNewFile()
            logRandomString()
        }

        val fos = FileOutputStream(file)
        logRandomString()
        try {
            fos.write(byteArray)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            fos.close()
        }
        return true
    }

    private fun createDir(dir: String): Boolean {
        val dirFile = File(dir)
        logRandomString()
        return if (dirFile.exists()) {
            true
        } else {
            dirFile.mkdirs()
        }
    }
}