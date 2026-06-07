package app.allever.android.sample.store.core

import android.view.View
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.store.core.Storage
import com.chad.library.adapter.base.BaseQuickAdapter

/**
 * 基础 CRUD 操作示例
 *
 * 演示 [Storage] 的完整 API：增删改查、批量操作、清空等。
 */
class BasicCrudFragment : ListFragment<FragmentListBinding, ListViewModel, TextClickItem>() {

    override fun getAdapter(): BaseQuickAdapter<TextClickItem, *> = TextClickAdapter()

    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        // ========== String ==========
        TextClickItem("String: put + get") {
            Storage.putString("name", "Tom")
            val value = Storage.getString("name")
            val msg = "put(name, Tom) → get(name) = $value"
            log(msg)
            toast(msg)
        },
        TextClickItem("String: put null (删除)") {
            Storage.putString("name", "Tom")
            Storage.putString("name", null) // 等价于 remove
            val value = Storage.getString("name", "null")
            val msg = "put(null) 后读取: $value"
            log(msg)
            toast(msg)
        },

        // ========== Int / Long / Float / Boolean ==========
        TextClickItem("Int: put + get") {
            Storage.putInt("age", 25)
            val msg = "age = ${Storage.getInt("age")}"
            log(msg)
            toast(msg)
        },
        TextClickItem("Long: put + get") {
            Storage.putLong("timestamp", System.currentTimeMillis())
            val msg = "timestamp = ${Storage.getLong("timestamp")}"
            log(msg)
            toast(msg)
        },
        TextClickItem("Float: put + get") {
            Storage.putFloat("pi", 3.14159f)
            val msg = "pi = ${Storage.getFloat("pi")}"
            log(msg)
            toast(msg)
        },
        TextClickItem("Boolean: put + get") {
            Storage.putBoolean("isVip", true)
            val msg = "isVip = ${Storage.getBoolean("isVip")}"
            log(msg)
            toast(msg)
        },

        // ========== StringSet ==========
        TextClickItem("StringSet: put + get") {
            val tags = setOf("android", "kotlin", "jetpack")
            Storage.putStringSet("tags", tags)
            val result = Storage.getStringSet("tags")
            val msg = "tags = $result"
            log(msg)
            toast(msg)
        },

        // ========== 批量操作 ==========
        TextClickItem("putAll: 批量写入") {
            val map = mapOf<String, Any?>(
                "batch_name" to "Batch",
                "batch_age" to 30,
                "batch_score" to 99.5f,
            )
            Storage.putAll(map)
            val msg = "批量写入 3 个键值对"
            log(msg)
            toast(msg)
        },

        // ========== 查询 ==========
        TextClickItem("contains: 检查 key 是否存在") {
            val exists = Storage.contains("name")
            val msg = "contains('name') = $exists"
            log(msg)
            toast(msg)
        },
        TextClickItem("allKeys: 获取所有 key") {
            val keys = Storage.allKeys
            val msg = "allKeys = $keys"
            log(msg)
            toast(msg)
        },

        // ========== 删除 & 清空 ==========
        TextClickItem("remove: 删除指定 key") {
            Storage.putString("temp", "will be removed")
            Storage.remove("temp")
            val msg = "remove('temp') 后 exists = ${Storage.contains("temp")}"
            log(msg)
            toast(msg)
        },
        TextClickItem("remove: 批量删除多个 key") {
            Storage.putString("rm1", "a")
            Storage.putString("rm2", "b")
            Storage.putString("rm3", "c")
            Storage.remove("rm1", "rm2", "rm3")
            val msg = "批量删除 rm1/rm2/rm3"
            log(msg)
            toast(msg)
        },
        TextClickItem("clear: 清空所有数据") {
            Storage.clear()
            val msg = "已清空，剩余 keys = ${Storage.allKeys.size}"
            log(msg)
            toast(msg)
        },
    )

    override fun onViewCreated(view: View, androidSavedInstanceState: android.os.Bundle?) {
        super.onViewCreated(view, androidSavedInstanceState)
        // 确保有可用引擎（使用默认 SP）
    }
}
