package com.allever.app.gif.memes.ui.main

import android.Manifest
import android.app.ProgressDialog
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.allever.app.gif.memes.R
import com.funny.gif.memes.app.Global
import com.funny.gif.memes.bean.event.DownloadFinishEvent
import com.funny.gif.memes.bean.event.LikeEvent
import com.funny.gif.memes.bean.event.RemoveLikeListEvent
import com.allever.app.gif.memes.databinding.GsFragmentTrendBinding
import com.funny.gif.memes.func.store.Repository
import com.funny.gif.memes.func.store.Store
import com.funny.gif.memes.func.store.Version
import com.allever.app.gif.memes.ui.adapter.GifAdapter
import com.allever.app.gif.memes.ui.main.model.TrendViewModel
import com.allever.app.gif.memes.ui.widget.RecyclerViewScrollListener
import com.funny.gif.memes.util.SpUtils
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.core.helper.CoroutineHelper
import app.allever.android.lib.mvvm.base.BaseMvvmFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class TrendFragment : BaseMvvmFragment<GsFragmentTrendBinding, TrendViewModel>(){

    private lateinit var mProgressDialog: ProgressDialog
    private lateinit var recyclerViewScrollListener: RecyclerViewScrollListener

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
    }

    private fun requestPermission() {
        Global.createDir()
        getData()
    }

    private fun getData(isLoadMore: Boolean = false) {
        CoroutineHelper.MAIN.launch {
            val count = Global.SHOW_COUNT
            var offset = SpUtils.getString(Global.SP_OFFSET, "0")
            log("offset = $offset")

            showLoadingProgressDialog(getString(R.string.loading))
            val gifItemList = Repository.getGifItemList(offset)
            hideLoadingProgressDialog()

            delay(500)

            recyclerViewScrollListener.setLoadDataStatus(false)
            mBinding.gifRecyclerView.visibility = View.VISIBLE
            mBinding.ivRetry.visibility = View.GONE

            if (!isLoadMore) {
                mViewModel.gifDataList.clear()
            }

            mViewModel.gifDataList.addAll(gifItemList)

            mViewModel.adapter.notifyDataSetChanged()

            offset = if (Store.getVersion() == Version.INTERNATIONAL) {
                if (mViewModel.gifDataList.size < count) {
                    "0"
                } else {
                    (offset.toInt() + count + 1).toString()
                }
            } else {
                if (gifItemList.isEmpty()) {
                    "0"
                } else {
                    gifItemList.last().id
                }
            }

            SpUtils.putString(Global.SP_OFFSET, offset)

            //失败
            if (gifItemList.isEmpty()) {
                recyclerViewScrollListener.setLoadDataStatus(false)
                if (!isLoadMore) {
                    mBinding.gifRecyclerView.visibility = View.GONE
                    mBinding.ivRetry.visibility = View.VISIBLE
                }
            }

        }
    }

    private fun showLoadingProgressDialog(msg: String) {
        if (!mProgressDialog.isShowing) {
            mProgressDialog.setMessage(msg)
            mProgressDialog.show()
        }
    }

    private fun hideLoadingProgressDialog() {
        if (mProgressDialog.isShowing) {
            mProgressDialog.dismiss()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLikeUpdate(likeEvent: LikeEvent) {
        val position = Global.getIndex(likeEvent.id, mViewModel.gifDataList)
        mViewModel.adapter.notifyItemChanged(position, position)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDownloadFinishEvent(event: DownloadFinishEvent) {
        val position = Global.getIndex(event.id, mViewModel.gifDataList)
        mViewModel.adapter.notifyItemChanged(position, position)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRemoveLikeLietEvent(event: RemoveLikeListEvent) {
        event.gifIdList.map {
            val position = Global.getIndex(it, mViewModel.gifDataList)
            mViewModel.adapter.notifyItemChanged(position, position)
        }
    }

    override fun inflate(): GsFragmentTrendBinding = GsFragmentTrendBinding.inflate(layoutInflater)

    override fun init() {

        EventBus.getDefault().register(this)
        mProgressDialog = ProgressDialog(activity)

        mBinding.ivRetry.setOnClickListener {
            requestPermission()
            mBinding.ivRetry.visibility = View.GONE
        }

        mViewModel.adapter = GifAdapter(requireContext(), R.layout.item_gif, mViewModel.gifDataList)

        recyclerViewScrollListener = RecyclerViewScrollListener(object :
            RecyclerViewScrollListener.OnRecycleRefreshListener {
            override fun refresh() {

            }

            override fun loadMore() {
                getData(true)
            }
        })


        mBinding.gifRecyclerView.addOnScrollListener(recyclerViewScrollListener)

        mBinding.gifRecyclerView.layoutManager = LinearLayoutManager(context)
        val pagerSnapHelper = PagerSnapHelper()
        pagerSnapHelper.attachToRecyclerView(mBinding.gifRecyclerView)
        mBinding.gifRecyclerView.adapter = mViewModel.adapter

        requestPermission()
    }
}