package app.allever.android.sample.camera.core

import app.allever.android.lib.common.FragmentActivity
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import com.chad.library.adapter.base.BaseQuickAdapter

class CameraSampleListFragment: ListFragment<FragmentListBinding, ListViewModel, TextClickItem>() {
    override fun getAdapter(): BaseQuickAdapter<TextClickItem, *> = TextClickAdapter()

    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        TextClickItem("Camera") {
            FragmentActivity.start<CameraFragment>(it.title)
        },
        TextClickItem("Camera2") {
            FragmentActivity.start<Camera2Fragment>(it.title)
        },
        TextClickItem("CameraX") {
            FragmentActivity.start<CameraXFragment>(it.title)
        },
        TextClickItem("CameraCore-CameraX") {
            FragmentActivity.start<CameraCoreFragment>(it.title) {
                it.putString("engine", "camerax")
            }
        },
        TextClickItem("CameraCore-Camera2") {
            FragmentActivity.start<CameraCoreFragment>(it.title) {
                it.putString("engine", "camera2")
            }
        },
        TextClickItem("CameraCore-Camera") {
            FragmentActivity.start<CameraCoreFragment>(it.title) {
                it.putString("engine", "camera")
            }
        },
    )
}