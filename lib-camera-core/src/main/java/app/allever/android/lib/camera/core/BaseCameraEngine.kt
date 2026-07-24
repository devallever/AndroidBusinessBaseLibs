package app.allever.android.lib.camera.core

import android.view.View
import java.lang.ref.WeakReference

abstract class BaseCameraEngine : ICameraEngine {
    protected var currentFacing = CameraFacing.FACE_BACK
    protected var previewRef: WeakReference<View>? = WeakReference<View>(null)
}