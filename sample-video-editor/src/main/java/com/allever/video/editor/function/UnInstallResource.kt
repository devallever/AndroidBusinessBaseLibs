package com.allever.video.editor.function

import android.content.res.Resources

abstract class UnInstallResource : EffectResource() {
    var resource: Resources? = null
    var zipPath: String? = null
    var exist: Boolean = false
}