package app.flash.tunnel.vpn.lib.common.util

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AssetsManager {

    suspend fun readFile2String(context: Context, fileName: String) =
        withContext(Dispatchers.IO) {
            try {
                val inputStream = context.assets.open(fileName)
                val bufferReader = inputStream.bufferedReader()
                bufferReader.use {
                    return@withContext it.readText()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return@withContext ""
        }
}