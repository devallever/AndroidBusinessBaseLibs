package app.allever.android.sample.store.core

import androidx.fragment.app.Fragment
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.TabFragment
import app.allever.android.lib.common.TabViewModel
import app.allever.android.lib.common.adapter.TextAdapter
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import app.allever.android.lib.common.databinding.FragmentTabBinding
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.store.core.Storage
import app.allever.android.lib.store.core.engine.SPEngine
import app.allever.android.lib.store.datastore.engine.DataStoreEngine
import app.allever.android.lib.store.mmkv.engine.MMKVEngine
import com.chad.library.adapter.base.BaseQuickAdapter

class SampleStoreMainFragment: ListFragment<FragmentListBinding, ListViewModel, TextClickItem>() {
    override fun getAdapter(): BaseQuickAdapter<TextClickItem, *> = TextClickAdapter()

    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        TextClickItem("SP存储"){
            Storage.init {
                SPEngine()
            }
        },
        TextClickItem("DataStore存储"){
            Storage.init {
                DataStoreEngine()
            }
        },
        TextClickItem("MMKV存储"){
            Storage.init {
                MMKVEngine()
            }
        }
    )
}