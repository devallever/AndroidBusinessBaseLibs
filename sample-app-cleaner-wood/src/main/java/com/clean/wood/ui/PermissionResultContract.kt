package com.clean.wood.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.ContextCompat
import com.clean.wood.WoodApp

class PermissionResultContract : ActivityResultContract<Any?, Boolean>() {
    @SuppressLint("InlinedApi")
    override fun createIntent(context: Context, input: Any?): Intent {
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        intent.data = Uri.parse("package:${WoodApp.context.packageName}")
        return intent
    }

    @SuppressLint("NewApi")
    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        return ContextCompat.checkSelfPermission(
            WoodApp.context,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED || Environment.isExternalStorageManager()
    }
}