package app.allever.android.lib.router

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import dalvik.system.DexFile

class RouterAutoInitializer : ContentProvider() {

    override fun onCreate(): Boolean {
        try {
            val context = context ?: return true
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