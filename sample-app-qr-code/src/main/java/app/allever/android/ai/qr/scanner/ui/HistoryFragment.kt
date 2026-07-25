package app.allever.android.ai.qr.scanner.ui

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import app.allever.android.ai.qr.scanner.core.history.AnyItem
import app.android.base.lib.tab.TabLayout
import com.android.absbase.utils.ResourcesUtils
import com.google.zxing.client.android.history.HistoryManager
import com.allever.app.qr.code.scaner.R
import app.allever.android.ai.qr.scanner.core.history.GenerateHistoryManager
import app.allever.android.ai.qr.scanner.core.history.HistoryAdapter
import app.allever.android.ai.qr.scanner.core.history.HistoryDataModel
import app.allever.android.ai.qr.scanner.core.history.HistoryItem
import app.allever.android.ai.qr.scanner.core.preview.PreviewResultFragment
import app.allever.android.ai.qr.scanner.ui.widget.TabAndPagerHelper


class HistoryFragment : BaseFragment(), View.OnClickListener {

    data class HistoryDataItem(val type: Int, val tab: String)

    private data class HistoryData(val recyclerView: RecyclerView, val historyAdapter: HistoryAdapter, val dataModel: HistoryDataModel)

    companion object {
        internal const val HISTORY_TYPE_SCAN = 0
        internal const val HISTORY_TYPE_GENERATE = 1

        val HISTORY_DATA_ITEM_SCAN = HistoryDataItem(HISTORY_TYPE_SCAN, ResourcesUtils.getString(R.string.history_tab_scan))
        val HISTORY_DATA_ITEM_GENERATE = HistoryDataItem(HISTORY_TYPE_GENERATE, ResourcesUtils.getString(R.string.history_tab_generate))

        const val EXTRA_HISTORY_ITEM_TYPE = "hf_ehit_dkkdkkf"
        const val EXTRA_HISTORY_SHOW_TAGS = "hf_ehst_jekdkf"
    }

    private lateinit var mTabs: TabLayout
    private lateinit var mContainer: androidx.viewpager.widget.ViewPager
    private lateinit var mTabAndPagerHelper: TabAndPagerHelper
    private var mBottomToolbar: ViewGroup? = null
    private lateinit var mBottomSelectAll: CheckBox
    private lateinit var mBottomBack: ImageView
    private lateinit var mBottomDelete: ImageView

    private var mScanData: HistoryData? = null
    private var mGenerateData: HistoryData? = null
    private var mCurrentData: HistoryData? = null
        get() {
            val currentItem = mTabAndPagerHelper.currentItem()
            val historyDataItem = historyDataItems[currentItem]
            val type = historyDataItem.type
            return when (type) {
                HISTORY_TYPE_SCAN -> mScanData
                HISTORY_TYPE_GENERATE -> mGenerateData
                else -> null
            }
        }

    private var mCurrentAutoCheckedStateChange = false

    var historyDataItems = listOf<HistoryDataItem>(
            HISTORY_DATA_ITEM_SCAN,
            HISTORY_DATA_ITEM_GENERATE
    )

