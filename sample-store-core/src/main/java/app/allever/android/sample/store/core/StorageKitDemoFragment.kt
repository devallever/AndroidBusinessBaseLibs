package app.allever.android.sample.store.core

import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.core.store.StorageKit
import app.allever.android.lib.store.engine.datastore.DataStoreEngine
import com.chad.library.adapter.base.BaseQuickAdapter

/**
 * StorageKit 多实例示例
 *
 * 演示通过 [StorageKit] 创建多个隔离的存储实例，
 * 各实例数据互不干扰。
 */
class StorageKitDemoFragment : ListFragment<FragmentListBinding, ListViewModel, TextClickItem>() {

    override fun getAdapter(): BaseQuickAdapter<TextClickItem, *> = TextClickAdapter()

    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        // ========== 多实例创建与隔离 ==========
        TextClickItem("1. 创建 user 实例并写入") {
            val userStore = StorageKit.get("user_demo")
            userStore.putString("username", "Alice")
            userStore.putInt("level", 88)
            val msg = "user 实例: username=Alice, level=88"
            log(msg)
            toast(msg)
        },
        TextClickItem("2. 创建 cache 实例并写入") {
            val cacheStore = StorageKit.get("cache_demo")
            cacheStore.putString("token", "abc123xyz")
            cacheStore.putLong("expire_time", System.currentTimeMillis())
            val msg = "cache 实例: token=abc123xyz"
            log(msg)
            toast(msg)
        },
        TextClickItem("3. 验证 user 数据") {
            val userStore = StorageKit.get("user_demo")
            val msg = "user: ${userStore.getString("username")}, level=${userStore.getInt("level")}"
            log(msg)
            toast(msg)
        },
        TextClickItem("4. 验证 cache 数据") {
            val cacheStore = StorageKit.get("cache_demo")
            val msg = "cache: token=${cacheStore.getString("token")}, expire=${cacheStore.getLong("expire_time")}"
            log(msg)
            toast(msg)
        },
        TextClickItem("5. 验证隔离性: user 中无 token") {
            val userStore = StorageKit.get("user_demo")
            val hasToken = userStore.contains("token")
            val msg = "user contains('token') = $hasToken (应为 false)"
            log(msg)
            toast(msg)
        },

        // ========== 自定义引擎实例 ==========
        TextClickItem("6. 创建 DataStore 引擎的 config 实例") {
            val configStore = StorageKit.get("config_demo") { DataStoreEngine() }
            configStore.putBoolean("dark_mode", true)
            configStore.putString("language", "zh_CN")
            val msg = "config(DataStore): dark_mode=true, language=zh_CN"
            log(msg)
            toast(msg)
        },
        TextClickItem("7. 读取 config 实例") {
            val configStore = StorageKit.get("config_demo") { DataStoreEngine() }
            val msg = "config: dark=${configStore.getBoolean("dark_mode")}, lang=${configStore.getString("language")}"
            log(msg)
            toast(msg)
        },

        // ========== 释放实例 ==========
        TextClickItem("8. 释放 user 实例") {
            StorageKit.release("user_demo")
            val msg = "已释放 user_demo 实例"
            log(msg)
            toast(msg)
        },
        TextClickItem("9. 释放所有实例") {
            StorageKit.releaseAll()
            val msg = "已释放所有 StorageKit 实例"
            log(msg)
            toast(msg)
        },
    )
}
