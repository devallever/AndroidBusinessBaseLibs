package app.allever.android.sample.store.core

import android.view.View
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.store.core.StoreCore
import com.chad.library.adapter.base.BaseQuickAdapter

/**
 * 基础 CRUD 操作示例
 *
 * 演示 [StoreCore] 的完整 API：增删改查、批量操作、清空等。
 */
class BasicCrudFragment : ListFragment<FragmentListBinding, ListViewModel, TextClickItem>() {

    override fun getAdapter(): BaseQuickAdapter<TextClickItem, *> = TextClickAdapter()

    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        // ========== String ==========
        TextClickItem("String: put + get") {
            StoreCore.putString("name", "Tom")
            val value = StoreCore.getString("name")
            val msg = "put(name, Tom) → get(name) = $value"
            log(msg)
            toast(msg)
        },
        TextClickItem("String: put null (删除)") {
            StoreCore.putString("name", "Tom")
            StoreCore.putString("name", null) // 等价于 remove
            val value = StoreCore.getString("name", "null")
            val msg = "put(null) 后读取: $value"
            log(msg)
            toast(msg)
        },

        // ========== Int / Long / Float / Boolean ==========
        TextClickItem("Int: put + get") {
            StoreCore.putInt("age", 25)
            val msg = "age = ${StoreCore.getInt("age")}"
            log(msg)
            toast(msg)
        },
        TextClickItem("Long: put + get") {
            StoreCore.putLong("timestamp", System.currentTimeMillis())
            val msg = "timestamp = ${StoreCore.getLong("timestamp")}"
            log(msg)
            toast(msg)
        },
        TextClickItem("Float: put + get") {
            StoreCore.putFloat("pi", 3.14159f)
            val msg = "pi = ${StoreCore.getFloat("pi")}"
            log(msg)
            toast(msg)
        },
        TextClickItem("Boolean: put + get") {
            StoreCore.putBoolean("isVip", true)
            val msg = "isVip = ${StoreCore.getBoolean("isVip")}"
            log(msg)
            toast(msg)
        },

        // ========== StringSet ==========
        TextClickItem("StringSet: put + get") {
            val tags = setOf("android", "kotlin", "jetpack")
            StoreCore.putStringSet("tags", tags)
            val result = StoreCore.getStringSet("tags")
            val msg = "tags = $result"
            log(msg)
            toast(msg)
        },

        // ========== Object (JSON) ==========
        TextClickItem("Object: put + get (Class)") {
            val user = User("Tom", 25, true)
            StoreCore.putObject("user_obj", user)
            val restored = StoreCore.getObject("user_obj", User::class.java)
            val msg = "putObject → getObject: $restored"
            log(msg)
            toast(msg)
        },
        TextClickItem("Object: get (reified)") {
            val user: User? = StoreCore.getObject<User>("user_obj")
            val msg = "getObject<User>: name=${user?.name}, age=${user?.age}"
            log(msg)
            toast(msg)
        },
        TextClickItem("Object: put null (删除)") {
            StoreCore.putObject("user_obj", null)
            val user = StoreCore.getObject("user_obj", User::class.java)
            val msg = "putObject(null) 后读取: $user"
            log(msg)
            toast(msg)
        },
        TextClickItem("Object: List 存储") {
            val list = listOf(
                User("Alice", 20, false),
                User("Bob", 28, true),
                User("Carol", 35, false),
            )
            StoreCore.putObject("user_list", list)
            // List 需要用 TypeToken 方式反序列化，这里用 String 展示
            val raw = StoreCore.getString("user_list")
            val msg = "List JSON: ${raw?.take(80)}..."
            log(msg)
            toast(msg)
        },
        TextClickItem("Object: Map 存储") {
            val config = mapOf(
                "theme" to "dark",
                "language" to "zh_CN",
                "version" to 100,
            )
            StoreCore.putObject("config_map", config)
            val raw = StoreCore.getString("config_map")
            val msg = "Map JSON: ${raw?.take(100)}"
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
            StoreCore.putAll(map)
            val msg = "批量写入 3 个键值对"
            log(msg)
            toast(msg)
        },

        // ========== 查询 ==========
        TextClickItem("contains: 检查 key 是否存在") {
            val exists = StoreCore.contains("name")
            val msg = "contains('name') = $exists"
            log(msg)
            toast(msg)
        },
        TextClickItem("allKeys: 获取所有 key") {
            val keys = StoreCore.allKeys
            val msg = "allKeys = $keys"
            log(msg)
            toast(msg)
        },

        // ========== 删除 & 清空 ==========
        TextClickItem("remove: 删除指定 key") {
            StoreCore.putString("temp", "will be removed")
            StoreCore.remove("temp")
            val msg = "remove('temp') 后 exists = ${StoreCore.contains("temp")}"
            log(msg)
            toast(msg)
        },
        TextClickItem("remove: 批量删除多个 key") {
            StoreCore.putString("rm1", "a")
            StoreCore.putString("rm2", "b")
            StoreCore.putString("rm3", "c")
            StoreCore.remove("rm1", "rm2", "rm3")
            val msg = "批量删除 rm1/rm2/rm3"
            log(msg)
            toast(msg)
        },
        TextClickItem("clear: 清空所有数据") {
            StoreCore.clear()
            val msg = "已清空，剩余 keys = ${StoreCore.allKeys.size}"
            log(msg)
            toast(msg)
        },
    )

    override fun onViewCreated(view: View, androidSavedInstanceState: android.os.Bundle?) {
        super.onViewCreated(view, androidSavedInstanceState)
        // 确保有可用引擎（使用默认 SP）
    }
}

/** 对象存储示例数据类 */
data class User(val name: String, val age: Int, val isVip: Boolean)