    var editMode: Boolean = false
        set(value) {
            field = value
            mBottomToolbar?.visibility = if (value) View.VISIBLE else View.GONE
            mCurrentData?.historyAdapter?.editMode = value
        }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_history, container, false)

        mContainer = rootView.findViewById(R.id.container)
        mTabs = rootView.findViewById(R.id.tabs)
        mTabAndPagerHelper = TabAndPagerHelper(mTabs, mContainer)

        val historyType = arguments?.getIntegerArrayList(EXTRA_HISTORY_ITEM_TYPE)
        val showTabs = arguments?.getBoolean(EXTRA_HISTORY_SHOW_TAGS, true)
        showTabs(showTabs == true)
        val tmpHistoryDataItems = historyType?.map {
            when (it) {
                HISTORY_TYPE_SCAN -> HISTORY_DATA_ITEM_SCAN
                HISTORY_TYPE_GENERATE -> HISTORY_DATA_ITEM_GENERATE
                else -> null
            }
        }?.filterNotNull()

        if (tmpHistoryDataItems != null) {
            historyDataItems = tmpHistoryDataItems
        }

        val adapter: TabAndPagerHelper.Adapter = object : TabAndPagerHelper.Adapter {
            override fun pageSelected(position: Int) {
                editMode = false
//                mCurrentData?.dataModel?.updateData()
            }

            override fun getCount(): Int {
                return historyDataItems.size
            }

            override fun getView(position: Int): View {
                val historyDataItem = historyDataItems[position]
                val type = historyDataItem.type
                val historyView: HistoryData? =
                        when (type) {
                            HISTORY_TYPE_SCAN -> {
                                var historyView = mScanData
                                if (historyView == null) {
                                    historyView = createHistoryView(HISTORY_TYPE_SCAN)
                                    mScanData = historyView
                                }
                                historyView
                            }
                            HISTORY_TYPE_GENERATE -> {
                                var historyView = mGenerateData
                                if (historyView == null) {
                                    historyView = createHistoryView(HISTORY_TYPE_GENERATE)
                                    mGenerateData = historyView
                                }
                                historyView
                            }
                            else -> null
                        }
                historyView?.dataModel?.updateData()
                return historyView?.recyclerView ?: TextView(context)
            }

            override fun getTitle(position: Int): String {
                val historyDataItem = historyDataItems[position]
                return when (historyDataItem.type) {
                    HISTORY_TYPE_SCAN -> ResourcesUtils.getString(R.string.history_tab_scan)
                    HISTORY_TYPE_GENERATE -> ResourcesUtils.getString(R.string.history_tab_generate)
                    else -> ""
                }
            }

        }

        mTabAndPagerHelper.adapter = adapter

        mBottomToolbar = rootView.findViewById(R.id.rl_bottom_toolbar)
        mBottomSelectAll = rootView.findViewById(R.id.cb_select_all)
        mBottomBack = rootView.findViewById(R.id.iv_back)
        mBottomDelete = rootView.findViewById(R.id.iv_delete)
        mBottomBack.setOnClickListener(this)
        mBottomDelete.setOnClickListener(this)
        mBottomSelectAll.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!mCurrentAutoCheckedStateChange) {
                mCurrentData?.historyAdapter?.checkedAll = isChecked
            }
            mCurrentAutoCheckedStateChange = false
        }


        editMode = false

        return rootView
    }

    override fun onPause() {
        super.onPause()
    }

    fun showTabs(show: Boolean) {
        mTabs.visibility = View.VISIBLE
    }

    private fun createHistoryView(type: Int): HistoryData {
        val recyclerView = RecyclerView(requireContext())
        recyclerView.isClickable = false
        recyclerView.clipToPadding = false
        recyclerView.scrollBarStyle = View.SCROLLBARS_OUTSIDE_OVERLAY
        recyclerView.isVerticalScrollBarEnabled = true


        val historyAdapter = HistoryAdapter(requireActivity())
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = historyAdapter
        historyAdapter.itemClickListener = object : HistoryAdapter.OnItemClickListener {

            var prevClickTime: Long = 0
            override fun onItemClick(position: Int, item: AnyItem?) {
                if (historyAdapter.editMode) {
                    val checkedAll = historyAdapter.checkedAll
                    if (checkedAll != mBottomSelectAll.isChecked) {
                        mCurrentAutoCheckedStateChange = true
                        mBottomSelectAll.isChecked = checkedAll
                    }
                    return
                }
                val currentTimeMillis = System.currentTimeMillis()
                if (currentTimeMillis - prevClickTime < 500) {
                    return
                }
            }

            override fun onItemLongClick(position: Int, item: AnyItem?): Boolean {
                if (historyAdapter.editMode) {
                    return false
                }
                editMode = true
                return true
            }

        }

        val historyManager = when (type) {
            HISTORY_TYPE_SCAN -> HistoryManager(activity)
            HISTORY_TYPE_GENERATE -> GenerateHistoryManager(
                activity
            )
            else -> HistoryManager(activity)
        }

    val dataModel = HistoryDataModel(requireActivity(), historyManager)
        dataModel.dataChangeListner = object : HistoryDataModel.OnDataChangeLisenter {
            override fun onChange() {
                val formatDatas = dataModel.getFormatDatas()
                val adIndex = if (formatDatas.size > 0) {
                    1
                } else {
                    0
                }
                runOnUiThread(Runnable {
                    historyAdapter.datas = formatDatas
                })
            }
        }

        return HistoryData(recyclerView, historyAdapter, dataModel)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (editMode) {
            editMode = false
            return true
        }
        return false
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        if (activity != null && isVisibleToUser) {
            mScanData?.dataModel?.updateData()
            mGenerateData?.dataModel?.updateData()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_delete -> {
                editMode = false
                if (mBottomSelectAll.isChecked) {
                    mCurrentData?.dataModel?.clearup(null)
                    return
                }
                val datas = mCurrentData?.historyAdapter?.datas
                if (datas != null) {
                    val deleteItems = mutableListOf<HistoryItem>()
                    for (data in datas) {
                        if (data.checked == true && data is HistoryItem) {
                            deleteItems.add(data as HistoryItem)
                        }
                    }
                    mCurrentData?.dataModel?.clearup(deleteItems)
                }
            }
            R.id.iv_back -> {
                editMode = false
            }
        }
    }
}
