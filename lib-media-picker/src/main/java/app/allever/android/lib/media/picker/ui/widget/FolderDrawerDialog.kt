package app.allever.android.lib.media.picker.ui.widget

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import app.allever.android.lib.core.base.dialog.AbstractBottomDialog
import app.allever.android.lib.media.core.model.MediaFolder
import app.allever.android.lib.media.picker.databinding.DialogFolderPickerBinding
import app.allever.android.lib.media.picker.ui.adapter.FolderAdapter

/**
 * 目录选择抽屉（底部弹出的 Dialog）
 */
class FolderDrawerDialog(context: Context) : AbstractBottomDialog(context) {

    private val adapter: FolderAdapter by lazy {
        FolderAdapter {
            dismiss()
            onFolderSelected?.invoke(it)
        }
    }

    private val pendingFolders = mutableListOf<MediaFolder>()
    private var pendingSelectedId: Long = -1L
    private var onFolderSelected: ((MediaFolder) -> Unit)? = null

    override fun getLayoutId(): Int = app.allever.android.lib.media.picker.R.layout.dialog_folder_picker

    override fun initView() {
        val binding = DialogFolderPickerBinding.bind(
            window?.decorView?.findViewById<ViewGroup>(android.R.id.content)?.getChildAt(0)!!
        )

        binding.recyclerViewFolders.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewFolders.adapter = adapter

        binding.tvDismiss.setOnClickListener { dismiss() }

        if (pendingFolders.isNotEmpty()) {
            adapter.submitList(pendingFolders.toList())
            adapter.setSelectedBucketId(pendingSelectedId)
            pendingFolders.clear()
        }
    }

    /**
     * 设置目录数据并显示
     */
    fun showWithFolders(
        allFolders: List<MediaFolder>,
        selectedId: Long,
        callback: (MediaFolder) -> Unit,
    ) {
        pendingFolders.clear()
        pendingFolders.addAll(allFolders)
        pendingSelectedId = selectedId
        onFolderSelected = callback

        show()
    }

    companion object {
        const val TAG = "FolderDrawerDialog"
    }
}
