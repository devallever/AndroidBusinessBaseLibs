package com.clean.wood.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clean.wood.R
import com.clean.wood.WoodApp
import com.clean.wood.data.AdManager
import com.clean.wood.data.JunkManager
import com.clean.wood.data.model.JunkItem
import com.clean.wood.ui.adapter.JunkItemAdapter
import com.clean.wood.utils.Constant
import com.clean.wood.utils.StorageUtils
import com.clean.wood.utils.log
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ScanJunkViewModel : ViewModel() {

    val progressLiveData = MutableLiveData(0)
    private var finishFlag = false

    var scanning = true
    val junkSizeLiveData = MutableLiveData<String>()
    val junkList by lazy {
        mutableListOf<JunkItem>().apply {
            add(
                JunkItem(
                    Constant.JunkType.SystemCache,
                    R.drawable.ic_junk_system_cache,
                    WoodApp.context.getString(R.string.junk_type_system_cache)
                )
            )
            add(
                JunkItem(
                    Constant.JunkType.Residual,
                    R.drawable.ic_junk_residual,
                    WoodApp.context.getString(R.string.junk_type_residual)
                )
            )
            add(
                JunkItem(
                    Constant.JunkType.Ad,
                    R.drawable.ic_junk_ad,
                    WoodApp.context.getString(R.string.junk_type_ad)
                )
            )
            add(
                JunkItem(
                    Constant.JunkType.ObsoleteApk,
                    R.drawable.ic_junk_apk,
                    WoodApp.context.getString(R.string.junk_type_apk)
                )
            )
            add(
                JunkItem(
                    Constant.JunkType.Temp,
                    R.drawable.ic_junk_temp_file,
                    WoodApp.context.getString(R.string.junk_type_temp_file)
                )
            )
            add(
                JunkItem(
                    Constant.JunkType.Thumb,
                    R.drawable.ic_junk_thumb_pic,
                    WoodApp.context.getString(R.string.junk_type_thumb_pic)
                )
            )
        }
    }
    val adapter by lazy {
        JunkItemAdapter(junkList).apply {
            itemClickListener = object : JunkItemAdapter.ItemClickListener {
                override fun onItemClick(item: JunkItem) {
                    updateSelectedJunkSize()
                }
            }
        }
    }

    init {
        WoodApp.junkSize.observeForever {
            val sizeDisplay = StorageUtils.convertBytesToMBOrGB((it * 1024).toLong())
            log("sizeDisplay = $sizeDisplay")
            junkSizeLiveData.postValue(sizeDisplay)
        }
    }

    fun startProgress() {
        var step = 15
        var delayTime = 100L
        viewModelScope.launch {
            while (progressLiveData.value!! < 95 && !finishFlag) {
                val progress = progressLiveData.value!! + 1
                progressLiveData.postValue(progress)
                if (progress == step) {
                    junkList.forEach {
                        val index = junkList.indexOf(it)
                        if (it.scanning && progress == step && index != junkList.size - 1) {
                            step += 15
                            it.scanning = false
                            it.select = true
                            adapter.notifyItemChanged(index, index)
                            log("notifyItemChanged: $index")
                            return@forEach
                        }
                    }
                }
                if (progress < 50) {
                    delayTime = 50
                } else if (progress < 70) {
                    delayTime = 100
                } else {
                    delayTime = 200
                }
                delay(delayTime)
            }
        }
    }

    fun finishProgress() {
        finishFlag = true
        viewModelScope.launch {
            while (progressLiveData.value!! < 100) {
                val progress = progressLiveData.value!! + 1
                progressLiveData.postValue(progress)
                delay(10)
                junkList.forEach {
                    if (it.scanning) {
                        val index = junkList.indexOf(it)
                        it.scanning = false
                        it.select = true
                        adapter.notifyItemChanged(index, index)
                    }
                }
            }
        }
    }

    fun checkAd() {
        AdManager.ins.checkAd(Constant.AdPosition.ResultNative)

        AdManager.ins.checkAd(Constant.AdPosition.ScanningInter)
        AdManager.ins.checkAd(Constant.AdPosition.ExitInter)
    }

    fun showScanningAd() {
        viewModelScope.launch {
            AdManager.ins.showInterAd(Constant.AdPosition.ScanningInter)
        }
    }

    fun updateSelectedJunkSize() {
        var selectSize = 0.0
        adapter.selectTypeList().forEach {
            JunkManager.ins.junkCache[it]?.let { junkInfoList->
                junkInfoList.forEach { junkInfo->
                    selectSize += junkInfo.size
                }
            }
        }

        val sizeDisplay = StorageUtils.convertBytesToMBOrGB((selectSize).toLong())
//                    log("sizeDisplay = $sizeDisplay")
        junkSizeLiveData.postValue(sizeDisplay)
    }

}