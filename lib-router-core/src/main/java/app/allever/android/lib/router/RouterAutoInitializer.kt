package app.allever.android.lib.router

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import app.allever.android.lib.core.helper.CoroutineHelper
import dalvik.system.DexFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RouterAutoInitializer : ContentProvider() {

    override fun onCreate(): Boolean {
        CoroutineScope(Dispatchers.IO) .launch {
            try {
                val context = context ?: return@launch
                val applicationInfo = context.applicationInfo
                val dexFile = DexFile(applicationInfo.sourceDir)
                val entries = dexFile.entries()
                while (entries.hasMoreElements()) {
                    val className = entries.nextElement()
                    if (className.startsWith("app.allever.android.lib.router.module.RouterModule_")) {
                        try {
                            Class.forName(className)
                        } catch (_: Exception) {
                        }
                    }
                }
            } catch (_: Exception) {
            }
        }
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        return 0
    }
}