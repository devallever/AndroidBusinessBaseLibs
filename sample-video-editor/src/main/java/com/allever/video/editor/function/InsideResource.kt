package com.allever.video.editor.function

open class InsideResource : EffectResource() {
    var isAssertRes = false
        protected set
    var paths: Array<String>? = null
        protected set
    var names: Array<String>? = null
        protected set
    var ids: IntArray? = null
        protected set
    var iconResId: Int = 0
        protected set
    var iconName: String? = null
        protected set
    var iconAssertPath: String? = null
        protected set
}