package app.allever.android.sample.im.business

import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.databinding.FragmentListBinding
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.network.core.NetCore
import app.allever.android.sample.im.IMConfig
import app.allever.android.sample.im.business.adapter.ServerImageAdapter
import app.allever.android.sample.im.business.data.ServerImageItem
import app.allever.android.sample.im.http.API
import app.allever.android.sample.im.http.response.BaseResponse
import app.allever.android.sample.im.http.response.ImageData
import com.chad.library.adapter.base.BaseQuickAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ServerImageListFragment: ListFragment<FragmentListBinding, ListViewModel, ServerImageItem>() {
    override fun getAdapter(): BaseQuickAdapter<ServerImageItem, *> {
        return ServerImageAdapter()
    }

    override fun getList(): MutableList<ServerImageItem> = mutableListOf()

    override fun layoutManager(): RecyclerView.LayoutManager {
        return GridLayoutManager(requireContext(), 3)
    }

    override fun init() {
        super.init()
        lifecycleScope.launch {
            val response = NetCore.get<BaseResponse<List<ImageData>>>(API.IMAGE_LIST)
            if (response.isSuccess() && response.data != null) {
                val list = mutableListOf<ServerImageItem>()
                response.data.forEach {image ->
                    list.add(ServerImageItem("${IMConfig.getHttpBaseUrl()}${image.url}") {
                        log("image: $image")
                        toast("image: $image")
                    })
                }
                launch(Dispatchers.Main) {
                    mAdapter?.data?.clear()
                    mAdapter?.setList(list)
                }
            }
        }

        mAdapter?.setOnItemLongClickListener {
            adapter, view, position ->
            val item = adapter.getItem(position) as ServerImageItem
            log("long click: ${item.url}")
            val filename = item.url.substringAfterLast("/")
            lifecycleScope.launch {
                val response = NetCore.delete<BaseResponse<Any?>>("${API.IMAGE_DELETE}?filename=$filename")
                if (response.isSuccess()) {
                    toast("删除成功")
                    launch(Dispatchers.Main) {
                        mAdapter?.removeAt(position)
                    }
                } else {
                    toast("删除失败: ${response.msg}")
                }
            }
            true
        }
    }
}