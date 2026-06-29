package app.allever.android.lib.core.camera

/** 相机状态枚举 */
enum class CameraState {
    IDLE,           // 未打开/空闲
    OPENED,         // 已打开，预览中
    TAKING_PHOTO,   // 拍照中
    RECORDING       // 录像中
}