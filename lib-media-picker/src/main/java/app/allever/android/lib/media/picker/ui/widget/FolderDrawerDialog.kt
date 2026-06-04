package app.allever.android.lib.media.picker.ui.widget

import android.content.Context
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import app.allever.android.lib.core.base.dialog.AbstractBottomDialog
import app.allever.android.lib.media.core.model.MediaFolder
import app.allever.android.lib.media.picker.databinding.DialogFolderPickerBinding
import app.allever.android.lib.media.picker.ui.adapter.FolderAdapter

/**
 * 目录选择抽屉（底部弹出的 Dialog）
 */
class FolderDrawerDialog(context: Context) : AbstractBottomDialog(context) {

    private lateinit var binding: DialogFolderPickerBinding
    private lateinit var adapter: FolderAdapter

    private var currentBucketId: Long = -1L
    private val folders = mutableListOf<MediaFolder>()
    private var onFolderSelected: ((MediaFolder) -> Unit)? = null

    override fun getLayoutId(): Int = app.allever.android.lib.media.picker.R.layout.dialog_folder_picker

    override fun initView() {
        binding = DialogFolderPickerBinding.inflate(LayoutInflater.from(context))

        adapter = FolderAdapter { folder ->
            onFolderSelected?.invoke(folder)
            dismiss()
        }
        binding.recyclerViewFolders.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewFolders.adapter = adapter

        // 取消按钮
        binding.tvDismiss.setOnClickListener {
            dismiss()
        }
    }

    /**
     * 设置目录数据并显示
     * @param allFolders 所有目录列表
     * @param selectedId 当前选中的目录 ID（-1 表示全部目录）
     * @param callback 目录选中回调
     */
    fun showWithFolders(
        allFolders: List<MediaFolder>,
        selectedId: Long,
        callback: (MediaFolder) -> Unit,
    ) {
        this.currentBucketId = selectedId
        this.onFolderSelected = callback

        folders.clear()
        folders.addAll(allFolders)
        adapter.submitList(folders)
        adapter.setSelectedBucketId(selectedId)

        show()
    }

    companion object {
        const val TAG = "FolderDrawerDialog"
    }
}
