package z.app.allever.android.sample.function.download

import android.Manifest
import android.os.Build
import androidx.lifecycle.lifecycleScope
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.core.permission.PermissionEngine
import app.allever.android.lib.core.permission.PermissionHelper
import app.allever.android.lib.core.permission.StoragePermissionStrategy
import app.allever.android.lib.mvvm.base.BaseViewModel
import kotlinx.coroutines.launch

/**
 *@Description
 *@author: zq
 *@date: 2024/1/16
 */
class DownloadMainFragment : ListFragment<FragmentListBinding, BaseViewModel, TextClickItem>() {

    private val permissionLauncher = PermissionEngine.with(this)

    private val permissionsList = java.util.ArrayList<String>().apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    override fun getAdapter() = TextClickAdapter()

    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        TextClickItem("下载并保存图片") {
            performDownload()
        }
    )

    private fun performDownload() {
        //
        if (PermissionHelper.hasPermissions(requireContext(), permissionsList)) {
            downloadAndSave()
        } else {
            permissionLauncher
                .permissions()
                .strategy(StoragePermissionStrategy)
                .request { allGranted, deniedList ->
                    downloadAndSave()
                }
        }
    }

    private fun downloadAndSave() {
        lifecycleScope.launch {
            val url = "https://img0.baidu.com/it/u=962361882,2281204904&fm=253&fmt=auto&app=138&f=JPEG?w=889&h=500"
            log("开始下载")
            toast("开始下载")
//            ImageLoader.download(url) {success, file ->
//                file?.copyToAlbum(requireContext(), file.name, "AndroidSampleLibs")
//            }
        }
    }
}