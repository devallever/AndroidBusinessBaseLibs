package com.allever.app.gif.memes.ui.maker.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.GridLayoutManager
import com.funny.gif.memes.event.GifMakeEvent
import com.funny.gif.memes.func.maker.GifMakeHelper
import com.funny.gif.memes.func.media.MediaHelper
import com.allever.app.gif.memes.ui.maker.GifMakerActivity
import com.allever.app.gif.memes.ui.maker.adapter.MediaItemAdapter
import com.allever.app.gif.memes.ui.maker.adapter.bean.MediaItem
import com.allever.lib.common.util.FileUtils
import com.allever.lib.common.util.log
import com.allever.lib.common.util.toast
import com.xm.lib.base.inters.IBaseView
import com.xm.lib.base.model.BaseViewModelKt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.io.File

class PickViewModel: BaseViewModelKt<IBaseView>() {
    lateinit var adapter : MediaItemAdapter

    lateinit var layoutManager: GridLayoutManager

    private val mediaItemList = mutableListOf<MediaItem>()
    val mediaItemListLiveData = MutableLiveData<List<MediaItem>>()
    var lastPosition = 0
    val confirmShow = MutableLiveData<Boolean>()
    val confirmClickAble = MutableLiveData<Boolean>()

    override fun onCreated() {
        confirmClickAble.value = true
        layoutManager = GridLayoutManager(mCxt, 3)
        adapter = MediaItemAdapter()
        adapter.setOnItemClickedListener { v, position, item ->
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

        adapter.setOnItemLongClickedListener{ v, position, item ->
            toast(item?.data?.path?:"")
            return@setOnItemLongClickedListener true
        }

        viewModelScope.launch {
            val allVideo = MediaHelper.getVideoMedia(mCxt, "", 0)
            allVideo.map {
                val mediaItem = MediaItem()
                mediaItem.data = it
                mediaItemList.add(mediaItem)
            }
            mediaItemListLiveData.value = mediaItemList
        }
    }

    fun onClickConfirm() {
        val item = mediaItemList[lastPosition]
        GifMakerActivity.start(mCxt, item.data?:return)
        return
    }
}