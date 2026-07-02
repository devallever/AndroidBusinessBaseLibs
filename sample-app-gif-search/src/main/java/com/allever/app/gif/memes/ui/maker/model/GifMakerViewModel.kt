package com.allever.app.gif.memes.ui.maker.model

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.getString
import com.allever.app.gif.memes.R
import com.funny.gif.memes.event.GifMakeEvent
import com.funny.gif.memes.func.maker.GifMakeHelper
import com.funny.gif.memes.func.media.MediaBean
import app.allever.android.lib.core.util.FileUtils
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.mvvm.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.io.File

class GifMakerViewModel: BaseViewModel() {
    var mediaBean: MediaBean? = null
    var startPosition = 0
    var endPosition = 0
    val startText = MutableLiveData<String>()
    val endText = MutableLiveData<String>()
    val durationText = MutableLiveData<String>()
    val confirmClickAble = MutableLiveData<Boolean>()
    val confirmText = MutableLiveData<String>()

    override fun init() {
        super.init()
        confirmClickAble.value = true
        confirmText.value = getString(R.string.convert)
    }

    fun onClickConfirm(activity: Activity) {
        mediaBean?.let {
            viewModelScope.launch(Dispatchers.Main) {
                val fileName = it.name
                val gifName = if (fileName.contains(".")) {
                    "${fileName.split(".")[0]}_${startPosition}_${endPosition}.gif"
                } else {
                    "${fileName}_${startPosition}_${endPosition}.gif"
                }
                val toFile = "${GifMakeHelper.gifDir}${File.separator}${gifName}"
                if (FileUtils.checkExist(toFile)) {
                    log("${toFile}已存在")
                    return@launch
                }
                confirmClickAble.value = false
                confirmText.value = getString(R.string.creating)
                val result = GifMakeHelper.makeGif(App.context, it.uri?: return@launch, toFile, startPosition,  endPosition, 150) { current, total, percent->
                    val logMsg = "$percent %"
                    log(logMsg)
                    confirmText.postValue(logMsg)
                }
                log("完成：$result -> $toFile" )
                if (result) {
                    EventBus.getDefault().post(GifMakeEvent())
                    delay(1000)
                    activity.finish()
                } else  {
                    FileUtils.delete(File(toFile))
                }
                confirmClickAble.value = true
            }
        }

    }
}