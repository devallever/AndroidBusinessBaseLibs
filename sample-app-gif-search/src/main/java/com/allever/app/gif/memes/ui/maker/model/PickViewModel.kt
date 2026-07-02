package com.allever.app.gif.memes.ui.maker.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.GridLayoutManager
import app.allever.android.lib.core.app.App
import com.funny.gif.memes.func.media.MediaHelper
import com.allever.app.gif.memes.ui.maker.GifMakerActivity
import com.allever.app.gif.memes.ui.maker.adapter.MediaItemAdapter
import com.allever.app.gif.memes.ui.maker.adapter.bean.MediaItem
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.mvvm.base.BaseViewModel
import com.allever.app.gif.memes.R
import com.allever.app.gif.memes.ui.widget.recycler.BaseViewHolder
import com.allever.app.gif.memes.ui.widget.recycler.ItemListener
import kotlinx.coroutines.launch

class PickViewModel: BaseViewModel() {
    lateinit var adapter : MediaItemAdapter

    lateinit var layoutManager: GridLayoutManager

    private val mediaItemList = mutableListOf<MediaItem>()
    val mediaItemListLiveData = MutableLiveData<List<MediaItem>>()
    var lastPosition = 0
    val confirmShow = MutableLiveData<Boolean>()
    val confirmClickAble = MutableLiveData<Boolean>()
    
    init {
        confirmClickAble.value = true
        layoutManager = GridLayoutManager(App.context, 3)
        adapter = MediaItemAdapter(App.context, R.layout.item_media, mediaItemList)
        adapter.setItemListener(object : ItemListener {
            override fun onItemClick(
                position: Int,
                holder: BaseViewHolder
            ) {
                val item = mediaItemList[position]
                if (lastPosition == position) {
                    item.selected = !item.selected
                } else {
                    mediaItemList[lastPosition].selected = false
                    adapter.notifyItemChanged(lastPosition, lastPosition)
                    item.selected = !item.selected
                    lastPosition = position
                }
                confirmShow.value = item.selected
                adapter.notifyItemChanged(position, position)
            }

            override fun onItemLongClick(position: Int, holder: BaseViewHolder): Boolean {
                val item = mediaItemList[position]
                toast(item.data?.path?:"")
                return true
            }

        })
    }

    fun fetchData() {
        viewModelScope.launch {
            val allVideo = MediaHelper.getVideoMedia(App.context, "", 0)
            allVideo.map {
                val mediaItem = MediaItem()
                mediaItem.data = it
                mediaItemList.add(mediaItem)
            }
            mediaItemListLiveData.value = mediaItemList
            adapter.notifyDataSetChanged()
        }
    }

    fun onClickConfirm() {
        val item = mediaItemList[lastPosition]
        GifMakerActivity.start(App.context, item.data?:return)
        return
    }
}