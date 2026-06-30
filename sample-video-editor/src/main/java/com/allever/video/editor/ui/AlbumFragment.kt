package com.allever.video.editor.ui

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.android.absbase.utils.ResourcesUtils
import com.allever.video.editor.R
import com.allever.video.editor.ui.adapter.AlbumCellAdapter
import com.allever.video.editor.ui.bean.ThumbnailBean
import com.allever.video.editor.ui.widget.EmptyRecyclerView

class AlbumFragment : androidx.fragment.app.Fragment(), AlbumCellAdapter.OptionListener {

    companion object {
        private const val SPAN_COUNT = 4
        private const val MAX_COL = SPAN_COUNT
        private val TAG = AlbumFragment::class.java.simpleName
    }

    var callback: Callback? = null
    var type: TabModel.Tab? = null

    private lateinit var mRecyclerView: EmptyRecyclerView

    private var mAdapter: AlbumCellAdapter? = null
    private var mData = mutableListOf<ThumbnailBean>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_album, container, false)
        initView(view)
        return view
    }

    private fun initView(parent: View) {
        val emptyView = parent.findViewById<View>(R.id.empty_view)
        val emptyIcon = parent.findViewById<ImageView>(R.id.iv_empty_type)
        val resId = type?.emptyIconResId
        if (resId != null) {
            emptyIcon.setImageResource(resId)
        }
        mRecyclerView = parent.findViewById(R.id.fg_album_recycler_view)
        mRecyclerView.layoutManager = androidx.recyclerview.widget.GridLayoutManager(context,
            SPAN_COUNT
        )
        mRecyclerView.setEmptyView(emptyView)
        mAdapter = AlbumCellAdapter(
            this.context!!,
            R.layout.ve_item_cell,
            mData
        )
        mAdapter?.setOptionListener(this)
        mRecyclerView.adapter = mAdapter

        val spacingInPixels = ResourcesUtils.getDimension(R.dimen.item_cell_space_width).toInt()
        val firstTopSpacing = ResourcesUtils.getDimension(R.dimen.item_cell_space_top).toInt()
        val bottomSpacing = ResourcesUtils.getDimension(R.dimen.item_cell_space_width).toInt()
        val middleSpacing = ResourcesUtils.getDimension(R.dimen.item_cell_space_middle).toInt()
        mRecyclerView.addItemDecoration(object : androidx.recyclerview.widget.RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect, view: View,
                parent: androidx.recyclerview.widget.RecyclerView, state: androidx.recyclerview.widget.RecyclerView.State
            ) {
                val pos = parent.getChildLayoutPosition(view)
                if (pos / MAX_COL == 0) {
                    //设置第一行
                    outRect.top = firstTopSpacing
                }

                outRect.bottom = bottomSpacing
            }
        })

    }


    fun updateData(data: MutableList<ThumbnailBean>?) {
        data ?: return
        mData.clear()
        mData.addAll(data)
        mAdapter?.notifyDataSetChanged()
    }

    fun updateData(data: ThumbnailBean?) {
        data ?: return
        val index = mData.indexOf(data)
        mAdapter?.notifyItemChanged(index, index)
    }

    /***
     * 选中图片的回调
     */
    override fun onAlbumCellAdapterItemClick(thumbnailBean: ThumbnailBean, position: Int): Boolean {
        return callback?.onAlbumImgItemClick(thumbnailBean) ?: true
    }

    /***
     * 长按图片的回调
     */
    override fun onAlbumCellAdapterItemLongClick(thumbnailBean: ThumbnailBean, position: Int) {
        callback?.onAlbumImgItemLongClick(thumbnailBean)
    }

    public interface Callback {
        fun onAlbumImgItemClick(thumbnailBean: ThumbnailBean): Boolean
        fun onAlbumImgItemLongClick(thumbnailBean: ThumbnailBean)
    }
}