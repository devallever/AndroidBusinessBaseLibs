package com.allever.app.gif.memes.ui.search

import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.allever.app.gif.memes.R
import com.funny.gif.memes.app.Global
import com.funny.gif.memes.bean.event.DownloadFinishEvent
import com.funny.gif.memes.bean.event.LikeEvent
import com.funny.gif.memes.bean.event.RemoveLikeListEvent
import com.allever.app.gif.memes.databinding.FragmentSearchBinding
import com.funny.gif.memes.func.download.DownloadManager
import com.funny.gif.memes.func.store.Repository
import com.funny.gif.memes.func.store.Store
import com.funny.gif.memes.func.store.Version
import com.allever.app.gif.memes.ui.ViewHelper
import com.allever.app.gif.memes.ui.adapter.GifAdapter
import com.allever.app.gif.memes.ui.search.model.SearchViewModel
import com.allever.app.gif.memes.ui.widget.RecyclerViewScrollListener
import com.funny.gif.memes.util.ImageLoader
import com.funny.gif.memes.util.SpUtils
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.core.util.BarUtils
import app.allever.android.lib.mvvm.base.BaseMvvmFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SearchFragment : BaseMvvmFragment<FragmentSearchBinding, SearchViewModel>() {

    private var mAdapter: GifAdapter? = null
    private lateinit var mProgressDialog: ProgressDialog
    private lateinit var recyclerViewScrollListener: RecyclerViewScrollListener

    private lateinit var mKeyword: String

    override fun onDestroyView() {
        super.onDestroyView()
        DownloadManager.getInstance().cancelAllTask()
        ImageLoader.clearMemoryCache()
        EventBus.getDefault().unregister(this)
    }

    private fun search(keyword: String, isLoadMore: Boolean = false) {
        if (keyword == "") {
            toast(getString(R.string.please_input_search_content))
            return
        }

        val keyword = keyword
//        if (!BuildConfig.DEBUG) {
//            keyword = keyword.replace("sexy", "")
//            keyword = keyword.replace("sex", "")
//        }

        mKeyword = keyword
        val count = Global.SHOW_COUNT
        var offset = SpUtils.getString(Global.SP_SEARCH_OFFSET, "0")
        log("offset = $offset")
        showLoadingProgressDialog(getString(R.string.searching))
        mViewModel.viewModelScope.launch(Dispatchers.Main) {
            val gifItemList = Repository.search(keyword, offset)
            hideLoadingProgressDialog()
            mBinding.ivRetry.visibility = View.VISIBLE
//            if (mDetailInsertAd != null) {
//                HandlerHelper.mainHandler.postDelayed({
//                    toast(R.string.loading_ad_tips)
//                    mDetailInsertAd?.show()
//                }, 200)
//            }

            recyclerViewScrollListener.setLoadDataStatus(false)
            mBinding.gifRecyclerView.visibility = View.VISIBLE
            mBinding.ivRetry.visibility = View.GONE

            if (!isLoadMore) {
                mViewModel.gifDataList.clear()
            }

            mViewModel.gifDataList.addAll(gifItemList)

            mAdapter?.notifyDataSetChanged()

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

            SpUtils.putString(Global.SP_SEARCH_OFFSET, offset)

//            loadDetailInsert()

            if (gifItemList.isEmpty()) {
                recyclerViewScrollListener.setLoadDataStatus(false)
                if (!isLoadMore) {
                    mBinding.gifRecyclerView.visibility = View.GONE
                    mBinding.ivRetry.visibility = View.VISIBLE
                }
            }
        }
    }

//    private fun loadDetailInsert() {
//        AdChainHelper.loadAd(AdConstants.AD_NAME_DETAIL_INSERT, null, object : SimpleAdChainListener {
//            override fun onLoaded(ad: IAd?) {
//                mDetailInsertAd = ad
//            }
//        })
//    }

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
        if (!userVisibleHint) {
            val position = Global.getIndex(likeEvent.id, mViewModel.gifDataList)
            mAdapter?.notifyItemChanged(position, position)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDownloadFinishEvent(event: DownloadFinishEvent) {
        if (!userVisibleHint) {
            val position = Global.getIndex(event.id, mViewModel.gifDataList)
            mAdapter?.notifyItemChanged(position, position)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRemoveLikeLietEvent(event: RemoveLikeListEvent) {
        if (!userVisibleHint) {
            event.gifIdList.map {
                val position = Global.getIndex(it, mViewModel.gifDataList)
                mAdapter?.notifyItemChanged(position, position)
            }
        }
    }

    override fun inflate(): FragmentSearchBinding = FragmentSearchBinding.inflate(layoutInflater)

    override fun init() {
        EventBus.getDefault().register(this)

        ViewHelper.setMarginTop(mBinding.searchViewContainer, BarUtils.getStatusBarHeight())

        mKeyword = arguments?.getString(EXTRA_KEY_WORD) ?: ""

        mProgressDialog = ProgressDialog(activity)

        mBinding.ivRetry.setOnClickListener {
            mBinding.ivRetry.visibility = View.GONE
            search(mKeyword)
        }

        mBinding.searchView.setText(mKeyword)
        mBinding.searchView.setSelection(mKeyword.length)
        mBinding.searchView.addSearchListener {
            search(it)
        }

        mAdapter = GifAdapter(requireContext(), R.layout.item_gif, mViewModel.gifDataList)

        recyclerViewScrollListener = RecyclerViewScrollListener(object :
            RecyclerViewScrollListener.OnRecycleRefreshListener {
            override fun refresh() {

            }

            override fun loadMore() {
                showLoadingProgressDialog(getString(R.string.searching))
                search(mKeyword, true)
            }
        })


        mBinding.gifRecyclerView.addOnScrollListener(recyclerViewScrollListener)

        mBinding.gifRecyclerView.layoutManager = LinearLayoutManager(context)
        val pagerSnapHelper = PagerSnapHelper()
        pagerSnapHelper.attachToRecyclerView(mBinding.gifRecyclerView)
        mBinding.gifRecyclerView.adapter = mAdapter

        if (mKeyword != "") {
            search(mKeyword)
        }
    }

    companion object {

        private const val EXTRA_KEY_WORD = "EXTRA_KEY_WORD"

        fun newInstance(keyword: String): SearchFragment {
            val fragment = SearchFragment()
            val bundle = Bundle()
            bundle.putString(EXTRA_KEY_WORD, keyword)
            fragment.arguments = bundle
            return fragment
        }
    }
}