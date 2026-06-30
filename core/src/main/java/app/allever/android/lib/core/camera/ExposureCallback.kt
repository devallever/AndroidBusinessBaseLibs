package app.allever.android.lib.core.camera

/** 曝光补偿回调，用于 UI 层显示 EV 值 */
interface ExposureCallback {
    fun onEvChanged(ev: Int)
}