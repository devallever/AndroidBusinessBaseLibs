package app.funnyappptech.android.fake.call.ui

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import app.funnyappptech.android.fake.call.R
import com.allever.lib.common.ui.widget.recycler.BaseViewHolder
import com.allever.lib.common.ui.widget.recycler.ItemListener
import app.funnyappptech.android.fake.call.base.BasePage
import app.funnyappptech.android.fake.call.data.RingtoneItem
import app.funnyappptech.android.fake.call.databinding.VcActivityRingtonePickerBinding
import app.funnyappptech.android.fake.call.ui.adapter.RingtoneAdapter
import app.funnyappptech.android.fake.call.viewmodel.RingtonePickerViewModel

class RingtonePickerPage : BasePage<VcActivityRingtonePickerBinding, RingtonePickerViewModel>(),
    View.OnClickListener {

    private lateinit var mAdapter: RingtoneAdapter
    private var mSelectedPosition = 0
    private val mRingtoneItemList = mutableListOf<RingtoneItem>()

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_left -> {
                finish()
            }
        }
    }

    override fun onDestroy() {
        mViewModel.destroy()
        super.onDestroy()
    }

    private fun refreshRingtoneList(data: MutableList<RingtoneItem>, saveIndex: Int) {
        mRingtoneItemList.clear()
        mRingtoneItemList.addAll(data)
        mAdapter.notifyDataSetChanged()
        mSelectedPosition = saveIndex
    }

    override fun inflate(): VcActivityRingtonePickerBinding {
        return VcActivityRingtonePickerBinding.inflate(layoutInflater)
    }

    override fun init() {
        mViewModel.getRingtoneData()

        findViewById<View>(R.id.iv_left).setOnClickListener(this)
        findViewById<TextView>(R.id.tv_label).text = getString(R.string.vc_ringtone_picker)

        mBinding.ringtonePickerRv.layoutManager = LinearLayoutManager(this)
        mAdapter = RingtoneAdapter(this, R.layout.vc_item_ringtone, mRingtoneItemList)
        mBinding.ringtonePickerRv.adapter = mAdapter
        mAdapter.setItemListener(object : ItemListener {
            override fun onItemClick(position: Int, holder: BaseViewHolder) {
                //播放
                val lastItem = mRingtoneItemList[mSelectedPosition]
                lastItem.checked = false
                mAdapter.notifyItemChanged(mSelectedPosition, mSelectedPosition)

                val currentItem = mRingtoneItemList[position]
                currentItem.checked = true
                mAdapter.notifyItemChanged(position, position)

                mViewModel.playRingtone(currentItem)

                mViewModel.saveRingtone(currentItem)

                mSelectedPosition = position

            }
        })

        initObserver()
    }

    private fun initObserver() {
        mViewModel.ringtoneLiveData.observe(this) {
            refreshRingtoneList(it.ringtoneItemList, it.saveIndex)
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, RingtonePickerPage::class.java)
            context.startActivity(intent)
        }
    }
}