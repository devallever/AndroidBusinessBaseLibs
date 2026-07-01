package com.allever.video.editor.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.allever.video.editor.R

object ShareUtil {

    fun share(context: Context?, uri: Uri?, isImage: Boolean) {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        if (isImage) {
            intent.type = "image/*"
        } else {
            intent.type = "video/*"
        }
        context?.startActivity(Intent.createChooser(intent, context.getString(R.string.share_save_share_to)))
    }
}