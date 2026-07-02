package app.allever.android.lib.core.util

import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import androidx.core.content.FileProvider


/**
 * @author allever
 * 通过创建一个自定义的 FileProvider，拦截并重写 update 方法，使其静默返回成功，从而避免抛出异常。
 *
 */
class CustomFileProvider : FileProvider() {

    override fun update(uri: Uri, values: ContentValues?, extras: Bundle?): Int {
        return 1
    }
}