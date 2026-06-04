package app.allever.android.lib.media.picker.selection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.allever.android.lib.media.core.model.MediaItem
import java.util.LinkedHashSet

/**
 * 媒体选择状态管理器
 * 负责管理选中集合、数量限制、选中/取消逻辑
 */
class SelectionManager(private val maxSelect: Int = 9) {

    /** 选中项集合（LinkedHashSet 保持顺序、防重复） */
    private val _selectedItems = LinkedHashSet<MediaItem>()
    val selectedItems: Set<MediaItem> get() = _selectedItems

    /** 选中数量 */
    val selectedCount: Int get() = _selectedItems.size

    /** 是否已达上限 */
    val isFull: Boolean get() = _selectedCount >= maxSelect

    private var _selectedCount = 0

    /** 选中状态变化通知（用于 UI 更新） */
    private val _selectionChanged = MutableLiveData<Unit>()
    val selectionChanged: LiveData<Unit> get() = _selectionChanged

    /**
     * 切换选中状态
     * @return 是否操作成功（已满时添加返回 false）
     */
    fun toggle(item: MediaItem): Boolean {
        return if (_selectedItems.contains(item)) {
            remove(item)
            true
        } else {
            if (isFull) false else add(item)
        }
    }

    /** 添加选中项 */
    fun add(item: MediaItem): Boolean {
        if (isFull) return false
        if (_selectedItems.add(item)) {
            _selectedCount = _selectedItems.size
            notifyChange()
        }
        return true
    }

    /** 移除选中项 */
    fun remove(item: MediaItem) {
        if (_selectedItems.remove(item)) {
            _selectedCount = _selectedItems.size
            notifyChange()
        }
    }

    /** 某项是否被选中 */
    fun isSelected(item: MediaItem): Boolean = _selectedItems.contains(item)

    /** 获取某项的选中序号（1-based），未选中返回 -1 */
    fun selectedIndex(item: MediaItem): Int {
        return _selectedItems.indexOf(item).takeIf { it >= 0 }?.plus(1) ?: -1
    }

    /** 清空所有选中 */
    fun clear() {
        if (_selectedItems.isNotEmpty()) {
            _selectedItems.clear()
            _selectedCount = 0
            notifyChange()
        }
    }

    /** 获取选中列表（保持顺序） */
    fun toList(): List<MediaItem> = _selectedItems.toList()

    private fun notifyChange() {
        _selectionChanged.value = Unit
    }
}
