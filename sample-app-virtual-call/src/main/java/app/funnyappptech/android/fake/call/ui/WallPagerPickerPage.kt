package app.funnyappptech.android.fake.call.ui

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import app.funnyappptech.android.fake.call.R
import com.allever.lib.common.ui.widget.recycler.BaseViewHolder
import com.allever.lib.common.ui.widget.recycler.ItemListener
import app.funnyappptech.android.fake.call.base.BasePage
import app.funnyappptech.android.fake.call.data.WallPagerItem
import app.funnyappptech.android.fake.call.databinding.VcActivityWallPagerPickerBinding
import app.funnyappptech.android.fake.call.ui.adapter.WallPagerAdapter
import app.funnyappptech.android.fake.call.viewmodel.WallPagerPickerViewModel

class WallPagerPickerPage : BasePage<VcActivityWallPagerPickerBinding, WallPagerPickerViewModel>(),
    View.OnClickListener {

    private lateinit var mAdapter: WallPagerAdapter
    private var mSelectedPosition = 0
    private val mWallPagerItemList = mutableListOf<WallPagerItem>()

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_left -> {
                finish()
            }
        }
    }

    private fun refreshWallPagerList(data: MutableList<WallPagerItem>, saveIndex: Int) {
        mWallPagerItemList.clear()
        mWallPagerItemList.addAll(data)
        mAdapter.notifyDataSetChanged()
        mSelectedPosition = saveIndex
    }

    override fun inflate(): VcActivityWallPagerPickerBinding {
        return VcActivityWallPagerPickerBinding.inflate(layoutInflater)
    }

    private fun initObserver() {
        mViewModel.refreshWallPagerListLiveData.observe(this) {
            refreshWallPagerList(it.list as MutableList<WallPagerItem>, it.saveRingtoneItemPosition)
        }
    }

    override fun init() {
        mViewModel.getWallPagerData()

        findViewById<View>(R.id.iv_left).setOnClickListener(this)
        findViewById<TextView>(R.id.tv_label).text = getString(R.string.vc_wall_pager_picker)

        mBinding.wallPagerPickerRv.layoutManager = GridLayoutManager(this, 2)
        mAdapter = WallPagerAdapter(this, R.layout.vc_item_wall_pager, mWallPagerItemList)
        mBinding.wallPagerPickerRv.adapter = mAdapter
        mAdapter.setItemListener(object : ItemListener {
            override fun onItemClick(position: Int, holder: BaseViewHolder) {
                val lastItem = mWallPagerItemList[mSelectedPosition]
                lastItem.checked = false
                mAdapter.notifyItemChanged(mSelectedPosition, mSelectedPosition)

                val currentItem = mWallPagerItemList[position]
                currentItem.checked = true
                mAdapter.notifyItemChanged(position, position)

                 mViewModel.saveWallPager(currentItem)

                mSelectedPosition = position
            }
        })

        initObserver()
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, WallPagerPickerPage::class.java)
            context.startActivity(intent)
        }
    }
}