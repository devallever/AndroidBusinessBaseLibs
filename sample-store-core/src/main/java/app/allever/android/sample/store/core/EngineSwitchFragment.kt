package app.allever.android.sample.store.core

import android.os.Bundle
import android.view.View
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.core.store.IStoreEngine
import app.allever.android.lib.core.store.StoreCore
import app.allever.android.lib.core.store.engine.SPEngine
import app.allever.android.lib.store.engine.datastore.DataStoreEngine
import app.allever.android.lib.store.engine.mmkv.MMKVEngine
import com.chad.library.adapter.base.BaseQuickAdapter

/**
 * 引擎切换示例
 *
 * 演示通过 [StoreCore.init] 一行代码无缝切换底层存储引擎。
 */
class EngineSwitchFragment : ListFragment<FragmentListBinding, ListViewModel, TextClickItem>() {

    override fun getAdapter(): BaseQuickAdapter<TextClickItem, *> = TextClickAdapter()

    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        // ========== 切换引擎 ==========
        TextClickItem("1. 切换为 SP (SharedPreferences)") {
            switchEngine("SP") { SPEngine() }
        },
        TextClickItem("2. 切换为 DataStore") {
            switchEngine("DataStore") { DataStoreEngine() }
        },
        TextClickItem("3. 切换为 MMKV") {
            switchEngine("MMKV") { MMKVEngine() }
        },

        // ========== 验证：写入后切换引擎，数据隔离 ==========
        TextClickItem("4. 写入测试数据 (key=test, value=hello)") {
            StoreCore.putString("test", "hello")
            val msg = "已写入: test=hello"
            log(msg)
            toast(msg)
        },
        TextClickItem("5. 读取测试数据") {
            val value = StoreCore.getString("test", "未找到")
            val msg = "读取结果: $value"
            log(msg)
            toast(msg)
        },

        // ========== 其他类型操作 ==========
        TextClickItem("6. 写入多种类型数据") {
            StoreCore.putInt("int_key", 42)
            StoreCore.putLong("long_key", 123456789L)
            StoreCore.putFloat("float_key", 3.14f)
            StoreCore.putBoolean("bool_key", true)
            val msg = "已写入 int/long/float/boolean"
            log(msg)
            toast(msg)
        },
        TextClickItem("7. 读取所有类型数据") {
            val sb = StringBuilder().apply {
                appendLine("int=${StoreCore.getInt("int_key")}")
                appendLine("long=${StoreCore.getLong("long_key")}")
                appendLine("float=${StoreCore.getFloat("float_key")}")
                appendLine("bool=${StoreCore.getBoolean("bool_key")}")
                appendLine("---")
                appendLine("allKeys=${StoreCore.allKeys}")
            }
            log(sb.toString())
            toast(sb.toString())
        },
    )

    private fun switchEngine(name: String, factory: () -> Any) {
        @Suppress("UNCHECKED_CAST")
        StoreCore.init(factory as () -> IStoreEngine)
        val msg = "已切换引擎: $name"
        log(msg)
        toast(msg)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 默认使用 SP
        StoreCore.init { SPEngine() }
    }
}
