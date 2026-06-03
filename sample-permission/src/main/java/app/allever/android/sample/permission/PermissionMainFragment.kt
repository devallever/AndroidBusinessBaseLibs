package app.allever.android.sample.permission

import android.Manifest
import androidx.activity.result.contract.ActivityResultContracts
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.core.function.permission.JumpPermissionSettingDialog
import app.allever.android.lib.core.function.permission.PermissionHelper
import com.chad.library.adapter.base.BaseQuickAdapter

class PermissionMainFragment: ListFragment<FragmentListBinding, ListViewModel, TextClickItem>() {

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                log("相机权限已授予，可以使用相机功能")
                toast("相机权限已授予，可以使用相机功能")
            } else {
                if (PermissionHelper.hasAlwaysDeniedPermissionOrigin(requireActivity(), listOf(Manifest.permission.CAMERA))) {
                    JumpPermissionSettingDialog(requireActivity()).show()
                } else {
                    log("相机权限被拒绝")
                    toast("相机权限被拒绝")
                }
            }
        }

    private val requestMultiPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var isAllGranted = true
            val deniedList = mutableListOf<String>()
            permissions.entries.forEach {
                if (!it.value) {
                    isAllGranted = false
                    deniedList.add(it.key)
                }
            }
            if (isAllGranted) {
                log("权限已授予，可以使用相机、录音功能")
                toast("权限已授予，可以使用相机、录音功能")
            } else {
                deniedList.forEach {
                    log("权限被拒绝：$it")
                }
                toast("权限被拒绝")
                if (PermissionHelper.hasAlwaysDeniedPermissionOrigin(requireActivity(), deniedList)) {
                    JumpPermissionSettingDialog(requireActivity(), message = "${deniedList.size}个权限总是被拒绝，手动授权").show()
                }
            }
        }

    override fun getAdapter(): BaseQuickAdapter<TextClickItem, *> = TextClickAdapter()

    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        TextClickItem("申请相机权限 (ActivityResultContract)") {
            requestCameraPermission()
        },
        TextClickItem("申请多个权限 (ActivityResultContract)") {
            requestMultiPermission()
        }
    )

    private fun requestCameraPermission() {
        requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun requestMultiPermission() {
        requestMultiPermissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
    }

}