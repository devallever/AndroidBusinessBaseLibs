package org.xm.app.virtual.call.ui

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.allever.app.virtual.call.R
import com.allever.lib.ad.chain.AdChainHelper
import com.allever.lib.ad.chain.AdChainListener
import com.allever.lib.ad.chain.IAd
import com.allever.lib.common.ui.widget.recycler.BaseViewHolder
import com.allever.lib.common.ui.widget.recycler.ItemListener
import com.android.absbase.App
import kotlinx.android.synthetic.main.activity_ringtone_picker.*
import kotlinx.android.synthetic.main.include_top_bar.*
import org.xm.app.virtual.call.ad.AdContract
import org.xm.app.virtual.call.app.BaseActivity
import org.xm.app.virtual.call.bean.RingtoneItem
import org.xm.app.virtual.call.ui.adapter.RingtoneAdapter
import org.xm.app.virtual.call.ui.mvp.presenter.RingtonePickerPresenter
import org.xm.app.virtual.call.ui.mvp.view.RingtonePickerView

class RingtonePickerActivity : BaseActivity<RingtonePickerView, RingtonePickerPresenter>(),
    RingtonePickerView,
    View.OnClickListener {

    private lateinit var mAdapter: RingtoneAdapter
    private lateinit var mRv: RecyclerView
    private var mSelectedPosition = 0
    private val mRingtoneItemList = mutableListOf<RingtoneItem>()

    override fun getContentView(): Any = R.layout.activity_ringtone_picker

    override fun initView() {
        App
        //判断是否有刘海屏幕
        checkNotch(Runnable {
            val statusBarViewId = addStatusBar(rootLayout)
            if (rootLayout is RelativeLayout) {
                val topBar = top_bar.layoutParams as? RelativeLayout.LayoutParams
                topBar?.addRule(RelativeLayout.BELOW, statusBarViewId.id)
            }
        })

        findViewById<View>(R.id.iv_left).setOnClickListener(this)
        findViewById<TextView>(R.id.tv_label).text = getString(R.string.ringtone_picker)

        mRv = findViewById(R.id.ringtone_picker_rv)
        mRv.layoutManager = LinearLayoutManager(this)
        mAdapter = RingtoneAdapter(this, R.layout.item_ringtone, mRingtoneItemList)
        mRv.adapter = mAdapter
        mAdapter.setItemListener(object : ItemListener {
            override fun onItemClick(position: Int, holder: BaseViewHolder) {
                //播放
                val lastItem = mRingtoneItemList[mSelectedPosition]
                lastItem.checked = false
                mAdapter.notifyItemChanged(mSelectedPosition, mSelectedPosition)

                val currentItem = mRingtoneItemList[position]
                currentItem.checked = true
                mAdapter.notifyItemChanged(position, position)

                mPresenter.playRingtone(currentItem)

                mPresenter.saveRingtone(currentItem)

                mSelectedPosition = position

                mNeedShowInsertAd = true
            }
        })
    }

    override fun initData() {
        mPresenter.getRingtoneData()
        loadSettingInsert()
    }

    override fun createPresenter(): RingtonePickerPresenter = RingtonePickerPresenter()

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

    override fun onDestroy() {
        mPresenter.destroy()
        mSettingInsertAd?.destroy()
        super.onDestroy()
    }

    override fun refreshRingtoneList(data: MutableList<RingtoneItem>, saveIndex: Int) {
        mRingtoneItemList.clear()
        mRingtoneItemList.addAll(data)
        mAdapter.notifyDataSetChanged()
        mSelectedPosition = saveIndex
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

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, RingtonePickerActivity::class.java)
            context.startActivity(intent)
        }
    }
}