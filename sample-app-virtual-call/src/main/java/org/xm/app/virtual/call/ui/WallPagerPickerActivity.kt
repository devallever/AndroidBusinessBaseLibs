package org.xm.app.virtual.call.ui

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.allever.app.virtual.call.R
import com.allever.lib.ad.chain.AdChainHelper
import com.allever.lib.ad.chain.AdChainListener
import com.allever.lib.ad.chain.IAd
import com.allever.lib.common.ui.widget.recycler.BaseViewHolder
import com.allever.lib.common.ui.widget.recycler.ItemListener
import kotlinx.android.synthetic.main.activity_wall_pager_picker.*
import kotlinx.android.synthetic.main.include_top_bar.*
import org.xm.app.virtual.call.ad.AdContract
import org.xm.app.virtual.call.app.BaseActivity
import org.xm.app.virtual.call.bean.WallPagerItem
import org.xm.app.virtual.call.ui.adapter.WallPagerAdapter
import org.xm.app.virtual.call.ui.mvp.presenter.WallPagerPickerPresenter
import org.xm.app.virtual.call.ui.mvp.view.WallPagerPickerView

class WallPagerPickerActivity : BaseActivity<WallPagerPickerView, WallPagerPickerPresenter>(),
    WallPagerPickerView,
    View.OnClickListener {

    private lateinit var mAdapter: WallPagerAdapter
    private lateinit var mRv: RecyclerView
    private var mSelectedPosition = 0
    private val mWallPagerItemList = mutableListOf<WallPagerItem>()


    override fun getContentView(): Any = R.layout.activity_wall_pager_picker

    override fun initView() {
        //判断是否有刘海屏幕
        checkNotch(Runnable {
            val statusBarViewId = addStatusBar(rootLayout)
            if (rootLayout is RelativeLayout) {
                val topBar = top_bar.layoutParams as? RelativeLayout.LayoutParams
                topBar?.addRule(RelativeLayout.BELOW, statusBarViewId.id)
            }
        })

        findViewById<View>(R.id.iv_left).setOnClickListener(this)
        findViewById<TextView>(R.id.tv_label).text = getString(R.string.wall_pager_picker)

        mRv = findViewById(R.id.wall_pager_picker_rv)
        mRv.layoutManager = GridLayoutManager(this, 2)
        mAdapter = WallPagerAdapter(this, R.layout.item_wall_pager, mWallPagerItemList)
        mRv.adapter = mAdapter
        mAdapter.setItemListener(object : ItemListener {
            override fun onItemClick(position: Int, holder: BaseViewHolder) {
                val lastItem = mWallPagerItemList[mSelectedPosition]
                lastItem.checked = false
                mAdapter.notifyItemChanged(mSelectedPosition, mSelectedPosition)

                val currentItem = mWallPagerItemList[position]
                currentItem.checked = true
                mAdapter.notifyItemChanged(position, position)

                mPresenter.saveWallPager(currentItem)

                mSelectedPosition = position

                mNeedShowInsertAd = true
            }
        })
    }

    override fun initData() {
        mPresenter.getWallPagerData()
        loadSettingInsert()
    }

    override fun createPresenter(): WallPagerPickerPresenter = WallPagerPickerPresenter()

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_left -> {
                if (mNeedShowInsertAd && mSettingInsertAd != null) {
                    mSettingInsertAd?.show()
                } else {
                    finish()
                }
            }
        }
    }

    override fun refreshWallPagerList(data: MutableList<WallPagerItem>, saveIndex: Int) {
        mWallPagerItemList.clear()
        mWallPagerItemList.addAll(data)
        mAdapter.notifyDataSetChanged()
        mSelectedPosition = saveIndex
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, WallPagerPickerActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onDestroy() {
        mSettingInsertAd?.destroy()
        super.onDestroy()
    }

    private var mSettingInsertAd: IAd? = null
    override fun onBackPressed() {
        if (mNeedShowInsertAd && mSettingInsertAd != null) {
            mSettingInsertAd?.show()
            return
        }
        super.onBackPressed()
    }

    private var mNeedShowInsertAd = false
    private fun loadSettingInsert() {
        AdChainHelper.loadAd(
            AdContract.AD_NAME_SETTING_INSERT,
            window.decorView as ViewGroup,
            object :
                AdChainListener {
                override fun onLoaded(ad: IAd?) {
                    mSettingInsertAd = ad
                }

                override fun onShowed() {}
                override fun onDismiss() {
                    if (!isFinishing) {
                        mHandler.postDelayed({
                            finish()
                        }, 300)
                    }
                }

                override fun onFailed(msg: String) {}
            })
    }

}